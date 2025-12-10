package com.miyaong.invest.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miyaong.invest.ui.alert.AlertScreen
import com.miyaong.invest.ui.components.*
import com.miyaong.invest.ui.theme.*

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onStockClick: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val alertViewModel: com.miyaong.invest.ui.alert.AlertViewModel = hiltViewModel()

    Scaffold(
        containerColor = PrimaryDark,
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { alertViewModel.showAddDialog() },
                    containerColor = AccentCyan,
                    contentColor = PrimaryDark
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "알림 추가"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SecondaryDark,
                contentColor = AccentCyan
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
                    label = { Text("홈") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextDim,
                        unselectedTextColor = TextDim,
                        indicatorColor = AccentBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "분석") },
                    label = { Text("분석") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextDim,
                        unselectedTextColor = TextDim,
                        indicatorColor = AccentBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "알림") },
                    label = { Text("알림") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextDim,
                        unselectedTextColor = TextDim,
                        indicatorColor = AccentBlue.copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeContent(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchStocks(it)
                },
                onStockClick = onStockClick,
                viewModel = viewModel,
                paddingValues = paddingValues
            )
            1 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("시장분석 화면 (준비 중)", color = TextDim)
                }
            }
            2 -> AlertScreen(
                modifier = Modifier.padding(paddingValues),
                viewModel = alertViewModel
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: MainUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStockClick: (String, String) -> Unit,
    viewModel: MainViewModel,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Header(
            searchQuery = searchQuery,
            onQueryChange = onSearchQueryChange
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
                // Search Results
                if (searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SecondaryDark
                            )
                        ) {
                            Column {
                                uiState.searchResults.take(5).forEach { stock ->
                                    StockRow(
                                        symbol = stock.symbol,
                                        name = stock.shortName ?: stock.longName ?: stock.symbol,
                                        currentPrice = stock.currentPrice ?: 0.0,
                                        changePercent = stock.changePercent ?: 0.0,
                                        volume = stock.volume ?: 0L,
                                        marketCap = stock.marketCap ?: 0L,
                                        onClick = { onStockClick(stock.symbol, stock.shortName ?: stock.longName ?: stock.symbol) }
                                    )
                                    if (stock != uiState.searchResults.last()) {
                                        HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Message
                if (uiState.error != null) {
                    item {
                        ErrorMessage(
                            message = uiState.error!!,
                            onRetry = { viewModel.loadData() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Macro Indicators Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "실시간 글로벌 지표")
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    when {
                        uiState.isLoading && uiState.macroIndicators.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AccentCyan)
                            }
                        }
                        !uiState.isLoading && uiState.macroIndicators.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SecondaryDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📊", style = MaterialTheme.typography.headlineMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "글로벌 지표를 불러올 수 없습니다",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextDim
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { viewModel.loadData() }) {
                                            Text("다시 시도", color = AccentCyan)
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.macroIndicators) { indicator ->
                                    MacroCard(
                                        label = indicator.name,
                                        value = "${indicator.unit}${String.format("%.2f", indicator.value)}",
                                        change = indicator.changePercent,
                                        changeAmount = "${indicator.unit}${String.format("%.2f", indicator.changeAmount)}",
                                        icon = when (indicator.type) {
                                            "USD/KRW" -> "💱"
                                            "EUR/KRW" -> "💶"
                                            "JPY/KRW" -> "💴"
                                            "DXY" -> "📊"
                                            else -> "📈"
                                        },
                                        modifier = Modifier.width(280.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Trending Stocks Section with Tabs
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader(title = "🔥 실시간 인기 종목")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tab Row for 3 categories
                        val tabTitles = listOf("📊 거래량", "📈 상승률", "⚡ 변동률")
                        TabRow(
                            selectedTabIndex = uiState.selectedTrendingTab,
                            containerColor = SecondaryDark,
                            contentColor = AccentCyan
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = uiState.selectedTrendingTab == index,
                                    onClick = { viewModel.selectTrendingTab(index) },
                                    text = { 
                                        Text(
                                            text = title, 
                                            fontWeight = if (uiState.selectedTrendingTab == index) 
                                                FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    if (uiState.isLoading && uiState.trendingStocks == null) {
                        LoadingIndicator()
                    } else {
                        val currentStocks = when (uiState.selectedTrendingTab) {
                            0 -> uiState.trendingStocks?.mostActive ?: emptyList()
                            1 -> uiState.trendingStocks?.topGainers ?: emptyList()
                            2 -> uiState.trendingStocks?.mostVolatile ?: emptyList()
                            else -> emptyList()
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SecondaryDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TertiaryDark)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "종목",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextDim,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                    )
                                    Text(
                                        text = "현재가",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextDim,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "변동률",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextDim,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(70.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                HorizontalDivider(color = BorderColor)

                                // Stock Rows
                                if (currentStocks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "데이터를 불러오는 중...",
                                            color = TextDim
                                        )
                                    }
                                } else {
                                    currentStocks.take(10).forEachIndexed { index, stock ->
                                        StockRow(
                                            symbol = stock.symbol,
                                            name = stock.shortName ?: stock.longName ?: stock.symbol,
                                            currentPrice = stock.currentPrice ?: 0.0,
                                            changePercent = stock.changePercent ?: 0.0,
                                            volume = stock.volume ?: 0L,
                                            marketCap = stock.marketCap ?: 0L,
                                            onClick = { onStockClick(stock.symbol, stock.shortName ?: stock.longName ?: stock.symbol) }
                                        )
                                        if (index < currentStocks.size - 1 && index < 9) {
                                            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
private fun Header(
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryDark)
            .padding(16.dp)
    ) {
        Text(
            text = "◆ STOCK PULSE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )
        Spacer(Modifier.height(16.dp))
        SearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange
        )
    }
}

@Composable
private fun NavButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentBlue.copy(alpha = 0.1f),
            contentColor = AccentCyan
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = "기업명 또는 티커 검색 (예: AAPL, TSLA...)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "검색",
                tint = TextDim
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TertiaryDark,
            unfocusedContainerColor = TertiaryDark.copy(alpha = 0.6f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentCyan,
            focusedIndicatorColor = AccentCyan,
            unfocusedIndicatorColor = BorderColor,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AccentCyan, AccentBlue)
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
