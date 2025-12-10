package com.miyaong.invest.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import com.miyaong.invest.data.model.*
import com.miyaong.invest.ui.components.*
import com.miyaong.invest.ui.theme.*
import com.miyaong.invest.util.FormatUtils

import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.marker.*
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.*
import com.patrykandpatrick.vico.compose.common.shader.*
import com.patrykandpatrick.vico.core.cartesian.data.*
import com.patrykandpatrick.vico.core.cartesian.marker.*
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.shape.Shape

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext

@Composable
fun ChartTab(
    symbol: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // TradingView Ticker Formatting
    val formattedSymbol = remember(symbol) {
        when {
            symbol.endsWith(".KS") -> "KRX:${symbol.removeSuffix(".KS")}"
            symbol.endsWith(".KQ") -> "KOSDAQ:${symbol.removeSuffix(".KQ")}"
            else -> symbol // Default (mostly US)
        }
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body { margin: 0; padding: 0; background-color: #121212; height: 100vh; }
                .tradingview-widget-container { height: 100%; width: 100%; }
            </style>
        </head>
        <body>
            <div class="tradingview-widget-container">
                <div id="tradingview_widget"></div>
                <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                <script type="text/javascript">
                    new TradingView.widget({
                        "autosize": true,
                        "symbol": "$formattedSymbol",
                        "interval": "D",
                        "timezone": "Asia/Seoul",
                        "theme": "dark",
                        "style": "1",
                        "locale": "kr",
                        "toolbar_bg": "#f1f3f6",
                        "enable_publishing": false,
                        "hide_side_toolbar": false,
                        "allow_symbol_change": false,
                        "container_id": "tradingview_widget",
                        "studies": [
                            "MASimple@tv-basicstudies",
                            "RSI@tv-basicstudies"
                         ]
                    });
                </script>
            </div>
        </body>
        </html>
    """.trimIndent()

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = androidx.compose.ui.graphics.RectangleShape
    ) {
        AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = false
                    settings.useWideViewPort = false
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                    settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    loadDataWithBaseURL("https://www.tradingview.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                // Optimize: prevent reloading if symbol hasn't changed? 
                // For now, simple loading is fine.
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FinancialTab(
    financials: List<FinancialStatement>,
    balanceSheet: List<BalanceSheet>,
    cashFlow: List<CashFlow>,
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("손익계산서") }

    if (isLoading) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category & Type Selectors
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PeriodSelector(
                    periods = listOf("손익계산서", "재무상태표", "현금흐름표"),
                    selectedPeriod = selectedCategory,
                    onPeriodSelected = { selectedCategory = it }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    PeriodSelector(
                        periods = listOf("연간", "분기"),
                        selectedPeriod = if (selectedType == "annual") "연간" else "분기",
                        onPeriodSelected = { onTypeSelected(if (it == "연간") "annual" else "quarterly") }
                    )
                }
            }
            
            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
            
            // Content
            when (selectedCategory) {
                "손익계산서" -> {
                    if (financials.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("손익계산서 데이터가 없습니다.", color = TextDim)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            FinancialHeaderRow(financials.map { it.period })
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.1f))

                            FinancialRow("매출액", financials.map { it.revenue })
                            FinancialRow("매출원가", financials.map { it.costOfRevenue })
                            FinancialRow("매출총이익", financials.map { it.grossProfit }, highlight = true)
                            FinancialRow("영업비용", financials.map { it.operatingExpense })
                            FinancialRow("영업이익", financials.map { it.operatingIncome }, highlight = true)
                            FinancialRow("당기순이익", financials.map { it.netIncome }, highlight = true, primary = true)
                        }
                    }
                }
                "재무상태표" -> {
                    if (balanceSheet.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("재무상태표 데이터가 없습니다.", color = TextDim)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            FinancialHeaderRow(balanceSheet.map { it.period })
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.1f))

                            FinancialRow("자산총계", balanceSheet.map { it.totalAssets }, highlight = true)
                            FinancialRow("부채총계", balanceSheet.map { it.totalLiabilities })
                            FinancialRow("자본총계", balanceSheet.map { it.totalEquity }, highlight = true)
                            FinancialRow("총차입금", balanceSheet.map { it.totalDebt })
                        }
                    }
                }
                "현금흐름표" -> {
                    if (cashFlow.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("현금흐름표 데이터가 없습니다.", color = TextDim)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            FinancialHeaderRow(cashFlow.map { it.period })
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.1f))

                            FinancialRow("영업활동 현금흐름", cashFlow.map { it.operatingCashFlow }, highlight = true)
                            FinancialRow("투자활동 현금흐름", cashFlow.map { it.investingCashFlow })
                            FinancialRow("재무활동 현금흐름", cashFlow.map { it.financingCashFlow })
                            FinancialRow("잉여현금흐름 (FCF)", cashFlow.map { it.freeCashFlow }, highlight = true, primary = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialRow(
    label: String,
    values: List<Long>,
    highlight: Boolean = false,
    primary: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlight) AccentCyan.copy(alpha = 0.05f) else Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (primary) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(2f)
        )
        values.take(4).forEach { value ->
            Text(
                "₩${FormatUtils.formatNumber(value)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (highlight) AccentCyan else TextSecondary,
                fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
}

@Composable
fun FinancialHeaderRow(periods: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryDark)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(2f)) // Align with label column
        periods.take(4).forEach { period ->
            Text(
                period,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricsTab(
    metrics: InvestmentMetrics?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingIndicator()
    } else if (metrics == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("투자지표 데이터 없음", color = TextDim)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    "밸류에이션 지표",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "PER (주가수익비율)",
                        value = String.format("%.2f", metrics.per),
                        comparison = "업종 평균 대비",
                        badge = "평균 이상",
                        badgeColor = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "PBR (주가순자산비율)",
                        value = String.format("%.2f", metrics.pbr),
                        comparison = "업종 평균 대비",
                        badge = "프리미엄",
                        badgeColor = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "PSR (주가매출비율)",
                        value = String.format("%.2f", metrics.psr),
                        badge = "양호",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "EV/EBITDA",
                        value = String.format("%.2f", metrics.evEbitda),
                        badge = "양호",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "수익성 지표",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "ROE (자기자본이익률)",
                        value = String.format("%.1f%%", metrics.roe * 100),
                        comparison = "매우 높은 수준",
                        badge = "매우 우수",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "ROA (총자산이익률)",
                        value = String.format("%.1f%%", metrics.roa * 100),
                        comparison = "업종 최상위",
                        badge = "우수",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        label = "영업이익률",
                        value = String.format("%.1f%%", metrics.operatingMargin * 100),
                        badge = "우수",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "순이익률",
                        value = String.format("%.1f%%", metrics.netMargin * 100),
                        badge = "우수",
                        badgeColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DividendTab(
    dividend: DividendInfo?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingIndicator()
    } else if (dividend == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("배당정보 데이터 없음", color = TextDim)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    label = "배당 수익률",
                    value = String.format("%.2f%%", dividend.dividendYield),
                    comparison = "연간 배당금: ₩${String.format("%,.0f", dividend.annualDividend)}",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "배당 성향",
                    value = String.format("%.1f%%", dividend.payoutRatio),
                    comparison = "안정적 배당 가능",
                    badge = "안정적",
                    badgeColor = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    label = "배당 성장률 (5년)",
                    value = String.format("%.1f%%", dividend.dividendGrowth5Year),
                    comparison = "연평균 증가율",
                    badge = "성장중",
                    badgeColor = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "배당 지속 기간",
                    value = "${dividend.consecutiveYears}년",
                    comparison = "연속 배당 지급",
                    badge = "우수",
                    badgeColor = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PredictionTab(
    prediction: PredictionResult?,
    onLoadPrediction: (Int) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", style = MaterialTheme.typography.headlineMedium)
                Column {
                    Text(
                        "투자 주의사항",
                        style = MaterialTheme.typography.titleSmall,
                        color = AccentOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "AI 예측 모델은 과거 데이터를 기반으로 한 참고 자료이며, 실제 주가는 예측과 다를 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        PeriodSelector(
            periods = listOf("7일 예측", "14일 예측", "30일 예측"),
            selectedPeriod = "7일 예측",
            onPeriodSelected = {
                val days = when (it) {
                    "7일 예측" -> 7
                    "14일 예측" -> 14
                    "30일 예측" -> 30
                    else -> 7
                }
                onLoadPrediction(days)
            }
        )

        if (isLoading) {
            LoadingIndicator()
        } else if (prediction == null) {
            Button(
                onClick = { onLoadPrediction(7) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("예측 시작")
            }
        } else {
            // Model Performance
            Text(
                "🤖 AI 모델 성능 비교",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(prediction.performances) { performance ->
                    ModelPerformanceCard(performance)
                }
            }

            // Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SecondaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "📊 예측 요약",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )

                    SummaryRow("현재가", "₩${String.format("%,.0f", prediction.summary.currentPrice)}")
                    SummaryRow(
                        "예상가 (${prediction.summary.bestModel})",
                        "₩${String.format("%,.0f", prediction.summary.predictedPrice)}",
                        color = Positive
                    )
                    SummaryRow(
                        "예상 변동률",
                        "${if (prediction.summary.expectedChange >= 0) "+" else ""}${String.format("%.2f", prediction.summary.expectedChange)}%",
                        color = if (prediction.summary.expectedChange >= 0) Positive else Negative
                    )
                    SummaryRow("신뢰도", prediction.summary.confidence, color = AccentCyan)
                }
            }
        }
    }
}

@Composable
fun ModelPerformanceCard(performance: ModelPerformance) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                performance.modelName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            SummaryRow("RMSE", String.format("%.2f", performance.rmse))
            SummaryRow("MAE", String.format("%.2f", performance.mae))
            SummaryRow("R² Score", String.format("%.2f", performance.r2Score))
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextDim
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
