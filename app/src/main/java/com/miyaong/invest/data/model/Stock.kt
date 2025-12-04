package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("shortName")
    val shortName: String? = null,

    @SerialName("longName")
    val longName: String? = null,

    @SerialName("exchange")
    val exchange: String? = null,

    @SerialName("currency")
    val currency: String? = null,

    @SerialName("marketCap")
    val marketCap: Long? = null
)
