package com.miyaong.invest.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.BuyRecommendation
import com.miyaong.invest.data.model.CircuitBreakerData
import com.miyaong.invest.data.model.PredictionResult
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.model.TradingHaltsData
import com.miyaong.invest.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketAnalysisUiState(
    val isLoading: Boolean = false,
    val circuitBreaker: CircuitBreakerData? = null,
    val tradingHalts: TradingHaltsData? = null,
    val predictionTicker: String = "SPY",
    val prediction: PredictionResult? = null,
    val isPredicting: Boolean = false,
    val buyTicker: String = "AAPL",
    val buyRecommendation: BuyRecommendation? = null,
    val isBuyLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketAnalysisViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketAnalysisUiState())
    val uiState: StateFlow<MarketAnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 서킷브레이커 확률 로드
            when (val result = repository.getCircuitBreakerProbability()) {
                is Result.Success -> {
                    _uiState.update { it.copy(circuitBreaker = result.data) }
                }
                is Result.Error -> {
                    // 에러 시 기본값 사용
                }
                else -> {}
            }

            // 거래 정지 목록 로드
            when (val result = repository.getTradingHalts()) {
                is Result.Success -> {
                    _uiState.update { it.copy(tradingHalts = result.data) }
                }
                is Result.Error -> {
                    // 에러 시 무시
                }
                else -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setPredictionTicker(ticker: String) {
        _uiState.update { it.copy(predictionTicker = ticker) }
    }

    fun predictStock() {
        val ticker = _uiState.value.predictionTicker
        if (ticker.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPredicting = true, error = null) }

            when (val result = repository.predictStock(ticker, 7)) {
                is Result.Success -> {
                    _uiState.update { it.copy(prediction = result.data, isPredicting = false) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = "예측 실패: ${result.exception.message}",
                            isPredicting = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isPredicting = false) }
                }
            }
        }
    }

    fun setBuyTicker(ticker: String) {
        _uiState.update { it.copy(buyTicker = ticker) }
    }

    fun getBuyRecommendation() {
        val ticker = _uiState.value.buyTicker
        if (ticker.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBuyLoading = true, error = null) }

            when (val result = repository.getBuyRecommendation(ticker)) {
                is Result.Success -> {
                    _uiState.update { it.copy(buyRecommendation = result.data, isBuyLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = "매수단가 분석 실패: ${result.exception.message}",
                            isBuyLoading = false
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isBuyLoading = false) }
                }
            }
        }
    }
}
