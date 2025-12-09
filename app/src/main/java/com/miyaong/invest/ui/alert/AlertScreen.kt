package com.miyaong.invest.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miyaong.invest.data.local.PriceAlert
import com.miyaong.invest.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "🔔 가격 알림",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.activeAlerts.isEmpty() && uiState.triggeredAlerts.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextDim
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "설정된 알림이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDim
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ 버튼을 눌러 목표가 알림을 추가하세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDim
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Active Alerts Section
                    if (uiState.activeAlerts.isNotEmpty()) {
                        item {
                            Text(
                                text = "⏰ 대기 중인 알림",
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(uiState.activeAlerts) { alert ->
                            AlertCard(
                                alert = alert,
                                onDelete = { viewModel.deleteAlert(alert) }
                            )
                        }
                    }

                    // Triggered Alerts Section
                    if (uiState.triggeredAlerts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "✅ 도달한 알림",
                                style = MaterialTheme.typography.titleMedium,
                                color = Positive,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(uiState.triggeredAlerts) { alert ->
                            AlertCard(
                                alert = alert,
                                onDelete = { viewModel.deleteAlert(alert) },
                                isTriggered = true
                            )
                        }
                    }
                }
            }
        }

        // Add Alert Dialog
        if (uiState.showAddDialog) {
            AddAlertDialog(
                uiState = uiState,
                onDismiss = { viewModel.hideAddDialog() },
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onStockSelect = { viewModel.selectStock(it) },
                onConfirm = { targetPrice, isAbove ->
                    viewModel.addAlert(targetPrice, isAbove)
                }
            )
        }
    }
}

@Composable
fun AlertCard(
    alert: PriceAlert,
    onDelete: () -> Unit,
    isTriggered: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTriggered) Positive.copy(alpha = 0.1f) else SecondaryDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.stockName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = alert.ticker,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (alert.isAbove) "▲ 이상" else "▼ 이하",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (alert.isAbove) Positive else Negative
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format("%.2f", alert.targetPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Negative
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(
    uiState: AlertUiState,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStockSelect: (com.miyaong.invest.data.model.Stock) -> Unit,
    onConfirm: (targetPrice: Double, isAbove: Boolean) -> Unit
) {
    var targetPrice by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SecondaryDark,
        title = {
            Text(
                text = "목표가 알림 추가",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Stock Search Section
                if (uiState.selectedStock == null) {
                    Text(
                        text = "1. 주식 검색",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("주식명 또는 티커") },
                        placeholder = { Text("예: AAPL, Apple") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextDim
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Search Results
                    if (uiState.searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TertiaryDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            LazyColumn {
                                itemsIndexed(uiState.searchResults.take(5)) { index, stock ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onStockSelect(stock) },
                                        color = Color.Transparent
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = stock.symbol,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stock.shortName ?: stock.longName ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextDim
                                            )
                                        }
                                    }
                                    if (index < uiState.searchResults.take(5).size - 1) {
                                        HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Selected Stock Display
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = uiState.selectedStock.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.selectedStock.shortName ?: uiState.selectedStock.longName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "현재가: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDim
                                )
                                Text(
                                    text = "$${String.format("%.2f", uiState.selectedStock.currentPrice ?: 0.0)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. 목표가 설정",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = targetPrice,
                        onValueChange = { targetPrice = it },
                        label = { Text("목표가 ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextDim
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "3. 알림 조건",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Above/Below Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isAbove,
                            onClick = { isAbove = true },
                            label = { Text("이상일 때 ▲") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Positive.copy(alpha = 0.2f),
                                selectedLabelColor = Positive
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isAbove,
                            onClick = { isAbove = false },
                            label = { Text("이하일 때 ▼") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Negative.copy(alpha = 0.2f),
                                selectedLabelColor = Negative
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPrice.toDoubleOrNull() ?: 0.0
                    if (uiState.selectedStock != null && price > 0) {
                        onConfirm(price, isAbove)
                    }
                },
                enabled = uiState.selectedStock != null && targetPrice.toDoubleOrNull() != null && targetPrice.toDoubleOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("추가", color = PrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextDim)
            }
        }
    )
}
