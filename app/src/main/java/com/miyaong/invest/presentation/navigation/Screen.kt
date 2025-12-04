package com.miyaong.invest.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Detail : Screen("detail/{symbol}") {
        fun createRoute(symbol: String) = "detail/$symbol"
    }
    data object Settings : Screen("settings")
}
