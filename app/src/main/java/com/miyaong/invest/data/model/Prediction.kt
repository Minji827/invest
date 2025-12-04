package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prediction(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("strategy")
    val strategy: InvestmentStrategy,

    @SerialName("targetPrice")
    val targetPrice: Double,

    @SerialName("confidence")
    val confidence: Double,

    @SerialName("predictions")
    val predictions: List<PricePrediction>,

    @SerialName("recommendation")
    val recommendation: String
)

@Serializable
data class PricePrediction(
    @SerialName("date")
    val date: Long,

    @SerialName("price")
    val price: Double,

    @SerialName("upperBound")
    val upperBound: Double? = null,

    @SerialName("lowerBound")
    val lowerBound: Double? = null
)

@Serializable
enum class InvestmentStrategy {
    @SerialName("short_term")
    SHORT_TERM,  // 단타

    @SerialName("long_term")
    LONG_TERM    // 장기
}
