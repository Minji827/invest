package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CircuitBreaker(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("status")
    val status: CircuitStatus,

    @SerialName("currentPrice")
    val currentPrice: Double,

    @SerialName("upperLimit")
    val upperLimit: Double? = null,

    @SerialName("lowerLimit")
    val lowerLimit: Double? = null,

    @SerialName("probability")
    val probability: Double? = null, // AMEX 시장용 상승/하락 확률

    @SerialName("timestamp")
    val timestamp: Long
)

@Serializable
enum class CircuitStatus {
    @SerialName("normal")
    NORMAL,      // 정상

    @SerialName("upper")
    UPPER,       // 상한가 (상킷)

    @SerialName("lower")
    LOWER,       // 하한가 (하킷)

    @SerialName("warning")
    WARNING      // 경고
}

@Serializable
data class CircuitBreakerData(
    @SerialName("market")
    val market: String,  // AMEX, NYSE, NASDAQ 등

    @SerialName("currentLevel")
    val currentLevel: Int,  // 0, 1, 2, 3

    @SerialName("probability")
    val probability: Double,  // 서킷 발동 확률 (0~100)

    @SerialName("indexValue")
    val indexValue: Double,  // 현재 지수값

    @SerialName("indexChange")
    val indexChange: Double,  // 지수 변동률

    @SerialName("threshold1")
    val threshold1: Double = -7.0,  // 1단계 기준 (-7%)

    @SerialName("threshold2")
    val threshold2: Double = -13.0, // 2단계 기준 (-13%)

    @SerialName("threshold3")
    val threshold3: Double = -20.0, // 3단계 기준 (-20%)

    @SerialName("timestamp")
    val timestamp: Long
)

// 매수단가 추천 응답
@Serializable
data class BuyRecommendation(
    @SerialName("ticker")
    val ticker: String,

    @SerialName("currentPrice")
    val currentPrice: Double,

    @SerialName("recommendations")
    val recommendations: BuyPriceRecommendations,

    @SerialName("analysis")
    val analysis: TechnicalAnalysis,

    @SerialName("mlConfidence")
    val mlConfidence: Double,

    @SerialName("timestamp")
    val timestamp: Long
)

@Serializable
data class BuyPriceRecommendations(
    @SerialName("aggressive")
    val aggressive: BuyPriceLevel,

    @SerialName("moderate")
    val moderate: BuyPriceLevel,

    @SerialName("conservative")
    val conservative: BuyPriceLevel
)

@Serializable
data class BuyPriceLevel(
    @SerialName("price")
    val price: Double,

    @SerialName("discount")
    val discount: Double,

    @SerialName("reason")
    val reason: String
)

@Serializable
data class TechnicalAnalysis(
    @SerialName("rsi")
    val rsi: Double,

    @SerialName("rsiStatus")
    val rsiStatus: String,

    @SerialName("bollingerLower")
    val bollingerLower: Double,

    @SerialName("bollingerPosition")
    val bollingerPosition: Double,

    @SerialName("nearestSupport")
    val nearestSupport: Double,

    @SerialName("low52Week")
    val low52Week: Double,

    @SerialName("atr")
    val atr: Double,

    @SerialName("volatility")
    val volatility: Double
)
