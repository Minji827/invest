package com.miyaong.invest.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.MacroIndicator
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.model.Stock
import com.miyaong.invest.data.model.TrendingStocksData
import com.miyaong.invest.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val macroIndicators: List<MacroIndicator> = emptyList(),
    val trendingStocks: TrendingStocksData? = null,
    val selectedTrendingTab: Int = 0, // 0=거래량, 1=상승률, 2=변동률
    val searchResults: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // 매크로 지표 로드
            when (val result = repository.getMacroIndicators()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(macroIndicators = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "매크로 지표를 불러올 수 없습니다"
                    )
                }
                else -> {}
            }

            // 인기 종목 로드
            when (val result = repository.getTrendingStocks()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(trendingStocks = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "인기 종목을 불러올 수 없습니다"
                    )
                }
                else -> {}
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun searchStocks(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            when (val result = repository.searchStocks(query)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(searchResults = result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "검색 결과를 불러올 수 없습니다"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectTrendingTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTrendingTab = tabIndex)
    }
}
