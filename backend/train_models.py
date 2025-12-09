"""
Model Training Script
This script trains the stock prediction models and saves them to disk.
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import yfinance as yf
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.svm import SVR
from sklearn.preprocessing import StandardScaler
import joblib
import os
import warnings
import requests
import time

warnings.filterwarnings('ignore')

# Create a directory to store models if it doesn't exist
MODELS_DIR = os.path.join(os.path.dirname(__file__), 'models')
if not os.path.exists(MODELS_DIR):
    os.makedirs(MODELS_DIR)

# --- Data Fetching Functions (copied from app.py, without caching) ---

# API Keys - It's better to use environment variables in a real app
FINNHUB_API_KEY = "d4era09r01qlhj152j00d4era09r01qlhj152j0g"
ALPHA_VANTAGE_API_KEY = "demo"

FINNHUB_BASE_URL = "https://finnhub.io/api/v1"
ALPHA_VANTAGE_BASE_URL = "https://www.alphavantage.co/query"

def finnhub_get(endpoint, params=None):
    if params is None:
        params = {}
    params['token'] = FINNHUB_API_KEY
    url = f"{FINNHUB_BASE_URL}{endpoint}"
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error calling Finnhub API: {e}")
        return None

def generate_mock_historical_data(symbol, days=30):
    base_prices = {'AAPL': 175.0, 'MSFT': 380.0, 'GOOGL': 140.0, 'AMZN': 180.0, 'NVDA': 480.0, 'META': 350.0, 'TSLA': 250.0}
    base_price = base_prices.get(symbol, 100.0)
    dates = pd.date_range(end=datetime.now(), periods=days, freq='D')
    np.random.seed(hash(symbol) % 2**32)
    returns = np.random.normal(0.001, 0.02, days)
    prices = [base_price * (1 + r) for r in np.cumprod(1 + returns)]
    data = [{'open': p * (1 + np.random.uniform(-0.01, 0.01)), 'high': p * (1 + np.random.uniform(0, 0.02)), 'low': p * (1 - np.random.uniform(0, 0.02)), 'close': p, 'volume': int(np.random.uniform(5e7, 1.5e8))} for p in prices]
    df = pd.DataFrame(data, index=dates)
    return df

def get_stock_candles_yfinance(symbol, days=365, max_retries=3):
    period_map = {30: '1mo', 90: '3mo', 180: '6mo', 365: '1y', 1825: '5y', 3650: 'max'}
    period = period_map.get(days, '1y')
    for attempt in range(max_retries):
        try:
            stock = yf.Ticker(symbol)
            hist = stock.history(period=period)
            if not hist.empty:
                hist.columns = [col.lower() for col in hist.columns]
                return hist
        except Exception as e:
            if '429' in str(e):
                time.sleep((attempt + 1) * 2)
            else:
                print(f"Error fetching from yfinance for {symbol}: {e}")
                return None
    return None

def get_stock_candles(symbol, resolution='D', days=365):
    # Alpha Vantage
    if ALPHA_VANTAGE_API_KEY != "demo":
        try:
            params = {'function': 'TIME_SERIES_DAILY', 'symbol': symbol, 'outputsize': 'full' if days > 100 else 'compact', 'apikey': ALPHA_VANTAGE_API_KEY}
            response = requests.get(ALPHA_VANTAGE_BASE_URL, params=params)
            data = response.json()
            if 'Time Series (Daily)' in data:
                df = pd.DataFrame.from_dict(data['Time Series (Daily)'], orient='index')
                df.columns = ['open', 'high', 'low', 'close', 'volume']
                df.index = pd.to_datetime(df.index)
                df = df.astype(float).sort_index()
                return df[df.index >= datetime.now() - timedelta(days=days)]
        except Exception as e:
            print(f"Alpha Vantage failed for {symbol}: {e}")

    # Yahoo Finance
    yf_df = get_stock_candles_yfinance(symbol, days)
    if yf_df is not None and not yf_df.empty:
        return yf_df

    # Finnhub
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
    
    print(f"All sources failed for {symbol}. Using mock data.")
    return generate_mock_historical_data(symbol, days)

# --- Training Logic ---

def train_and_save_models(ticker):
    """
    Trains all models for a given ticker and saves them to disk.
    """
    print(f"Starting training for {ticker}...")

    try:
        # 1. Fetch data
        df = get_stock_candles(ticker, 'D', 365)
        if df is None or len(df) < 60:
            print(f"Not enough historical data for {ticker}. Skipping.")
            return

        # 2. Prepare features
        df['returns'] = df['close'].pct_change()
        df['ma5'] = df['close'].rolling(5).mean()
        df['ma20'] = df['close'].rolling(20).mean()
        df['volatility'] = df['returns'].rolling(20).std()
        df = df.dropna()

        if len(df) < 20:
            print(f"Not enough data after feature engineering for {ticker}. Skipping.")
            return

        features = ['open', 'high', 'low', 'volume', 'ma5', 'ma20', 'volatility']
        X = df[features].values
        y = df['close'].values

        # 3. Scale features
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        # 4. Train models
        models = {
            'Linear Regression': LinearRegression(),
            'Random Forest': RandomForestRegressor(n_estimators=100, random_state=42),
            'SVM': SVR(kernel='rbf')
        }

        for name, model in models.items():
            print(f"  Training {name}...")
            model.fit(X_scaled, y)
            joblib.dump(model, os.path.join(MODELS_DIR, f"{ticker}_{name.replace(' ', '_')}.joblib"))

        # 5. Save the scaler
        joblib.dump(scaler, os.path.join(MODELS_DIR, f"{ticker}_scaler.joblib"))

        print(f"Successfully trained and saved models for {ticker}.")

    except Exception as e:
        print(f"An error occurred during training for {ticker}: {e}")

if __name__ == '__main__':
    popular_tickers = ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'NVDA', 'META', 'TSLA']
    for ticker in popular_tickers:
        train_and_save_models(ticker)