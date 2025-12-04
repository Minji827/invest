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
