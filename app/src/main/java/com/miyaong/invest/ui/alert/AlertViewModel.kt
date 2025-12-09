package com.miyaong.invest.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.local.PriceAlert
import com.miyaong.invest.data.local.PriceAlertDao
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.model.Stock
import com.miyaong.invest.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertUiState(
    val activeAlerts: List<PriceAlert> = emptyList(),
    val triggeredAlerts: List<PriceAlert> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Stock> = emptyList(),
    val isSearching: Boolean = false,
    val selectedStock: Stock? = null,
    val error: String? = null
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val priceAlertDao: PriceAlertDao,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertUiState())
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            priceAlertDao.getActiveAlerts().collect { active ->
                _uiState.update { it.copy(activeAlerts = active) }
            }
        }
        viewModelScope.launch {
            priceAlertDao.getTriggeredAlerts().collect { triggered ->
                _uiState.update { it.copy(triggeredAlerts = triggered) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, searchQuery = "", searchResults = emptyList(), selectedStock = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, searchQuery = "", searchResults = emptyList(), selectedStock = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.length >= 1) {
            searchStocks(query)
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    private fun searchStocks(query: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSearching = true) }
                when (val result = stockRepository.searchStocks(query)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(searchResults = result.data, isSearching = false) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "검색 실패", isSearching = false) }
                    }
                    else -> {
                        _uiState.update { it.copy(isSearching = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "검색 중 오류: ${e.localizedMessage}", isSearching = false) }
            }
        }
    }

    fun selectStock(stock: Stock) {
        // Use the stock from search results directly (already has current price)
        _uiState.update {
            it.copy(
                selectedStock = stock,
                searchQuery = "",
                searchResults = emptyList(),
                isLoading = false
            )
        }
    }

    fun addAlert(targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            try {
                val stock = _uiState.value.selectedStock ?: return@launch
                val alert = PriceAlert(
                    ticker = stock.symbol,
                    stockName = stock.shortName ?: stock.longName ?: stock.symbol,
                    targetPrice = targetPrice,
                    currentPrice = stock.currentPrice ?: 0.0,
                    isAbove = isAbove
                )
                priceAlertDao.insertAlert(alert)
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "알림 추가 실패: ${e.localizedMessage}") }
            }
        }
    }

    fun deleteAlert(alert: PriceAlert) {
        viewModelScope.launch {
            priceAlertDao.deleteAlert(alert)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
