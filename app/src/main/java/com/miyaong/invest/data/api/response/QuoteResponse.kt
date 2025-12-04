package com.miyaong.invest.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteResponse(
    @SerialName("quoteResponse")
    val quoteResponse: QuoteData
)

@Serializable
data class QuoteData(
    @SerialName("result")
    val result: List<QuoteResult>?,

    @SerialName("error")
    val error: ApiError? = null
)

@Serializable
data class QuoteResult(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("shortName")
    val shortName: String? = null,

    @SerialName("longName")
    val longName: String? = null,

    @SerialName("exchange")
    val exchange: String? = null,

    @SerialName("regularMarketPrice")
    val regularMarketPrice: Double,

    @SerialName("regularMarketChange")
    val regularMarketChange: Double,

    @SerialName("regularMarketChangePercent")
    val regularMarketChangePercent: Double,

    @SerialName("regularMarketOpen")
    val regularMarketOpen: Double? = null,

    @SerialName("regularMarketDayHigh")
    val regularMarketDayHigh: Double? = null,

    @SerialName("regularMarketDayLow")
    val regularMarketDayLow: Double? = null,

    @SerialName("regularMarketVolume")
    val regularMarketVolume: Long? = null,

    @SerialName("fiftyDayAverage")
    val fiftyDayAverage: Double? = null,

    @SerialName("twoHundredDayAverage")
    val twoHundredDayAverage: Double? = null,

    @SerialName("trailingPE")
    val trailingPE: Double? = null,

    @SerialName("marketCap")
    val marketCap: Long? = null
)
