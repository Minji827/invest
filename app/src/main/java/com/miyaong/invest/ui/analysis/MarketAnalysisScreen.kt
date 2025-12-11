package com.miyaong.invest.ui.analysis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miyaong.invest.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import com.miyaong.invest.data.model.VolatilityWatchData
import com.miyaong.invest.data.model.VolatileStock


@Composable
fun MarketAnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: MarketAnalysisViewModel = hiltViewModel(),
    onStockClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 헤더
        item {
            Text(
                text = "시장 분석",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // LULD 임박 종목 섹션
        item {
            VolatilityWatchSection(
                volatilityWatch = uiState.volatilityWatch,
                isLoading = uiState.isLoading,
                onStockClick = onStockClick
            )
        }

        // 실시간 거래 정지 목록 섹션
        item {
            TradingHaltsSection(
                tradingHalts = uiState.tradingHalts,
                isLoading = uiState.isLoading,
                onStockClick = onStockClick
            )
        }

        // 매수단가 추천 섹션
        item {
            BuyRecommendationSection(
                ticker = uiState.buyTicker,
                onTickerChange = { viewModel.setBuyTicker(it) },
                onAnalyze = { viewModel.getBuyRecommendation() },
                recommendation = uiState.buyRecommendation,
                isLoading = uiState.isBuyLoading,
                error = uiState.error,
                onStockClick = onStockClick
            )
        }


    }
}

@Composable
private fun VolatilityWatchSection(
    volatilityWatch: VolatilityWatchData?,
    isLoading: Boolean,
    onStockClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📈", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "LULD 임박 종목",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "최근 5분간 변동성이 큰 종목을 감지합니다.",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && volatilityWatch == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentCyan)
                }
            } else if (volatilityWatch != null && (volatilityWatch.upwardWatch.isNotEmpty() || volatilityWatch.downwardWatch.isNotEmpty())) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 상승 주의
                    VolatilityList(
                        title = "상승 주의 ⬆️",
                        stocks = volatilityWatch.upwardWatch,
                        color = Positive,
                        onStockClick = onStockClick
                    )
                    // 하락 주의
                    VolatilityList(
                        title = "하락 주의 ⬇️",
                        stocks = volatilityWatch.downwardWatch,
                        color = Negative,
                        onStockClick = onStockClick
                    )
                }
            } else {
                Text(
                    text = "변동성 임박 종목이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun VolatilityList(
    title: String,
    stocks: List<VolatileStock>,
    color: Color,
    onStockClick: (String, String) -> Unit
) {
    if (stocks.isNotEmpty()) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stocks.forEach { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.1f))
                            .clickable { onStockClick(stock.symbol, stock.name) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stock.symbol, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = stock.name.take(20) + if (stock.name.length > 20) "..." else "", style = MaterialTheme.typography.bodySmall, color = TextDim)
                        }
                        Text(
                            text = "${if(stock.changePercent > 0) "+" else ""}${String.format("%.2f", stock.changePercent)}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyRecommendationSection(
    ticker: String,
    onTickerChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    recommendation: com.miyaong.invest.data.model.BuyRecommendation?,
    isLoading: Boolean,
    error: String?,
    onStockClick: (String, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "AI 매수단가 추천",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "머신러닝 기반 최적 매수가 분석",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 티커 입력
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = onTickerChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("티커 입력 (예: AAPL)", color = TextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onAnalyze()
                        }
                    )
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onAnalyze()
                    },
                    enabled = !isLoading && ticker.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = PrimaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("분석")
                    }
                }
            }

            // 에러 표시
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1F1F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B)
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }

            // 결과 표시
            if (recommendation != null) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(16.dp))

                // 현재가 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${recommendation.ticker} 현재가",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim
                    )
                    Text(
                        text = "$${String.format("%.2f", recommendation.currentPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 매수가 추천 카드들
                BuyPriceCard(
                    label = "공격적 매수",
                    icon = "🔥",
                    price = recommendation.recommendations.aggressive.price,
                    discount = recommendation.recommendations.aggressive.discount,
                    reason = recommendation.recommendations.aggressive.reason,
                    color = Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                BuyPriceCard(
                    label = "적정 매수",
                    icon = "✅",
                    price = recommendation.recommendations.moderate.price,
                    discount = recommendation.recommendations.moderate.discount,
                    reason = recommendation.recommendations.moderate.reason,
                    color = Positive
                )

                Spacer(modifier = Modifier.height(12.dp))

                BuyPriceCard(
                    label = "안전 매수",
                    icon = "🛡️",
                    price = recommendation.recommendations.conservative.price,
                    discount = recommendation.recommendations.conservative.discount,
                    reason = recommendation.recommendations.conservative.reason,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // 분석 근거
                Text(
                    text = "분석 근거",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val analysis = recommendation.analysis
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnalysisRow("RSI", "${analysis.rsi} (${analysis.rsiStatus})")
                    AnalysisRow("볼린저 하단", "$${String.format("%.2f", analysis.bollingerLower)}")
                    AnalysisRow("52주 최저", "$${String.format("%.2f", analysis.low52Week)}")
                    AnalysisRow("지지선", "$${String.format("%.2f", analysis.nearestSupport)}")
                    AnalysisRow("변동성", "${String.format("%.2f", analysis.volatility)}%")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ML 신뢰도
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ML 신뢰도: ${String.format("%.1f", recommendation.mlConfidence)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (recommendation.mlConfidence > 50) Positive else TextDim
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 상세보기 버튼
                OutlinedButton(
                    onClick = { onStockClick(recommendation.ticker, recommendation.ticker) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(listOf(AccentCyan, AccentBlue))
                    )
                ) {
                    Text("${recommendation.ticker} 상세 차트 보기")
                }
            }
        }
    }
}

@Composable
private fun BuyPriceCard(
    label: String,
    icon: String,
    price: Double,
    discount: Double,
    reason: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleLarge)
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "-${String.format("%.1f", discount)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun AnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "• $label",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary
        )
    }
}



@Composable
private fun TradingHaltsSection(
    tradingHalts: com.miyaong.invest.data.model.TradingHaltsData?,
    isLoading: Boolean,
    onStockClick: (String, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🚨", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "실시간 거래 정지 종목",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentCyan)
                }
            } else if (tradingHalts != null && tradingHalts.halts.isNotEmpty()) {
                val itemsToShow = if (isExpanded) tradingHalts.halts else tradingHalts.halts.take(5)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsToShow.forEach { halt ->
                        HaltItem(halt = halt, onStockClick = onStockClick)
                    }
                }

                if (tradingHalts.totalCount > 5) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isExpanded) "간략히 보기" else "더보기 (${tradingHalts.totalCount - 5}개 더 있음)",
                            color = AccentCyan
                        )
                    }
                }
            } else {
                Text(
                    text = "현재 거래 정지된 종목이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HaltItem(
    halt: com.miyaong.invest.data.model.TradingHalt,
    onStockClick: (String, String) -> Unit
) {
    val (icon, label, color) = getHaltTypeAttributes(halt.haltType)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onStockClick(halt.symbol, halt.name) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TertiaryDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
                Column {
                    Text(
                        text = halt.symbol,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = halt.name.take(20) + if (halt.name.length > 20) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = halt.haltTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
        }
    }
}

@Composable
private fun getHaltTypeAttributes(type: String): Triple<String, String, Color> {
    return when (type) {
        "upper" -> Triple("⏫", "상한가", Positive)
        "lower" -> Triple("⏬", "하한가", Negative)
        "luld" -> Triple("⏸️", "LULD", Color.Yellow)
        "news" -> Triple("📰", "뉴스 대기", AccentBlue)
        "volatility" -> Triple("⚡", "변동성 완화", Color(0xFFFFB300))
        else -> Triple("⚠️", "기타", TextDim)
    }
}
