package com.miyaong.invest.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("quotes")
    val quotes: List<SearchQuote>
)

@Serializable
data class SearchQuote(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("shortname")
    val shortName: String? = null,

    @SerialName("longname")
    val longName: String? = null,

    @SerialName("exchDisp")
    val exchange: String? = null,

    @SerialName("typeDisp")
    val type: String? = null
)
