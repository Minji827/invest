package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockQuote(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("regularMarketPrice")
    val currentPrice: Double,

    @SerialName("regularMarketChange")
    val priceChange: Double,

    @SerialName("regularMarketChangePercent")
    val changePercent: Double,

    @SerialName("regularMarketOpen")
    val openPrice: Double? = null,

    @SerialName("regularMarketHigh")
    val highPrice: Double? = null,

    @SerialName("regularMarketLow")
    val lowPrice: Double? = null,

    @SerialName("regularMarketVolume")
    val volume: Long? = null,

    @SerialName("fiftyDayAverage")
    val ma50: Double? = null,

    @SerialName("twoHundredDayAverage")
    val ma200: Double? = null,

    @SerialName("trailingPE")
    val pe: Double? = null
)
