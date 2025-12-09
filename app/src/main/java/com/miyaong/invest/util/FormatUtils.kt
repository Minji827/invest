package com.miyaong.invest.util

import java.text.NumberFormat
import java.util.*

object FormatUtils {
    fun formatCurrency(value: Double, currencySymbol: String = "$"): String {
        return "$currencySymbol${String.format("%.2f", value)}"
    }

    fun formatPercentage(value: Double, includeSign: Boolean = true): String {
        val sign = if (includeSign && value > 0) "+" else ""
        return "$sign${String.format("%.2f", value)}%"
    }

    fun formatLargeNumber(number: Long): String {
        return when {
            number >= 1_000_000_000_000 -> String.format("%.2fT", number / 1_000_000_000_000.0)
            number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.2fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    fun formatVolume(volume: Long): String {
        return formatLargeNumber(volume)
    }

    fun formatMarketCap(marketCap: Long): String {
        return formatLargeNumber(marketCap)
    }

    fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000_000_000 -> String.format("%.1fT", number / 1_000_000_000_000.0)
            number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }
}
