package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MacroData(
    @SerialName("usdkrw")
    val usdKrw: Double? = null, // 달러-원 환율

    @SerialName("dollarIndex")
    val dollarIndex: Double? = null, // 달러 인덱스 (DXY)

    @SerialName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
