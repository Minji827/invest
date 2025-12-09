"""
Cron Job Script
This script is intended to be run periodically (e.g., once a day) to
pre-populate and update the stock data in the database for popular tickers.
"""
import sys
import os

# Add the script's directory to the Python path to allow importing 'app'
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app import app, get_stock_candles
from database import initialize_database, db

def update_popular_stocks():
    """
    Fetches the latest historical data for a predefined list of popular stocks
    and saves it to the database.
    
    The get_stock_candles function has the logic to check if the DB data is stale,
    so calling it is enough to trigger an update.
    """
    popular_tickers = ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'NVDA', 'META', 'TSLA']
    
    print("Starting cron job to update popular stocks...")
    
    # Use the Flask app context to ensure extensions like Flask-Caching
    # (even if not used by this function, it's good practice) and Peewee
    # can access the application's configuration.
    with app.app_context():
        # Ensure the database is initialized before running
        if not os.path.exists('stock_data.db'):
             initialize_database()

        for ticker in popular_tickers:
            print(f"Updating data for {ticker}...")
            try:
                # We fetch for 5 years to ensure the database is well-populated.
                # The get_stock_candles function will handle fetching and saving.
                get_stock_candles(symbol=ticker, days=1825)
            except Exception as e:
                print(f"  Error updating {ticker}: {e}")
                
    print("Cron job finished.")

if __name__ == '__main__':
    update_popular_stocks()
