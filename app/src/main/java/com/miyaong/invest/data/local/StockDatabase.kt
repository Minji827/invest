package com.miyaong.invest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStock::class, StockCache::class, SearchHistory::class, PriceAlert::class],
    version = 2,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cacheDao(): CacheDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun priceAlertDao(): PriceAlertDao
}
