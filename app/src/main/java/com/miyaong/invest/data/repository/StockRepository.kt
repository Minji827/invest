package com.miyaong.invest.data.repository

import com.miyaong.invest.data.api.MacroApiService
import com.miyaong.invest.data.api.PredictRequest
import com.miyaong.invest.data.api.StockApiService
import com.miyaong.invest.data.local.CacheDao
import com.miyaong.invest.data.local.FavoriteDao
import com.miyaong.invest.data.local.SearchHistoryDao
import com.miyaong.invest.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepository @Inject constructor(
    private val stockApi: StockApiService,
    private val macroApi: MacroApiService,
    private val favoriteDao: FavoriteDao,
    private val cacheDao: CacheDao,
    private val searchHistoryDao: SearchHistoryDao
) {

    // 주식 정보
    suspend fun getStockInfo(ticker: String): Result<Stock> {
        return try {
            val response = stockApi.getStockInfo(ticker)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 주가 히스토리
    suspend fun getStockHistory(ticker: String, period: String): Result<List<StockHistory>> {
        return try {
            val response = stockApi.getStockHistory(ticker, period)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 기술적 지표
    suspend fun getTechnicalIndicators(ticker: String): Result<List<TechnicalIndicator>> {
        return try {
            val response = stockApi.getTechnicalIndicators(ticker, "ma,rsi,macd,bb")
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 재무제표
    suspend fun getFinancialStatements(ticker: String, type: String = "quarterly"): Result<List<FinancialStatement>> {
        return try {
            val response = stockApi.getFinancialStatements(ticker, type)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 투자 지표
    suspend fun getInvestmentMetrics(ticker: String): Result<InvestmentMetrics> {
        return try {
            val response = stockApi.getInvestmentMetrics(ticker)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 배당 정보
    suspend fun getDividendInfo(ticker: String): Result<DividendInfo> {
        return try {
            val response = stockApi.getDividendInfo(ticker)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // AI 예측
    suspend fun predictStock(ticker: String, days: Int = 7): Result<PredictionResult> {
        return try {
            val request = PredictRequest(ticker, days)
            val response = stockApi.predictStock(request)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 주식 검색
    suspend fun searchStocks(query: String): Result<List<Stock>> {
        return try {
            val response = stockApi.searchStocks(query)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 인기 종목 (3가지 카테고리)
    suspend fun getTrendingStocks(): Result<TrendingStocksData> {
        return try {
            val response = stockApi.getTrendingStocks()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "TrendingStocks Error: ${e.message}", e)
            Result.Error(e)
        }
    }

    // 매크로 지표
    suspend fun getMacroIndicators(): Result<List<MacroIndicator>> {
        return try {
            val response = macroApi.getAllMacroIndicators()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "MacroIndicators Error: ${e.message}", e)
            Result.Error(e)
        }
    }

    // LULD 임박 종목
    suspend fun getVolatilityWatch(): Result<VolatilityWatchData> {
        return try {
            val response = stockApi.getVolatilityWatch()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "VolatilityWatch Error: ${e.message}", e)
            Result.Error(e)
        }
    }

    // AI 매수단가 추천
    suspend fun getBuyRecommendation(ticker: String): Result<BuyRecommendation> {
        return try {
            val response = stockApi.getBuyRecommendation(ticker)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "BuyRecommendation Error: ${e.message}", e)
            Result.Error(e)
        }
    }

    // 실시간 거래 정지 목록
    suspend fun getTradingHalts(): Result<TradingHaltsData> {
        return try {
            val response = stockApi.getTradingHalts()
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("StockRepository", "TradingHalts Error: ${e.message}", e)
            Result.Error(e)
        }
    }

    // 즐겨찾기
    fun getAllFavorites() = favoriteDao.getAllFavorites()

    fun isFavorite(ticker: String) = favoriteDao.isFavorite(ticker)

    suspend fun addFavorite(ticker: String, name: String) {
        favoriteDao.insertFavorite(
            com.miyaong.invest.data.local.FavoriteStock(ticker, name)
        )
    }

    suspend fun removeFavorite(ticker: String) {
        favoriteDao.deleteFavoriteByTicker(ticker)
    }

    // 검색 히스토리
    fun getSearchHistory() = searchHistoryDao.getRecentSearches()

    suspend fun addSearchHistory(ticker: String, name: String) {
        searchHistoryDao.insertSearch(
            com.miyaong.invest.data.local.SearchHistory(ticker = ticker, name = name)
        )
    }
}
