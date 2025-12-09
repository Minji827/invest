import peewee as pw
import os
from datetime import datetime

# Get the directory of the current script
db_dir = os.path.dirname(os.path.abspath(__file__))
db_path = os.path.join(db_dir, 'stock_data.db')

# Create a database instance
db = pw.SqliteDatabase(db_path)

class BaseModel(pw.Model):
    class Meta:
        database = db

class Stock(BaseModel):
    """A stock ticker."""
    symbol = pw.CharField(unique=True, index=True)
    last_updated = pw.DateTimeField(null=True)

class StockData(BaseModel):
    """Historical daily stock data."""
    stock = pw.ForeignKeyField(Stock, backref='data_points')
    date = pw.DateField()
    open = pw.FloatField()
    high = pw.FloatField()
    low = pw.FloatField()
    close = pw.FloatField()
    volume = pw.BigIntegerField()

    class Meta:
        # Create a composite key to ensure one data point per stock per day
        primary_key = pw.CompositeKey('stock', 'date')
        indexes = (
            (('stock', 'date'), True),
        )

def initialize_database():
    """Create the database and tables if they don't exist."""
    db.connect()
    db.create_tables([Stock, StockData], safe=True)
    db.close()

if __name__ == '__main__':
    initialize_database()
    print("Database initialized successfully.")
