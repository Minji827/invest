package com.miyaong.invest.data.api

import com.miyaong.invest.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StockApiService {

    // 주식 기본 정보
    @GET("api/stock/info")
    suspend fun getStockInfo(
        @Query("ticker") ticker: String
    ): ApiResponse<Stock>

    // 과거 주가 데이터
    @GET("api/stock/historical")
    suspend fun getStockHistory(
        @Query("ticker") ticker: String,
        @Query("period") period: String = "1y" // 1mo, 3mo, 6mo, 1y, max
    ): ApiResponse<List<StockHistory>>

    // 기술적 지표
    @GET("api/stock/indicators")
    suspend fun getTechnicalIndicators(
        @Query("ticker") ticker: String,
        @Query("indicators") indicators: String // ma,rsi,macd,bb
    ): ApiResponse<List<TechnicalIndicator>>

    // 재무제표
    @GET("api/stock/financials")
    suspend fun getFinancialStatements(
        @Query("ticker") ticker: String,
        @Query("type") type: String = "quarterly" // quarterly, annual
    ): ApiResponse<List<FinancialStatement>>

    // 투자 지표
    @GET("api/stock/metrics")
    suspend fun getInvestmentMetrics(
        @Query("ticker") ticker: String
    ): ApiResponse<InvestmentMetrics>

    // 배당 정보
    @GET("api/stock/dividend")
    suspend fun getDividendInfo(
        @Query("ticker") ticker: String
    ): ApiResponse<DividendInfo>

    @GET("stock/financials")
    suspend fun getFinancials(
        @Query("ticker") ticker: String,
        @Query("sheet") sheet: String = "income",
        @Query("freq") freq: String = "annual"
    ): ApiResponse<List<FinancialStatement>>

    @GET("stock/financials")
    suspend fun getBalanceSheet(
        @Query("ticker") ticker: String,
        @Query("sheet") sheet: String = "balance",
        @Query("freq") freq: String = "annual"
    ): ApiResponse<List<BalanceSheet>>

    @GET("stock/financials")
    suspend fun getCashFlow(
        @Query("ticker") ticker: String,
        @Query("sheet") sheet: String = "cash",
        @Query("freq") freq: String = "annual"
    ): ApiResponse<List<CashFlow>>

    // AI 주가 예측
    @POST("api/stock/predict")
    suspend fun predictStock(
        @Body request: PredictRequest
    ): ApiResponse<PredictionResult>

    // 주식 검색
    @GET("api/stock/search")
    suspend fun searchStocks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Stock>>

    // 인기 종목 (3가지 카테고리)
    @GET("api/stock/trending")
    suspend fun getTrendingStocks(): ApiResponse<TrendingStocksData>

    // 시장 서킷브레이커 확률
    @GET("api/market/circuit-breaker")
    suspend fun getCircuitBreakerProbability(): ApiResponse<CircuitBreakerData>
}

interface MacroApiService {

    // 실시간 환율
    @GET("api/macro/exchange")
    suspend fun getExchangeRates(): ApiResponse<List<MacroIndicator>>

    // 달러 인덱스
    @GET("api/macro/dollar-index")
    suspend fun getDollarIndex(): ApiResponse<MacroIndicator>

    // 모든 매크로 지표
    @GET("api/macro/all")
    suspend fun getAllMacroIndicators(): ApiResponse<List<MacroIndicator>>
}

data class PredictRequest(
    val ticker: String,
    val days: Int = 7,
    val models: List<String> = listOf("linear", "rf", "svm")
)
