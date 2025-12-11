package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VolatilityWatchData(
    @SerialName("upward_watch")
    val upwardWatch: List<VolatileStock>,

    @SerialName("downward_watch")
    val downwardWatch: List<VolatileStock>,

    @SerialName("timestamp")
    val timestamp: Long
)

@Serializable
data class VolatileStock(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("name")
    val name: String,

    @SerialName("changePercent")
    val changePercent: Double,

    @SerialName("currentPrice")
    val currentPrice: Double
)
