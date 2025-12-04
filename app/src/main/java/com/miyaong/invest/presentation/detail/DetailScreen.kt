package com.miyaong.invest.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    symbol: String,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.symbol) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.quote == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 에러 메시지
                    uiState.error?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 주식 정보 카드
                    uiState.quote?.let { quote ->
                        StockInfoCard(quote)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 매크로 데이터 카드
                    uiState.macroData?.let { macroData ->
                        MacroDataCard(macroData)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 예측 정보 카드
                    uiState.prediction?.let { prediction ->
                        PredictionCard(
                            prediction = prediction,
                            selectedStrategy = uiState.selectedStrategy,
                            onStrategyChange = viewModel::changeStrategy
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 차트 범위 선택
                    ChartRangeSelector(
                        selectedRange = uiState.chartRange,
                        onRangeSelected = viewModel::changeChartRange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 주가 차트
                    if (uiState.chartData.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            StockChart(
                                data = uiState.chartData,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    } else if (!uiState.isLoading) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("차트 데이터 없음")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockInfoCard(quote: com.miyaong.invest.data.model.StockQuote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = quote.symbol,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$${String.format("%.2f", quote.currentPrice)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                val changeColor = if (quote.priceChange >= 0) Color.Green else Color.Red
                Text(
                    text = "${if (quote.priceChange >= 0) "+" else ""}${String.format("%.2f", quote.priceChange)} (${String.format("%.2f", quote.changePercent)}%)",
                    style = MaterialTheme.typography.titleMedium,
                    color = changeColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 추가 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("시가", quote.openPrice?.let { "$${String.format("%.2f", it)}" } ?: "-")
                InfoItem("고가", quote.highPrice?.let { "$${String.format("%.2f", it)}" } ?: "-")
                InfoItem("저가", quote.lowPrice?.let { "$${String.format("%.2f", it)}" } ?: "-")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("거래량", quote.volume?.let { "${it / 1_000_000}M" } ?: "-")
                InfoItem("MA50", quote.ma50?.let { "$${String.format("%.2f", it)}" } ?: "-")
                InfoItem("MA200", quote.ma200?.let { "$${String.format("%.2f", it)}" } ?: "-")
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PredictionCard(
    prediction: com.miyaong.invest.data.model.Prediction,
    selectedStrategy: com.miyaong.invest.data.model.InvestmentStrategy,
    onStrategyChange: (com.miyaong.invest.data.model.InvestmentStrategy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "매수 단가 예측",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 투자 전략 선택
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = selectedStrategy == com.miyaong.invest.data.model.InvestmentStrategy.SHORT_TERM,
                        onClick = { onStrategyChange(com.miyaong.invest.data.model.InvestmentStrategy.SHORT_TERM) },
                        label = { Text("단타") }
                    )
                    FilterChip(
                        selected = selectedStrategy == com.miyaong.invest.data.model.InvestmentStrategy.LONG_TERM,
                        onClick = { onStrategyChange(com.miyaong.invest.data.model.InvestmentStrategy.LONG_TERM) },
                        label = { Text("장기") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 목표 매수가
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "목표 매수가",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", prediction.targetPrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "신뢰도",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.0f", prediction.confidence * 100)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 추천 메시지
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = prediction.recommendation,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MacroDataCard(macroData: com.miyaong.invest.data.model.MacroData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "매크로 지표",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                macroData.usdKrw?.let { usdKrw ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "USD/KRW",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₩${String.format("%.2f", usdKrw)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                macroData.dollarIndex?.let { dollarIndex ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "달러 인덱스",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f", dollarIndex),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartRangeSelector(
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.displayName) }
            )
        }
    }
}

@Composable
fun StockChart(
    data: List<com.miyaong.invest.data.model.ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("차트 데이터 없음")
        }
        return
    }

    val prices = data.map { it.close }
    val maxPrice = prices.maxOrNull() ?: 0.0
    val minPrice = prices.minOrNull() ?: 0.0
    val priceRange = maxPrice - minPrice

    Canvas(modifier = modifier) {
        if (priceRange == 0.0 || data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val step = width / (data.size - 1).coerceAtLeast(1)

        // 차트 선 그리기
        for (i in 0 until data.size - 1) {
            val x1 = i * step
            val y1 = height - ((prices[i] - minPrice) / priceRange * height).toFloat()
            val x2 = (i + 1) * step
            val y2 = height - ((prices[i + 1] - minPrice) / priceRange * height).toFloat()

            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3f
            )
        }
    }
}
