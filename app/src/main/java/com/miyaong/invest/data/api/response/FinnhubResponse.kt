package com.miyaong.invest.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Finnhub 검색 응답
 */
@Serializable
data class FinnhubSearchResponse(
    @SerialName("count")
    val count: Int,

    @SerialName("result")
    val result: List<FinnhubSearchResult>
)

@Serializable
data class FinnhubSearchResult(
    @SerialName("description")
    val description: String,

    @SerialName("displaySymbol")
    val displaySymbol: String,

    @SerialName("symbol")
    val symbol: String,

    @SerialName("type")
    val type: String
)

/**
 * Finnhub 시세 응답
 */
@Serializable
data class FinnhubQuoteResponse(
    @SerialName("c")
    val current: Double, // Current price

    @SerialName("d")
    val change: Double?, // Change

    @SerialName("dp")
    val percentChange: Double?, // Percent change

    @SerialName("h")
    val high: Double?, // High price of the day

    @SerialName("l")
    val low: Double?, // Low price of the day

    @SerialName("o")
    val open: Double?, // Open price of the day

    @SerialName("pc")
    val previousClose: Double?, // Previous close price

    @SerialName("t")
    val timestamp: Long? // Timestamp
)

/**
 * Finnhub 캔들 응답 (차트 데이터)
 */
@Serializable
data class FinnhubCandleResponse(
    @SerialName("c")
    val close: List<Double>?,

    @SerialName("h")
    val high: List<Double>?,

    @SerialName("l")
    val low: List<Double>?,

    @SerialName("o")
    val open: List<Double>?,

    @SerialName("s")
    val status: String, // "ok" or "no_data"

    @SerialName("t")
    val timestamp: List<Long>?,

    @SerialName("v")
    val volume: List<Long>?
)
