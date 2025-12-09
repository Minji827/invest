package com.miyaong.invest.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miyaong.invest.ui.components.*
import com.miyaong.invest.ui.theme.*
import com.miyaong.invest.util.FormatUtils
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    ticker: String,
    onNavigateBack: () -> Unit,
    viewModel: StockDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stock = uiState.stock

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ticker) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "즐겨찾기",
                            tint = if (uiState.isFavorite) AccentRed else TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecondaryDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryDark)
                .padding(paddingValues)
        ) {
            // Header Section
            item {
                StockDetailHeader(
                    name = stock?.shortName ?: stock?.longName ?: ticker,
                    symbol = ticker,
                    exchange = stock?.exchange ?: "",
                    sector = stock?.sector ?: "",
                    currentPrice = stock?.currentPrice ?: 0.0,
                    changeAmount = stock?.changeAmount ?: 0.0,
                    changePercent = stock?.changePercent ?: 0.0,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            }

            // Tab Navigation
            item {
                TabNavigation(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tab Content
            item {
                when (uiState.selectedTab) {
                    0 -> ChartTab(
                        history = uiState.history,
                        indicators = uiState.indicators,
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) },
                        isLoading = uiState.isLoading
                    )
                    1 -> FinancialTab(
                        financials = uiState.financials,
                        selectedType = uiState.selectedFinancialType,
                        onTypeSelected = { viewModel.selectFinancialType(it) },
                        isLoading = uiState.isLoading
                    )
                    2 -> MetricsTab(
                        metrics = uiState.metrics,
                        isLoading = uiState.isLoading
                    )
                    3 -> DividendTab(
                        dividend = uiState.dividend,
                        isLoading = uiState.isLoading
                    )
                    4 -> PredictionTab(
                        prediction = uiState.prediction,
                        onLoadPrediction = { viewModel.loadPredictionData(it) },
                        isLoading = uiState.isLoading
                    )
                }
            }

            // Error Message
            if (uiState.error != null) {
                item {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadStockData() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StockDetailHeader(
    name: String,
    symbol: String,
    exchange: String,
    sector: String,
    currentPrice: Double,
    changeAmount: Double,
    changePercent: Double,
    lastUpdatedTimestamp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1a237e),
                        Color(0xFF283593),
                        Color(0xFF1a237e)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$exchange: $symbol · $sector",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "₩${String.format("%,.0f", currentPrice)}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                val changeColor = if (changePercent >= 0) Positive else Negative
                Box(
                    modifier = Modifier
                        .background(changeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${if (changeAmount >= 0) "+" else ""}${String.format("%,.0f", changeAmount)} (${if (changePercent >= 0) "+" else ""}${String.format("%.2f", changePercent)}%)",
                        style = MaterialTheme.typography.titleLarge,
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "마지막 업데이트: ${java.text.SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREAN).format(Date(lastUpdatedTimestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TabNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("📈 차트", "📊 재무제표", "📐 투자지표", "💰 배당정보", "🔮 AI 예측")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedTab
                Button(
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) TertiaryDark else Color.Transparent,
                        contentColor = if (isSelected) AccentCyan else TextSecondary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

