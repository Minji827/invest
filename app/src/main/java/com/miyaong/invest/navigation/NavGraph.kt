package com.miyaong.invest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.miyaong.invest.ui.detail.StockDetailScreen
import com.miyaong.invest.ui.main.MainScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object StockDetail : Screen("stock_detail/{ticker}") {
        fun createRoute(ticker: String) = "stock_detail/$ticker"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onStockClick = { ticker ->
                    navController.navigate(Screen.StockDetail.createRoute(ticker))
                }
            )
        }

        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(
                navArgument("ticker") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker") ?: return@composable
            StockDetailScreen(
                ticker = ticker,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
