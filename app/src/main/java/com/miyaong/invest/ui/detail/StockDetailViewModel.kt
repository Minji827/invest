package com.miyaong.invest.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.*
import com.miyaong.invest.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockDetailUiState(
    val stock: Stock? = null,
    val history: List<StockHistory> = emptyList(),
    val indicators: List<TechnicalIndicator> = emptyList(),
    val financials: List<FinancialStatement> = emptyList(),
    val metrics: InvestmentMetrics? = null,
    val dividend: DividendInfo? = null,
    val prediction: PredictionResult? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isChartLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val selectedPeriod: String = "6mo",
    val selectedFinancialType: String = "quarterly"
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val repository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticker: String = savedStateHandle.get<String>("ticker") ?: ""

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    init {
        if (ticker.isNotEmpty()) {
            loadStockInfo()
            loadChartData()
            observeFavoriteStatus()
        }
    }

    fun loadStockInfo() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // 기본 정보
                when (val result = repository.getStockInfo(ticker)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(stock = result.data)
                    }
                    is Result.Error -> {
                        // 차트 데이터가 있으면 에러 무시 (Fallback이 처리함)
                        if (_uiState.value.history.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                error = result.message ?: "주식 정보를 불러올 수 없습니다"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                if (_uiState.value.history.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "주식 정보를 불러오는 중 오류가 발생했습니다: ${e.localizedMessage}"
                    )
                }
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadChartData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isChartLoading = true)
                
                // 주가 히스토리
                when (val result = repository.getStockHistory(ticker, _uiState.value.selectedPeriod)) {
                    is Result.Success -> {
                        val history = result.data
                        var currentStock = _uiState.value.stock
                        
                        // Fallback: 상세 정보 API가 실패하여 stock이 없는 경우, 차트 데이터 최신값으로 채워넣기
                        if (currentStock == null && history.isNotEmpty()) {
                            val latest = history.last()
                            val previous = if (history.size > 1) history[history.size - 2] else latest
                            
                            val price = latest.close
                            val changeAmount = price - previous.close
                            val changePercent = if (previous.close != 0.0) (changeAmount / previous.close) * 100 else 0.0
                            
                            currentStock = Stock(
                                symbol = ticker,
                                shortName = ticker,
                                currentPrice = price,
                                changeAmount = changeAmount,
                                changePercent = changePercent,
                                currency = "USD"
                            )
                            
                            val currentError = _uiState.value.error
                            val newError = if (currentError?.contains("주식 정보") == true) null else currentError

                            _uiState.value = _uiState.value.copy(
                                history = history, 
                                stock = currentStock,
                                error = newError
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(history = history)
                        }
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "차트 데이터를 불러올 수 없습니다"
                        )
                    }
                    else -> {}
                }

                // 기술적 지표
                when (val result = repository.getTechnicalIndicators(ticker)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(indicators = result.data)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "차트 데이터를 불러오는 중 오류가 발생했습니다: ${e.localizedMessage}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isChartLoading = false)
            }
        }
    }

    fun loadFinancialData() {
        viewModelScope.launch {
            when (val result = repository.getFinancialStatements(ticker, _uiState.value.selectedFinancialType)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(financials = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "재무제표를 불러올 수 없습니다"
                    )
                }
                else -> {}
            }
        }
    }

    fun loadMetricsData() {
        viewModelScope.launch {
            when (val result = repository.getInvestmentMetrics(ticker)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(metrics = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "투자지표를 불러올 수 없습니다"
                    )
                }
                else -> {}
            }
        }
    }

    fun loadDividendData() {
        viewModelScope.launch {
            when (val result = repository.getDividendInfo(ticker)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(dividend = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "배당정보를 불러올 수 없습니다"
                    )
                }
                else -> {}
            }
        }
    }

    fun loadPredictionData(days: Int = 7) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                when (val result = repository.predictStock(ticker, days)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(prediction = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "예측 데이터를 불러올 수 없습니다"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "예측 데이터를 불러오는 중 오류가 발생했습니다: ${e.localizedMessage}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        when (index) {
            0 -> loadChartData()
            1 -> loadFinancialData()
            2 -> loadMetricsData()
            3 -> loadDividendData()
            4 -> loadPredictionData()
        }
    }

    fun selectPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadChartData()
    }

    fun selectFinancialType(type: String) {
        _uiState.value = _uiState.value.copy(selectedFinancialType = type)
        loadFinancialData()
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val stock = _uiState.value.stock ?: return@launch
            if (_uiState.value.isFavorite) {
                repository.removeFavorite(ticker)
            } else {
                repository.addFavorite(ticker, stock.shortName ?: stock.longName ?: ticker)
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            repository.isFavorite(ticker).collect { isFavorite ->
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
