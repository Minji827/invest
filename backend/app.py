"""
Stock Pulse Backend Server
Flask API server for stock data, macro indicators, and ML predictions
Using Finnhub API for stock data with Yahoo Finance fallback
"""

from flask import Flask, jsonify, request
from flask_cors import CORS
from flask_caching import Cache
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import requests
import time
import yfinance as yf
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.svm import SVR
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
import warnings
import joblib
import os
from concurrent.futures import ThreadPoolExecutor, as_completed
from database import db, Stock, StockData
warnings.filterwarnings('ignore')

app = Flask(__name__)
CORS(app)

# Cache configuration
config = {
    "DEBUG": True,
    "CACHE_TYPE": "SimpleCache",  # Use in-memory cache
    "CACHE_DEFAULT_TIMEOUT": 300,  # Default cache timeout in seconds
}
app.config.from_mapping(config)
cache = Cache(app)

# API Keys
FINNHUB_API_KEY = "d4era09r01qlhj152j00d4era09r01qlhj152j0g"
EXCHANGE_RATE_API_KEY = "b0291ce736032066c1f7351c"
FRED_API_KEY = "abcdefghijklmnopqrstuvwxyz123456"
# Alpha Vantage free API key (get yours at https://www.alphavantage.co/support/#api-key)
ALPHA_VANTAGE_API_KEY = "demo"  # Replace with your key for production

FINNHUB_BASE_URL = "https://finnhub.io/api/v1"
ALPHA_VANTAGE_BASE_URL = "https://www.alphavantage.co/query"
MODELS_DIR = os.path.join(os.path.dirname(__file__), 'models')

# ============== Helper Functions ==============

@cache.cached(timeout=60)
def finnhub_get(endpoint, params=None):
    """Make a GET request to Finnhub API"""
    if params is None:
        params = {}
    params['token'] = FINNHUB_API_KEY
    url = f"{FINNHUB_BASE_URL}{endpoint}"
    response = requests.get(url, params=params)
    return response.json()

def format_large_number(num):
    """Format large numbers with K, M, B, T suffixes"""
    if num is None:
        return None
    if abs(num) >= 1e12:
        return f"{num/1e12:.2f}T"
    elif abs(num) >= 1e9:
        return f"{num/1e9:.2f}B"
    elif abs(num) >= 1e6:
        return f"{num/1e6:.2f}M"
    elif abs(num) >= 1e3:
        return f"{num/1e3:.2f}K"
    return str(num)

def calculate_technical_indicators(df):
    """Calculate technical indicators for stock data"""
    # Moving Averages
    df['ma5'] = df['close'].rolling(window=5).mean()
    df['ma20'] = df['close'].rolling(window=20).mean()
    df['ma60'] = df['close'].rolling(window=60).mean()
    df['ma120'] = df['close'].rolling(window=120).mean()

    # RSI
    delta = df['close'].diff()
    gain = (delta.where(delta > 0, 0)).rolling(window=14).mean()
    loss = (-delta.where(delta < 0, 0)).rolling(window=14).mean()
    rs = gain / loss
    df['rsi'] = 100 - (100 / (1 + rs))

    # MACD
    exp1 = df['close'].ewm(span=12, adjust=False).mean()
    exp2 = df['close'].ewm(span=26, adjust=False).mean()
    df['macd'] = exp1 - exp2
    df['macd_signal'] = df['macd'].ewm(span=9, adjust=False).mean()

    # Bollinger Bands
    df['bb_middle'] = df['close'].rolling(window=20).mean()
    std = df['close'].rolling(window=20).std()
    df['bb_upper'] = df['bb_middle'] + (std * 2)
    df['bb_lower'] = df['bb_middle'] - (std * 2)

    return df

def generate_mock_historical_data(symbol, days=30):
    """Generate mock historical data for testing"""
    # Base prices for popular stocks
    base_prices = {
        'AAPL': 175.0,
        'MSFT': 380.0,
        'GOOGL': 140.0,
        'AMZN': 180.0,
        'NVDA': 480.0,
        'META': 350.0,
        'TSLA': 250.0,
        'AMD': 140.0,
        'NFLX': 480.0,
        'INTC': 45.0
    }

    base_price = base_prices.get(symbol, 100.0)

    dates = pd.date_range(end=datetime.now(), periods=days, freq='D')
    np.random.seed(hash(symbol) % 2**32)

    # Generate realistic price movements
    returns = np.random.normal(0.001, 0.02, days)
    prices = [base_price]
    for r in returns[1:]:
        prices.append(prices[-1] * (1 + r))

    data = []
    for i, date in enumerate(dates):
        price = prices[i]
        volatility = np.random.uniform(0.005, 0.02)
        data.append({
            'open': price * (1 + np.random.uniform(-volatility, volatility)),
            'high': price * (1 + np.random.uniform(0, volatility * 2)),
            'low': price * (1 - np.random.uniform(0, volatility * 2)),
            'close': price,
            'volume': int(np.random.uniform(50000000, 150000000))
        })

    df = pd.DataFrame(data, index=dates)
    return df

def _get_candles_alpha_vantage(symbol, days):
    """Fetches daily stock data from Alpha Vantage."""
    if ALPHA_VANTAGE_API_KEY == "demo":
        return None  # Skip if using the demo key
    try:
        params = {
            'function': 'TIME_SERIES_DAILY',
            'symbol': symbol,
            'outputsize': 'full' if days > 100 else 'compact',
            'apikey': ALPHA_VANTAGE_API_KEY
        }
        response = requests.get(ALPHA_VANTAGE_BASE_URL, params=params)
        response.raise_for_status()
        data = response.json()
        if 'Time Series (Daily)' in data:
            df = pd.DataFrame.from_dict(data['Time Series (Daily)'], orient='index')
            df.columns = ['open', 'high', 'low', 'close', 'volume']
            df.index = pd.to_datetime(df.index)
            df = df.astype(float).sort_index()
            cutoff_date = datetime.now() - timedelta(days=days)
            return df[df.index >= cutoff_date]
    except Exception as e:
        print(f"Alpha Vantage failed for {symbol}: {e}")
    return None

def _get_candles_yfinance(symbol, days):
    """Fetches historical candle data from Yahoo Finance."""
    period_map = {30: '1mo', 90: '3mo', 180: '6mo', 365: '1y', 1825: '5y', 3650: 'max'}
    period = period_map.get(days, '1y')
    try:
        stock = yf.Ticker(symbol)
        hist = stock.history(period=period)
        if not hist.empty:
            hist.columns = [col.lower() for col in hist.columns]
            return hist
    except Exception as e:
        print(f"Yahoo Finance failed for {symbol}: {e}")
    return None

def _get_candles_finnhub(symbol, resolution, days):
    """Fetches historical candle data from Finnhub."""
    try:
        end_time = int(datetime.now().timestamp())
        start_time = int((datetime.now() - timedelta(days=days)).timestamp())
        finnhub_data = finnhub_get('/stock/candle', {'symbol': symbol, 'resolution': resolution, 'from': start_time, 'to': end_time})
        if finnhub_data and finnhub_data.get('s') != 'no_data' and 'c' in finnhub_data:
            df = pd.DataFrame(finnhub_data)
            df['date'] = pd.to_datetime(df['t'], unit='s')
            df = df.set_index('date').rename(columns={'o': 'open', 'h': 'high', 'l': 'low', 'c': 'close', 'v': 'volume'})
            return df
    except Exception as e:
        print(f"Finnhub failed for {symbol}: {e}")
    return None

def get_stock_data_from_db(symbol, days):
    """
    Retrieves historical stock data from the local SQLite database.
    """
    try:
        db.connect(reuse_if_open=True)
        stock, created = Stock.get_or_create(symbol=symbol)

        # Check if the data is recent (updated in the last 24 hours)
        if stock.last_updated and (datetime.now() - stock.last_updated).days < 1:
            cutoff_date = datetime.now().date() - timedelta(days=days)
            query = (StockData
                     .select()
                     .where(StockData.stock == stock, StockData.date >= cutoff_date)
                     .order_by(StockData.date))
            
            if query.exists():
                records = [{
                    "date": r.date, "open": r.open, "high": r.high,
                    "low": r.low, "close": r.close, "volume": r.volume
                } for r in query]
                
                df = pd.DataFrame(records)
                df = df.set_index('date')
                print(f"Successfully fetched data from DB for {symbol}")
                return df
    except Exception as e:
        print(f"Error reading from DB for {symbol}: {e}")
    finally:
        if not db.is_closed():
            db.close()
    return None

def save_stock_data_to_db(symbol, df):
    """
    Saves a DataFrame of historical stock data to the database.
    """
    try:
        db.connect(reuse_if_open=True)
        stock, created = Stock.get_or_create(symbol=symbol)
        
        data_to_insert = []
        for date, row in df.iterrows():
            data_to_insert.append({
                'stock': stock,
                'date': date.date(),
                'open': row['open'],
                'high': row['high'],
                'low': row['low'],
                'close': row['close'],
                'volume': row['volume']
            })
        
        if data_to_insert:
            # Use 'replace' to handle existing records
            with db.atomic():
                StockData.replace_many(data_to_insert).execute()

        # Update the last_updated timestamp
        stock.last_updated = datetime.now()
        stock.save()
        print(f"Successfully saved data to DB for {symbol}")

    except Exception as e:
        print(f"Error saving to DB for {symbol}: {e}")
    finally:
        if not db.is_closed():
            db.close()


def get_stock_candles(symbol, resolution='D', days=365):
    """
    Get historical candle data.
    1. Try to fetch from the local database.
    2. If not found or stale, fetch from external APIs in parallel.
    3. Save the newly fetched data to the database.
    """
    # 1. Try fetching from the database first
    df_db = get_stock_data_from_db(symbol, days)
    if df_db is not None and not df_db.empty:
        return df_db

    # 2. If not in DB or stale, fetch from external APIs
    print(f"No recent data in DB for {symbol}. Fetching from external APIs...")
    with ThreadPoolExecutor(max_workers=3) as executor:
        future_to_source = {
            executor.submit(_get_candles_yfinance, symbol, days): "Yahoo Finance",
            executor.submit(_get_candles_alpha_vantage, symbol, days): "Alpha Vantage",
            executor.submit(_get_candles_finnhub, symbol, resolution, days): "Finnhub",
        }

        for future in as_completed(future_to_source):
            source = future_to_source[future]
            try:
                result_df = future.result()
                if result_df is not None and not result_df.empty:
                    print(f"Successfully fetched data from {source} for {symbol}")
                    # 3. Save to database for next time
                    save_stock_data_to_db(symbol, result_df)
                    return result_df
            except Exception as exc:
                print(f'{source} generated an exception: {exc}')

    # Last fallback: generate mock data
    print(f"All real-time sources failed for {symbol}. Using mock data.")
    return generate_mock_historical_data(symbol, days)

# ============== Stock API Endpoints ==============

@app.route('/api/stock/info', methods=['GET'])
@cache.cached()
def get_stock_info():
    """Get basic stock information using yfinance"""
    ticker_symbol = request.args.get('ticker', '').upper()

    if not ticker_symbol:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        ticker = yf.Ticker(ticker_symbol)
        info = ticker.info
        
        # Check if we got valid data
        if not info or 'regularMarketPrice' not in info and 'currentPrice' not in info:
             # Try fetching history if info is incomplete (common yfinance issue)
             hist = ticker.history(period="1d")
             if hist.empty:
                return jsonify({"success": False, "error": "Stock not found"}), 404
             current_price = hist['Close'].iloc[-1]
        else:
             current_price = info.get('currentPrice') or info.get('regularMarketPrice')

        # Map yfinance info to our API response format
        data = {
            "symbol": ticker_symbol,
            "shortName": info.get('shortName', ticker_symbol),
            "longName": info.get('longName', ticker_symbol),
            "exchange": info.get('exchange', ''),
            "currency": info.get('currency', 'USD'),
            "marketCap": info.get('marketCap'),
            "currentPrice": current_price,
            "changePercent": ((current_price - info.get('previousClose', current_price)) / info.get('previousClose', 1)) * 100 if info.get('previousClose') else 0,
            "changeAmount": current_price - info.get('previousClose', current_price) if info.get('previousClose') else 0,
            "sector": info.get('sector', ''),
            "volume": info.get('volume') or info.get('regularMarketVolume'),
            "high52Week": info.get('fiftyTwoWeekHigh'),
            "low52Week": info.get('fiftyTwoWeekLow')
        }

        return jsonify({"success": True, "data": data})

    except Exception as e:
        print(f"Yahoo Finance failed for {ticker_symbol}: {e}. Trying Finnhub fallback...")
        try:
            # Fallback to Finnhub
            quote = finnhub_get('/quote', {'symbol': ticker_symbol})
            profile = finnhub_get('/stock/profile2', {'symbol': ticker_symbol})

            if not quote or quote.get('c') == 0:
                 return jsonify({"success": False, "error": "Stock not found in Finnhub"}), 404

            current_price = quote.get('c', 0)
            previous_close = quote.get('pc', current_price)
            change_amount = quote.get('d', 0)
            change_percent = quote.get('dp', 0)

            data = {
                "symbol": ticker_symbol,
                "shortName": profile.get('name', ticker_symbol),
                "longName": profile.get('name', ticker_symbol),
                "exchange": profile.get('exchange', ''),
                "currency": profile.get('currency', 'USD'),
                "marketCap": profile.get('marketCapitalization', 0) * 1000000, # Finnhub returns distinct unit
                "currentPrice": current_price,
                "changePercent": change_percent,
                "changeAmount": change_amount,
                "sector": profile.get('finnhubIndustry', ''),
                "volume": 0, # Quote doesn't have volume, could fetch from candle if needed but skipping for speed
                "high52Week": 0, # Not in basic quote
                "low52Week": 0
            }
            print(f"Successfully fetched info from Finnhub for {ticker_symbol}")
            return jsonify({"success": True, "data": data})

        except Exception as fallback_error:
            print(f"Finnhub fallback failed: {fallback_error}")
            return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/historical', methods=['GET'])
@cache.cached()
def get_stock_historical():
    """Get historical stock data"""
    ticker = request.args.get('ticker', '').upper()
    period = request.args.get('period', '1y')

    if not ticker:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    # Convert period to days
    period_days = {
        '1mo': 30,
        '3mo': 90,
        '6mo': 180,
        '1y': 365,
        '5y': 1825,
        'max': 3650
    }
    days = period_days.get(period, 365)

    try:
        df = get_stock_candles(ticker, 'D', days)

        if df is None or df.empty:
            return jsonify({"success": False, "error": "No historical data available"}), 404

        data = []
        for date, row in df.iterrows():
            data.append({
                "date": date.strftime('%Y-%m-%d'),
                "open": round(row['open'], 2),
                "high": round(row['high'], 2),
                "low": round(row['low'], 2),
                "close": round(row['close'], 2),
                "volume": int(row['volume'])
            })

        return jsonify({"success": True, "data": data})

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/indicators', methods=['GET'])
@cache.cached()
def get_technical_indicators():
    """Get technical indicators"""
    ticker = request.args.get('ticker', '').upper()

    if not ticker:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        df = get_stock_candles(ticker, 'D', 365)

        if df is None or df.empty:
            return jsonify({"success": False, "error": "No data available"}), 404

        df = calculate_technical_indicators(df)

        data = []
        for date, row in df.iterrows():
            data.append({
                "date": date.strftime('%Y-%m-%d'),
                "ma5": round(row['ma5'], 2) if pd.notna(row['ma5']) else None,
                "ma20": round(row['ma20'], 2) if pd.notna(row['ma20']) else None,
                "ma60": round(row['ma60'], 2) if pd.notna(row['ma60']) else None,
                "ma120": round(row['ma120'], 2) if pd.notna(row['ma120']) else None,
                "rsi": round(row['rsi'], 2) if pd.notna(row['rsi']) else None,
                "macd": round(row['macd'], 4) if pd.notna(row['macd']) else None,
                "macdSignal": round(row['macd_signal'], 4) if pd.notna(row['macd_signal']) else None,
                "bollingerUpper": round(row['bb_upper'], 2) if pd.notna(row['bb_upper']) else None,
                "bollingerMiddle": round(row['bb_middle'], 2) if pd.notna(row['bb_middle']) else None,
                "bollingerLower": round(row['bb_lower'], 2) if pd.notna(row['bb_lower']) else None
            })

        return jsonify({"success": True, "data": data})

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/financials', methods=['GET'])
@cache.cached()
def get_financial_statements():
    """Get financial statements using yfinance"""
    ticker_symbol = request.args.get('ticker', '').upper()
    
    if not ticker_symbol:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        ticker = yf.Ticker(ticker_symbol)
        
        # Try to get annual income statement
        financials = ticker.financials
        if financials.empty:
            financials = ticker.quarterly_financials
            
        if financials.empty:
             return jsonify({"success": True, "data": []})

        # Get the most recent period
        recent_date = financials.columns[0]
        recent_data = financials[recent_date]

        # Extract safely
        revenue = recent_data.get('Total Revenue') or recent_data.get('Revenue') or 0
        gross_profit = recent_data.get('Gross Profit') or 0
        operating_income = recent_data.get('Operating Income') or 0
        net_income = recent_data.get('Net Income') or 0
        
        # cost of revenue = revenue - gross profit (approx)
        cost_of_revenue = revenue - gross_profit

        data = [{
            "period": recent_date.strftime('%Y-%m-%d'),
            "revenue": int(revenue),
            "costOfRevenue": int(cost_of_revenue),
            "grossProfit": int(gross_profit),
            "operatingExpense": int(gross_profit - operating_income), # approx
            "operatingIncome": int(operating_income),
            "netIncome": int(net_income)
        }]

        return jsonify({"success": True, "data": data})

    except Exception as e:
        print(f"Error fetching financials for {ticker_symbol}: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/metrics', methods=['GET'])
@cache.cached()
def get_investment_metrics():
    """Get investment metrics using yfinance"""
    ticker_symbol = request.args.get('ticker', '').upper()

    if not ticker_symbol:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        ticker = yf.Ticker(ticker_symbol)
        info = ticker.info
        
        if not info:
             return jsonify({"success": False, "error": "Metrics not found"}), 404

        data = {
            "per": info.get('trailingPE') or info.get('forwardPE') or 0,
            "pbr": info.get('priceToBook') or 0,
            "psr": info.get('priceToSalesTrailing12Months') or 0,
            "evEbitda": info.get('enterpriseToEbitda') or 0,
            "roe": info.get('returnOnEquity') or 0,
            "roa": info.get('returnOnAssets') or 0,
            "operatingMargin": info.get('operatingMargins') or 0,
            "netMargin": info.get('profitMargins') or 0,
            "debtRatio": info.get('debtToEquity') or 0, # Note: yfinance returns this as %, e.g., 150.5
            "currentRatio": info.get('currentRatio') or 0,
            "quickRatio": info.get('quickRatio') or 0
        }

        return jsonify({"success": True, "data": data})

    except Exception as e:
        print(f"Error fetching metrics for {ticker_symbol}: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/dividend', methods=['GET'])
@cache.cached()
def get_dividend_info():
    """Get dividend information using yfinance"""
    ticker_symbol = request.args.get('ticker', '').upper()

    if not ticker_symbol:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        ticker = yf.Ticker(ticker_symbol)
        info = ticker.info
        
        if not info:
            # Return zeros if no info
             return jsonify({"success": True, "data": {
                "dividendYield": 0, "annualDividend": 0, "payoutRatio": 0,
                "dividendGrowth5Year": 0, "consecutiveYears": 0
            }})

        data = {
            "dividendYield": (info.get('dividendYield', 0) or 0) * 100, # yfinance returns e.g. 0.005 for 0.5%
            "annualDividend": info.get('dividendRate') or 0,
            "payoutRatio": (info.get('payoutRatio', 0) or 0) * 100,
            "dividendGrowth5Year": info.get('fiveYearAvgDividendYield') or 0, # Not exactly growth, but closest in info
            "consecutiveYears": 0 # Not available in basic info
        }

        return jsonify({"success": True, "data": data})

    except Exception as e:
        print(f"Error fetching dividend for {ticker_symbol}: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/predict', methods=['POST'])
def predict_stock():
    """AI stock price prediction using pre-trained models"""
    data = request.get_json()
    ticker = data.get('ticker', '').upper()

    if not ticker:
        return jsonify({"success": False, "error": "Ticker is required"}), 400

    try:
        # 1. Check if models for this ticker exist
        model_path = os.path.join(MODELS_DIR, f"{ticker}_Random_Forest.joblib")
        if not os.path.exists(model_path):
            return jsonify({"success": False, "error": f"Models for {ticker} are not trained yet."}), 404

        # 2. Load models and scaler
        models = {}
        for name in ['Linear Regression', 'Random Forest', 'SVM']:
            model_file = os.path.join(MODELS_DIR, f"{ticker}_{name.replace(' ', '_')}.joblib")
            models[name] = joblib.load(model_file)
        
        scaler_path = os.path.join(MODELS_DIR, f"{ticker}_scaler.joblib")
        scaler = joblib.load(scaler_path)

        # 3. Fetch latest data to get features
        df = get_stock_candles(ticker, 'D', 120) # Need enough data for MAs and volatility
        if df is None or len(df) < 60:
            return jsonify({"success": False, "error": "Not enough historical data to make a prediction."}), 400

        # 4. Prepare features for the last data point
        df['returns'] = df['close'].pct_change()
        df['ma5'] = df['close'].rolling(5).mean()
        df['ma20'] = df['close'].rolling(20).mean()
        df['volatility'] = df['returns'].rolling(20).std()
        df = df.dropna()

        features = ['open', 'high', 'low', 'volume', 'ma5', 'ma20', 'volatility']
        last_features = df[features].iloc[-1].values.reshape(1, -1)
        
        # 5. Scale features and predict
        last_features_scaled = scaler.transform(last_features)
        
        predictions = {}
        for name, model in models.items():
            predictions[name] = model.predict(last_features_scaled)[0]

        current_price = df['close'].iloc[-1]
        # Use Random Forest as the primary prediction
        predicted_price = predictions['Random Forest']
        expected_change = ((predicted_price - current_price) / current_price) * 100

        # Simplified response
        result = {
            "summary": {
                "currentPrice": round(current_price, 2),
                "predictedPrice": round(predicted_price, 2),
                "expectedChange": round(expected_change, 2),
                "confidence": "중간", # Confidence is now static as we don't calculate R2
                "bestModel": "Random Forest"
            },
            "predictions": [
                {
                    "modelName": "Linear Regression",
                    "predictedPrice": round(predictions['Linear Regression'], 2)
                },
                {
                    "modelName": "Random Forest",
                    "predictedPrice": round(predictions['Random Forest'], 2)
                },
                {
                    "modelName": "SVM",
                    "predictedPrice": round(predictions['SVM'], 2)
                }
            ]
        }

        return jsonify({"success": True, "data": result})

    except Exception as e:
        # Log the exception for debugging
        print(f"Error during prediction for {ticker}: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/stock/search', methods=['GET'])
def search_stocks():
    """Search for stocks using Yahoo Finance"""
    query = request.args.get('q', '')
    limit = int(request.args.get('limit', 10))

    if not query:
        return jsonify({"success": False, "error": "Query is required"}), 400

    try:
        # Use Yahoo Finance search API (unofficial but standard)
        url = f"https://query2.finance.yahoo.com/v1/finance/search?q={query}"
        headers = {'User-Agent': 'Mozilla/5.0'} # Required to avoid 403
        response = requests.get(url, headers=headers)
        data = response.json()

        results = []
        if 'quotes' in data:
            for item in data['quotes'][:limit]:
                # Filter for equity only
                if item.get('quoteType') == 'EQUITY' and item.get('isYaPro') is not True:
                     symbol = item.get('symbol')
                     
                     # Simple data, real-time price optional or skipped for speed in search
                     results.append({
                        "symbol": symbol,
                        "shortName": item.get('shortname', symbol),
                        "longName": item.get('longname', symbol),
                        "exchange": item.get('exchange', ''),
                        "currency": "USD", # Yahoo search results might mix currencies, simplifying here
                        "marketCap": None,
                        "currentPrice": 0, # Search endpoint doesn't give price
                        "changePercent": 0,
                        "changeAmount": 0,
                        "sector": item.get('sector', ''),
                        "volume": None,
                        "high52Week": None,
                        "low52Week": None
                     })

        return jsonify({"success": True, "data": results})

    except Exception as e:
        print(f"Search error: {e}")
        return jsonify({"success": False, "error": str(e)}), 500



# ============== Macro API Endpoints ==============

# ============== Macro API Endpoints ==============

def _fetch_exchange_rates():
    """Helper to fetch exchange rates logic"""
    try:
        url = f"https://v6.exchangerate-api.com/v6/{EXCHANGE_RATE_API_KEY}/latest/USD"
        response = requests.get(url)
        data = response.json()

        if data.get('result') != 'success':
            raise Exception("Failed to fetch exchange rates")

        rates = data.get('conversion_rates', {})

        result = [
            {
                "type": "USD/KRW",
                "name": "USD/KRW 환율",
                "value": rates.get('KRW', 0),
                "changePercent": 0.39,
                "changeAmount": 5.20,
                "timestamp": int(datetime.now().timestamp() * 1000),
                "unit": "₩"
            },
            {
                "type": "EUR/KRW",
                "name": "EUR/KRW 환율",
                "value": rates.get('KRW', 0) * rates.get('EUR', 1),
                "changePercent": -0.24,
                "changeAmount": -3.50,
                "timestamp": int(datetime.now().timestamp() * 1000),
                "unit": "₩"
            },
            {
                "type": "JPY/KRW",
                "name": "JPY/KRW 환율",
                "value": rates.get('KRW', 0) / rates.get('JPY', 1),
                "changePercent": 0.55,
                "changeAmount": 0.05,
                "timestamp": int(datetime.now().timestamp() * 1000),
                "unit": "₩"
            }
        ]
        return result
    except Exception as e:
        print(f"Exchange rate error: {e}")
        return []

def _fetch_dollar_index():
    """Helper to fetch dollar index logic with fallback"""
    try:
        # Try fetching from Yahoo Finance
        dxy = yf.Ticker("DX-Y.NYB")
        # period="5d" is safer to ensure we get some data even on weekends/holidays
        hist = dxy.history(period="5d")

        if hist.empty or len(hist) < 2:
            # Fallback for when "DX-Y.NYB" fails (sometimes yfinance issue)
            print("DX-Y.NYB data empty, trying fallback ticker 'UUP' (Bullish Fund)")
            dxy = yf.Ticker("UUP") 
            hist = dxy.history(period="5d")
            
        if hist.empty or len(hist) < 2:
             raise Exception("No data found for Dollar Index")

        latest_price = hist['Close'].iloc[-1]
        previous_price = hist['Close'].iloc[-2]
        
        # Handle division by zero
        if previous_price == 0:
            change_percent = 0
        else:
            change = latest_price - previous_price
            change_percent = (change / previous_price) * 100
            
        change = latest_price - previous_price

        result = {
            "type": "DXY",
            "name": "달러 인덱스 (DXY)",
            "value": round(float(latest_price), 2),
            "changePercent": round(float(change_percent), 2),
            "changeAmount": round(float(change), 2),
            "timestamp": int(datetime.now().timestamp() * 1000),
            "unit": ""
        }
        return result
    except Exception as e:
        print(f"Dollar Index error: {e}")
        # Return fallback hardcoded data to prevent app crash
        return {
            "type": "DXY", "name": "달러 인덱스 (DXY)", 
            "value": 104.5, 
            "changePercent": 0.05, 
            "changeAmount": 0.05, 
            "timestamp": int(datetime.now().timestamp() * 1000), 
            "unit": ""
        }

@app.route('/api/macro/exchange', methods=['GET'])
def get_exchange_rates():
    """Get exchange rates"""
    try:
        data = _fetch_exchange_rates()
        if not data:
            return jsonify({"success": False, "error": "Failed to fetch data"}), 500
        return jsonify({"success": True, "data": data})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/macro/dollar-index', methods=['GET'])
@cache.cached(timeout=3600)
def get_dollar_index():
    """Get Dollar Index"""
    try:
        data = _fetch_dollar_index()
        return jsonify({"success": True, "data": data})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/macro/all', methods=['GET'])
def get_all_macro_indicators():
    """Get all macro indicators"""
    try:
        # Get exchange rates directly
        exchange_data = _fetch_exchange_rates()
        
        # Get dollar index directly
        dxy_data = _fetch_dollar_index()

        all_data = []
        if exchange_data:
            all_data.extend(exchange_data)
        
        if dxy_data:
            all_data.append(dxy_data)

        return jsonify({"success": True, "data": all_data})

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

def _fetch_yahoo_screener(screener_id):
    """Fetch stocks from Yahoo Finance screener API"""
    url = f"https://query1.finance.yahoo.com/v1/finance/screener/predefined/saved?scrIds={screener_id}&count=10"
    headers = {'User-Agent': 'Mozilla/5.0'}
    
    try:
        response = requests.get(url, headers=headers, timeout=10)
        data = response.json()
        
        quotes = data.get('finance', {}).get('result', [{}])[0].get('quotes', [])
        
        result = []
        for quote in quotes[:10]:
            result.append({
                "symbol": quote.get('symbol', ''),
                "shortName": quote.get('shortName', quote.get('symbol', '')),
                "longName": quote.get('longName', quote.get('shortName', '')),
                "exchange": quote.get('exchange', 'NASDAQ'),
                "currency": quote.get('currency', 'USD'),
                "marketCap": quote.get('marketCap', 0),
                "currentPrice": quote.get('regularMarketPrice', 0),
                "changePercent": quote.get('regularMarketChangePercent', 0),
                "changeAmount": quote.get('regularMarketChange', 0),
                "sector": quote.get('sector', ''),
                "volume": quote.get('regularMarketVolume', 0),
                "high52Week": quote.get('fiftyTwoWeekHigh', 0),
                "low52Week": quote.get('fiftyTwoWeekLow', 0)
            })
        return result
    except Exception as e:
        print(f"Yahoo screener error for {screener_id}: {e}")
        return []

@app.route('/api/stock/trending', methods=['GET'])
@cache.cached(timeout=300) # Cache for 5 minutes
def get_trending_stocks():
    """Get trending stocks in 3 categories: Most Active, Top Gainers, Most Volatile"""
    category = request.args.get('category', 'all')  # 'active', 'gainers', 'volatile', or 'all'
    
    try:
        result = {}
        
        # Most Active (거래량 상위)
        if category in ['all', 'active']:
            result['mostActive'] = _fetch_yahoo_screener('most_actives')
        
        # Top Gainers (상승률 상위)
        if category in ['all', 'gainers']:
            result['topGainers'] = _fetch_yahoo_screener('day_gainers')
        
        # Most Volatile (변동률 상위) - using day losers + gainers combined for volatility
        if category in ['all', 'volatile']:
            # Yahoo doesn't have a direct "volatile" screener, so we use "day_losers" 
            # which shows high absolute change (negative)
            losers = _fetch_yahoo_screener('day_losers')
            gainers = _fetch_yahoo_screener('day_gainers')
            
            # Combine and sort by absolute change percent
            combined = losers + gainers
            combined.sort(key=lambda x: abs(x.get('changePercent', 0)), reverse=True)
            result['mostVolatile'] = combined[:10]
        
        # If single category requested, return flat list for backwards compatibility
        if category != 'all' and len(result) == 1:
            key = list(result.keys())[0]
            return jsonify({"success": True, "data": result[key]})
        
        return jsonify({"success": True, "data": result})
        
    except Exception as e:
        print(f"Trending stocks error: {e}")
        return jsonify({"success": False, "error": str(e)}), 500

# ============== Health Check ==============

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "timestamp": datetime.now().isoformat()})

@app.route('/', methods=['GET'])
def home():
    """Home endpoint"""
    return jsonify({
        "name": "Stock Pulse API",
        "version": "1.0.0",
        "data_source": "Finnhub",
        "endpoints": [
            "/api/stock/info",
            "/api/stock/historical",
            "/api/stock/indicators",
            "/api/stock/financials",
            "/api/stock/metrics",
            "/api/stock/dividend",
            "/api/stock/predict",
            "/api/stock/search",
            "/api/stock/trending",
            "/api/macro/exchange",
            "/api/macro/dollar-index",
            "/api/macro/all"
        ]
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True)
