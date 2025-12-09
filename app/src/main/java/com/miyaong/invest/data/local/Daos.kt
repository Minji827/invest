package com.miyaong.invest.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY `order` ASC, addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteStock>>

    @Query("SELECT * FROM favorites WHERE ticker = :ticker")
    suspend fun getFavorite(ticker: String): FavoriteStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteStock)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteStock)

    @Query("DELETE FROM favorites WHERE ticker = :ticker")
    suspend fun deleteFavoriteByTicker(ticker: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE ticker = :ticker)")
    fun isFavorite(ticker: String): Flow<Boolean>
}

@Dao
interface CacheDao {
    @Query("SELECT * FROM stock_cache WHERE ticker = :ticker AND ((:currentTime - cachedAt) < :maxAge)")
    suspend fun getCache(ticker: String, currentTime: Long, maxAge: Long = 300000): StockCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: StockCache)

    @Query("DELETE FROM stock_cache WHERE (:currentTime - cachedAt) > :maxAge")
    suspend fun deleteOldCache(currentTime: Long, maxAge: Long = 3600000)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    @Query("DELETE FROM search_history WHERE ticker = :ticker")
    suspend fun deleteSearch(ticker: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE isTriggered = 0")
    fun getActiveAlerts(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE isTriggered = 1 ORDER BY triggeredAt DESC")
    fun getTriggeredAlerts(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE id = :id")
    suspend fun getAlert(id: Int): PriceAlert?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert)

    @Update
    suspend fun updateAlert(alert: PriceAlert)

    @Delete
    suspend fun deleteAlert(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    @Query("UPDATE price_alerts SET isTriggered = 1, triggeredAt = :triggeredAt WHERE id = :id")
    suspend fun markAsTriggered(id: Int, triggeredAt: Long = System.currentTimeMillis())
}
