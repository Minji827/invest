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
