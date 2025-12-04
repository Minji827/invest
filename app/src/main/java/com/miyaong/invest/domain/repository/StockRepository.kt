package com.miyaong.invest.domain.repository

import com.miyaong.invest.data.model.MacroData
import com.miyaong.invest.data.model.StockChart
import com.miyaong.invest.data.model.StockQuote
import com.miyaong.invest.data.api.response.SearchQuote
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    /**
     * 주식 검색
     */
    suspend fun searchStocks(query: String): Result<List<SearchQuote>>

    /**
     * 주식 시세 조회
     */
    suspend fun getStockQuote(symbol: String): Result<StockQuote>

    /**
     * 주가 차트 데이터 조회
     */
    suspend fun getStockChart(
        symbol: String,
        interval: String = "1d",
        range: String = "1y"
    ): Result<StockChart>

    /**
     * 실시간 주가 구독 (Flow)
     */
    fun subscribeToStockUpdates(symbol: String): Flow<StockQuote>

    /**
     * 매크로 경제 지표 조회 (환율, 달러 인덱스)
     */
    suspend fun getMacroData(): Result<MacroData>
}
