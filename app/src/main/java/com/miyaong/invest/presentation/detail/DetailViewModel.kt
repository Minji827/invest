package com.miyaong.invest.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miyaong.invest.data.model.ChartDataPoint
import com.miyaong.invest.data.model.InvestmentStrategy
import com.miyaong.invest.data.model.Prediction
import com.miyaong.invest.data.model.StockQuote
import com.miyaong.invest.domain.repository.StockRepository
import com.miyaong.invest.domain.usecase.CalculateBuyPriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val symbol: String = "",
    val quote: StockQuote? = null,
    val chartData: List<ChartDataPoint> = emptyList(),
    val chartRange: ChartRange = ChartRange.ONE_YEAR,
    val macroData: com.miyaong.invest.data.model.MacroData? = null,
    val prediction: Prediction? = null,
    val selectedStrategy: InvestmentStrategy = InvestmentStrategy.LONG_TERM,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ChartRange(val displayName: String, val apiRange: String) {
    ONE_DAY("1일", "1d"),
    FIVE_DAYS("5일", "5d"),
    ONE_MONTH("1개월", "1mo"),
    THREE_MONTHS("3개월", "3mo"),
    SIX_MONTHS("6개월", "6mo"),
    ONE_YEAR("1년", "1y"),
    FIVE_YEARS("5년", "5y")
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val calculateBuyPriceUseCase: CalculateBuyPriceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle["symbol"])

    private val _uiState = MutableStateFlow(DetailUiState(symbol = symbol))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadStockData()
    }

    fun loadStockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 주식 시세 조회
            stockRepository.getStockQuote(symbol)
                .onSuccess { quote ->
                    _uiState.update { it.copy(quote = quote) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = "주식 정보를 불러오는데 실패했습니다: ${exception.message}")
                    }
                }

            // 매크로 데이터 조회
            stockRepository.getMacroData()
                .onSuccess { macroData ->
                    _uiState.update { it.copy(macroData = macroData) }
                }
                .onFailure { exception ->
                    // 매크로 데이터는 필수가 아니므로 에러를 무시
                }

            // 차트 데이터 조회
            loadChartData(_uiState.value.chartRange)
        }
    }

    fun changeChartRange(range: ChartRange) {
        _uiState.update { it.copy(chartRange = range) }
        loadChartData(range)
    }

    private fun loadChartData(range: ChartRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val interval = when (range) {
                ChartRange.ONE_DAY -> "5m"
                ChartRange.FIVE_DAYS -> "30m"
                ChartRange.ONE_MONTH -> "1d"
                else -> "1d"
            }

            stockRepository.getStockChart(symbol, interval, range.apiRange)
                .onSuccess { chart ->
                    val dataPoints = chart.timestamps.indices.map { i ->
                        ChartDataPoint(
                            timestamp = chart.timestamps[i],
                            open = chart.openPrices[i],
                            high = chart.highPrices[i],
                            low = chart.lowPrices[i],
                            close = chart.closePrices[i],
                            volume = chart.volumes[i]
                        )
                    }
                    _uiState.update { it.copy(chartData = dataPoints, isLoading = false) }

                    // 예측 계산
                    calculatePrediction(dataPoints)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = "차트 데이터를 불러오는데 실패했습니다: ${exception.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun changeStrategy(strategy: InvestmentStrategy) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
        calculatePrediction(_uiState.value.chartData)
    }

    private fun calculatePrediction(chartData: List<ChartDataPoint>) {
        val prediction = calculateBuyPriceUseCase(
            symbol = symbol,
            chartData = chartData,
            strategy = _uiState.value.selectedStrategy
        )
        _uiState.update { it.copy(prediction = prediction) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadStockData()
    }
}
