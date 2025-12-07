package com.miyaong.invest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StockPulseDarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = PrimaryDark,
    primaryContainer = SecondaryDark,
    onPrimaryContainer = TextPrimary,

    secondary = AccentBlue,
    onSecondary = PrimaryDark,
    secondaryContainer = TertiaryDark,
    onSecondaryContainer = TextSecondary,

    tertiary = AccentPurple,
    onTertiary = PrimaryDark,
    tertiaryContainer = CardBackground,
    onTertiaryContainer = TextPrimary,

    error = AccentRed,
    onError = PrimaryDark,
    errorContainer = Color(0xFF601410),
    onErrorContainer = AccentRed,

    background = PrimaryDark,
    onBackground = TextPrimary,

    surface = SecondaryDark,
    onSurface = TextPrimary,
    surfaceVariant = TertiaryDark,
    onSurfaceVariant = TextSecondary,

    outline = BorderColor,
    outlineVariant = Color(0xFF1e2644),

    inverseSurface = TextPrimary,
    inverseOnSurface = PrimaryDark,
    inversePrimary = AccentCyan,

    surfaceTint = AccentCyan
)

@Composable
fun InvestTheme(
    darkTheme: Boolean = true, // Always use dark theme for stock app
    content: @Composable () -> Unit
) {
    val colorScheme = StockPulseDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
