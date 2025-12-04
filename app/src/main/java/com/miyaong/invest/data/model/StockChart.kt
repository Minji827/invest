package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockChart(
    @SerialName("timestamp")
    val timestamps: List<Long>,

    @SerialName("open")
    val openPrices: List<Double>,

    @SerialName("high")
    val highPrices: List<Double>,

    @SerialName("low")
    val lowPrices: List<Double>,

    @SerialName("close")
    val closePrices: List<Double>,

    @SerialName("volume")
    val volumes: List<Long>
)

@Serializable
data class ChartDataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
