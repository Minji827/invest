package com.miyaong.invest.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChartResponse(
    @SerialName("chart")
    val chart: Chart
)

@Serializable
data class Chart(
    @SerialName("result")
    val result: List<ChartResult>?,

    @SerialName("error")
    val error: ApiError? = null
)

@Serializable
data class ChartResult(
    @SerialName("meta")
    val meta: ChartMeta,

    @SerialName("timestamp")
    val timestamp: List<Long>?,

    @SerialName("indicators")
    val indicators: Indicators
)

@Serializable
data class ChartMeta(
    @SerialName("currency")
    val currency: String,

    @SerialName("symbol")
    val symbol: String,

    @SerialName("exchangeName")
    val exchangeName: String,

    @SerialName("regularMarketPrice")
    val regularMarketPrice: Double
)

@Serializable
data class Indicators(
    @SerialName("quote")
    val quote: List<Quote>
)

@Serializable
data class Quote(
    @SerialName("open")
    val open: List<Double?>?,

    @SerialName("high")
    val high: List<Double?>?,

    @SerialName("low")
    val low: List<Double?>?,

    @SerialName("close")
    val close: List<Double?>?,

    @SerialName("volume")
    val volume: List<Long?>?
)

@Serializable
data class ApiError(
    @SerialName("code")
    val code: String,

    @SerialName("description")
    val description: String
)
