package com.miyaong.invest.ui.detail

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ChartTab(
    symbol: String,
    modifier: Modifier = Modifier
) {
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
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body {
                    width: 100%;
                    height: 100%;
                    background-color: #121212;
                    overflow: hidden;
                }
                .tradingview-widget-container {
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                }
                #tradingview_widget {
                    width: 100%;
                    height: 100%;
                }
            </style>
        </head>
        <body>
            <div class="tradingview-widget-container">
                <div id="tradingview_widget"></div>
                <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                <script type="text/javascript">
                    new TradingView.widget({
                        "width": "100%",
                        "height": "100%",
                        "symbol": "$formattedSymbol",
                        "interval": "30",
                        "timezone": "Asia/Seoul",
                        "theme": "dark",
                        "style": "1",
                        "locale": "kr",
                        "toolbar_bg": "#1e1e1e",
                        "enable_publishing": false,
                        "hide_side_toolbar": false,
                        "allow_symbol_change": false,
                        "container_id": "tradingview_widget",
                        "hide_top_toolbar": false,
                        "save_image": false,
                        "studies": [
                            "RSI@tv-basicstudies"
                         ]
                    });
                </script>
            </div>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                loadDataWithBaseURL("https://www.tradingview.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}
