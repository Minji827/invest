package com.miyaong.invest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteStock(
    @PrimaryKey val ticker: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis(),
    val order: Int = 0
)

@Entity(tableName = "stock_cache")
data class StockCache(
    @PrimaryKey val ticker: String,
    val dataJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val stockName: String,
    val targetPrice: Double,
    val currentPrice: Double,
    val isAbove: Boolean, // true = 목표가 이상일 때 알림, false = 이하일 때 알림
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)
