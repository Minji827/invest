package com.miyaong.invest.data.api

import com.miyaong.invest.data.api.response.FinnhubQuoteResponse
import com.miyaong.invest.data.api.response.FinnhubCandleResponse
import com.miyaong.invest.data.api.response.FinnhubSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {
    /**
     * 주식 검색
     * @param query 검색어
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String
    ): FinnhubSearchResponse

    /**
     * 실시간 시세 조회
     * @param symbol 종목 코드 (예: AAPL)
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String
    ): FinnhubQuoteResponse

    /**
     * 차트 데이터 (캔들스틱)
     * @param symbol 종목 코드
     * @param resolution 해상도 (1, 5, 15, 30, 60, D, W, M)
     * @param from 시작 시간 (Unix timestamp)
     * @param to 종료 시간 (Unix timestamp)
     */
    @GET("stock/candle")
    suspend fun getCandle(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): FinnhubCandleResponse

    /**
     * 회사 프로필
     * @param symbol 종목 코드
     */
    @GET("stock/profile2")
    suspend fun getProfile(
        @Query("symbol") symbol: String
    ): FinnhubProfileResponse
}

// 간단한 응답 모델 (별도 파일로 분리 가능)
@kotlinx.serialization.Serializable
data class FinnhubProfileResponse(
    @kotlinx.serialization.SerialName("name")
    val name: String? = null,

    @kotlinx.serialization.SerialName("ticker")
    val ticker: String? = null,

    @kotlinx.serialization.SerialName("exchange")
    val exchange: String? = null,

    @kotlinx.serialization.SerialName("marketCapitalization")
    val marketCap: Double? = null
)
