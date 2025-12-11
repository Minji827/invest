package com.miyaong.invest.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.BuyRecommendation
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.model.TradingHaltsData
import com.miyaong.invest.data.model.VolatilityWatchData
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
    val volatilityWatch: VolatilityWatchData? = null,
    val tradingHalts: TradingHaltsData? = null,
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

            // 변동성 주의 종목 로드
            when (val result = repository.getVolatilityWatch()) {
                is Result.Success -> {
                    _uiState.update { it.copy(volatilityWatch = result.data) }
                }
                is Result.Error -> {
                    // 에러 시 무시
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
