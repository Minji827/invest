package com.miyaong.invest.data.api

import com.miyaong.invest.data.api.response.ChartResponse
import com.miyaong.invest.data.api.response.QuoteResponse
import com.miyaong.invest.data.api.response.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApi {
    /**
     * 주가 차트 데이터 조회
     * @param symbol 종목 코드 (예: AAPL, TSLA)
     * @param interval 간격 (1m, 5m, 15m, 1h, 1d, 1wk, 1mo)
     * @param range 기간 (1d, 5d, 1mo, 3mo, 6mo, 1y, 5y, max)
     */
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "1y"
    ): ChartResponse

    /**
     * 주식 시세 정보 조회
     * @param symbols 종목 코드 리스트 (쉼표로 구분)
     */
    @GET("v7/finance/quote")
    suspend fun getQuote(
        @Query("symbols") symbols: String
    ): QuoteResponse

    /**
     * 주식 검색
     * @param query 검색어
     */
    @GET("v1/finance/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("quotesCount") quotesCount: Int = 10
    ): SearchResponse
}

// API 응답을 위한 래퍼 클래스들은 별도 파일에 정의
