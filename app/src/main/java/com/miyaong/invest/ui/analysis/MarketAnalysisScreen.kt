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

        // 서킷브레이커 확률 섹션
        item {
            CircuitBreakerSection(
                circuitBreaker = uiState.circuitBreaker,
                isLoading = uiState.isLoading
            )
        }

        // 주가 예측 섹션
        item {
            PredictionSection(
                ticker = uiState.predictionTicker,
                onTickerChange = { viewModel.setPredictionTicker(it) },
                onPredict = { viewModel.predictStock() },
                prediction = uiState.prediction,
                isPredicting = uiState.isPredicting,
                error = uiState.error,
                onStockClick = onStockClick
            )
        }
    }
}

@Composable
private fun CircuitBreakerSection(
    circuitBreaker: com.miyaong.invest.data.model.CircuitBreakerData?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AccentCyan
                )
                Text(
                    text = "AMEX 서킷브레이커 확률",
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
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentCyan)
                }
            } else {
                // 서킷브레이커 확률 게이지
                val probability = circuitBreaker?.probability ?: 0.0
                val animatedProgress by animateFloatAsState(
                    targetValue = (probability / 100f).toFloat(),
                    animationSpec = tween(1000),
                    label = "progress"
                )

                // 확률에 따른 색상
                val gaugeColor = when {
                    probability < 20 -> Positive
                    probability < 50 -> Color(0xFFFFB300) // 주황
                    else -> Negative
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 큰 확률 표시
                    Text(
                        text = "${String.format("%.1f", probability)}%",
                        style = MaterialTheme.typography.displaySmall,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 프로그레스 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(TertiaryDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(gaugeColor.copy(alpha = 0.7f), gaugeColor)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 서킷브레이커 단계 설명
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircuitLevel(level = 1, threshold = "-7%", isActive = (circuitBreaker?.currentLevel ?: 0) >= 1)
                        CircuitLevel(level = 2, threshold = "-13%", isActive = (circuitBreaker?.currentLevel ?: 0) >= 2)
                        CircuitLevel(level = 3, threshold = "-20%", isActive = (circuitBreaker?.currentLevel ?: 0) >= 3)
                    }

                    if (circuitBreaker != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("현재 지수", style = MaterialTheme.typography.bodySmall, color = TextDim)
                                Text(
                                    String.format("%.2f", circuitBreaker.indexValue),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("변동률", style = MaterialTheme.typography.bodySmall, color = TextDim)
                                val changeColor = if (circuitBreaker.indexChange >= 0) Positive else Negative
                                Text(
                                    "${if (circuitBreaker.indexChange >= 0) "+" else ""}${String.format("%.2f", circuitBreaker.indexChange)}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = changeColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "업데이트: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(circuitBreaker.timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircuitLevel(
    level: Int,
    threshold: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isActive) Negative.copy(alpha = 0.8f) else TertiaryDark
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$level",
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) Color.White else TextDim,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = threshold,
            style = MaterialTheme.typography.bodySmall,
            color = TextDim
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PredictionSection(
    ticker: String,
    onTickerChange: (String) -> Unit,
    onPredict: () -> Unit,
    prediction: com.miyaong.invest.data.model.PredictionResult?,
    isPredicting: Boolean,
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
                text = "AI 주가 예측",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
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
                            onPredict()
                        }
                    )
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onPredict()
                    },
                    enabled = !isPredicting && ticker.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = PrimaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isPredicting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "예측")
                    }
                }
            }

            // 에러 메시지
            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Negative
                )
            }

            // 예측 결과
            if (prediction != null) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(16.dp))

                // 요약 정보
                val summary = prediction.summary
                val expectedChangeColor = if (summary.expectedChange >= 0) Positive else Negative

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("현재가", style = MaterialTheme.typography.bodySmall, color = TextDim)
                        Text(
                            "$${String.format("%.2f", summary.currentPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("예상 변동", style = MaterialTheme.typography.bodySmall, color = TextDim)
                        Text(
                            "${if (summary.expectedChange >= 0) "+" else ""}${String.format("%.2f", summary.expectedChange)}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = expectedChangeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("예측가", style = MaterialTheme.typography.bodySmall, color = TextDim)
                        Text(
                            "$${String.format("%.2f", summary.predictedPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 모델별 성능
                Text(
                    text = "모델별 성능 (R² Score)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                prediction.performances.forEach { perf ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = perf.modelName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = String.format("%.3f", perf.r2Score),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (perf.r2Score > 0.7) Positive else TextDim,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 신뢰도 & 최적 모델
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "신뢰도: ${summary.confidence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Text(
                        text = "최적 모델: ${summary.bestModel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 상세보기 버튼
                OutlinedButton(
                    onClick = { onStockClick(ticker, ticker) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(listOf(AccentCyan, AccentBlue))
                    )
                ) {
                    Text("${ticker} 상세 차트 보기")
                }
            }
        }
    }
}
