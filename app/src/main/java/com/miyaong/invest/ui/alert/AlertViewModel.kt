package com.miyaong.invest.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.local.PriceAlert
import com.miyaong.invest.data.local.PriceAlertDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertUiState(
    val activeAlerts: List<PriceAlert> = emptyList(),
    val triggeredAlerts: List<PriceAlert> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val priceAlertDao: PriceAlertDao
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
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addAlert(ticker: String, stockName: String, targetPrice: Double, currentPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            val alert = PriceAlert(
                ticker = ticker,
                stockName = stockName,
                targetPrice = targetPrice,
                currentPrice = currentPrice,
                isAbove = isAbove
            )
            priceAlertDao.insertAlert(alert)
            hideAddDialog()
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
