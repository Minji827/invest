package com.miyaong.invest.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.model.Stock
import com.miyaong.invest.data.model.StockHistory
import com.miyaong.invest.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockDetailUiState(
    val ticker: String = "",
    val stock: Stock? = null,
    val history: List<StockHistory> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isChartLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: String = "6mo"
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val repository: StockRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticker: String = savedStateHandle.get<String>("ticker") ?: ""

    private val _uiState = MutableStateFlow(StockDetailUiState(ticker = ticker))
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.getStockInfo(ticker)) {
                is Result.Success -> {
                    _uiState.update { it.copy(stock = result.data, isLoading = false) }
                }
                is Result.Error -> {
                    if (_uiState.value.stock == null && _uiState.value.history.isEmpty()) {
                        _uiState.update { it.copy(error = result.message ?: "주식 정보를 불러올 수 없습니다", isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun loadChartData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChartLoading = true) }
            
            // 주가 히스토리 (Fallback용)
            when (val result = repository.getStockHistory(ticker, _uiState.value.selectedPeriod)) {
                is Result.Success -> {
                    val history = result.data
                    _uiState.update { state ->
                        var currentStock = state.stock
                        
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
                            
                            state.copy(
                                history = history, 
                                stock = currentStock,
                                error = null, // Clear error if fallback succeeded
                                isChartLoading = false
                            )
                        } else {
                            state.copy(history = history, isChartLoading = false)
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isChartLoading = false) }
                }
                else -> {
                    _uiState.update { it.copy(isChartLoading = false) }
                }
            }
        }
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
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }
}
