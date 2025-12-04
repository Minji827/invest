package com.miyaong.invest.data.repository

import com.miyaong.invest.data.api.FinnhubApi
import com.miyaong.invest.data.api.response.SearchQuote
import com.miyaong.invest.data.model.StockChart
import com.miyaong.invest.data.model.StockQuote
import com.miyaong.invest.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val finnhubApi: FinnhubApi
) : StockRepository {

    override suspend fun searchStocks(query: String): Result<List<SearchQuote>> {
        return try {
            val response = finnhubApi.search(query)
            // Finnhub 응답을 SearchQuote로 변환
            val quotes = response.result.map { result ->
                SearchQuote(
                    symbol = result.symbol,
                    shortName = result.displaySymbol,
                    longName = result.description,
                    exchange = null,
                    type = result.type
                )
            }
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStockQuote(symbol: String): Result<StockQuote> {
        return try {
            val response = finnhubApi.getQuote(symbol)

            val quote = StockQuote(
                symbol = symbol,
                currentPrice = response.current,
                priceChange = response.change ?: 0.0,
                changePercent = response.percentChange ?: 0.0,
                openPrice = response.open,
                highPrice = response.high,
                lowPrice = response.low,
                volume = null, // Finnhub quote에는 volume이 없음
                ma50 = null,
                ma200 = null,
                pe = null
            )

            Result.success(quote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStockChart(
        symbol: String,
        interval: String,
        range: String
    ): Result<StockChart> {
        return try {
            // Finnhub resolution: 1, 5, 15, 30, 60, D, W, M
            val resolution = when (interval) {
                "5m" -> "5"
                "30m" -> "30"
                "1h" -> "60"
                "1d" -> "D"
                else -> "D"
            }

            // 시간 범위 계산
            val toTime = System.currentTimeMillis() / 1000
            val fromTime = when (range) {
                "1d" -> toTime - (24 * 60 * 60)
                "5d" -> toTime - (5 * 24 * 60 * 60)
                "1mo" -> toTime - (30 * 24 * 60 * 60)
                "3mo" -> toTime - (90 * 24 * 60 * 60)
                "6mo" -> toTime - (180 * 24 * 60 * 60)
                "1y" -> toTime - (365 * 24 * 60 * 60)
                "5y" -> toTime - (5 * 365 * 24 * 60 * 60)
                else -> toTime - (365 * 24 * 60 * 60)
            }

            val response = finnhubApi.getCandle(symbol, resolution, fromTime, toTime)

            if (response.status != "ok" || response.close == null) {
                return Result.failure(Exception("차트 데이터를 찾을 수 없습니다"))
            }

            val chart = StockChart(
                timestamps = response.timestamp ?: emptyList(),
                openPrices = response.open ?: emptyList(),
                highPrices = response.high ?: emptyList(),
                lowPrices = response.low ?: emptyList(),
                closePrices = response.close,
                volumes = response.volume ?: emptyList()
            )

            Result.success(chart)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun subscribeToStockUpdates(symbol: String): Flow<StockQuote> = flow {
        // 실시간 업데이트는 나중에 WebSocket으로 구현
        // 현재는 주기적으로 폴링하는 방식으로 구현
        while (true) {
            getStockQuote(symbol).getOrNull()?.let { emit(it) }
            kotlinx.coroutines.delay(5000) // 5초마다 업데이트
        }
    }

    override suspend fun getMacroData(): Result<com.miyaong.invest.data.model.MacroData> {
        return try {
            // Finnhub에서 외환 데이터 조회
            // KRW/USD는 OANDA:KRW_USD 형식
            val usdKrwQuote = try {
                finnhubApi.getQuote("OANDA:USD_KRW")
            } catch (e: Exception) {
                null
            }

            // 달러 인덱스는 DXY
            val dxyQuote = try {
                finnhubApi.getQuote("DXY")
            } catch (e: Exception) {
                null
            }

            val macroData = com.miyaong.invest.data.model.MacroData(
                usdKrw = usdKrwQuote?.current,
                dollarIndex = dxyQuote?.current
            )

            Result.success(macroData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
