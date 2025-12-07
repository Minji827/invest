package com.miyaong.invest.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("shortName")
    val shortName: String? = null,

    @SerialName("longName")
    val longName: String? = null,

    @SerialName("exchange")
    val exchange: String? = null,

    @SerialName("currency")
    val currency: String? = null,

    @SerialName("marketCap")
    val marketCap: Long? = null,

    @SerialName("currentPrice")
    val currentPrice: Double? = null,

    @SerialName("changePercent")
    val changePercent: Double? = null,

    @SerialName("changeAmount")
    val changeAmount: Double? = null,

    @SerialName("sector")
    val sector: String? = null,

    @SerialName("volume")
    val volume: Long? = null,

    @SerialName("high52Week")
    val high52Week: Double? = null,

    @SerialName("low52Week")
    val low52Week: Double? = null
)

// 3가지 카테고리 인기 종목 응답
@Serializable
data class TrendingStocksData(
    @SerialName("mostActive")
    val mostActive: List<Stock> = emptyList(),
    
    @SerialName("topGainers")
    val topGainers: List<Stock> = emptyList(),
    
    @SerialName("mostVolatile")
    val mostVolatile: List<Stock> = emptyList()
)

@Serializable
data class StockHistory(
    @SerialName("date")
    val date: String,
    @SerialName("open")
    val open: Double,
    @SerialName("high")
    val high: Double,
    @SerialName("low")
    val low: Double,
    @SerialName("close")
    val close: Double,
    @SerialName("volume")
    val volume: Long
)

@Serializable
data class TechnicalIndicator(
    @SerialName("date")
    val date: String,
    @SerialName("ma5")
    val ma5: Double? = null,
    @SerialName("ma20")
    val ma20: Double? = null,
    @SerialName("ma60")
    val ma60: Double? = null,
    @SerialName("ma120")
    val ma120: Double? = null,
    @SerialName("rsi")
    val rsi: Double? = null,
    @SerialName("macd")
    val macd: Double? = null,
    @SerialName("macdSignal")
    val macdSignal: Double? = null,
    @SerialName("bollingerUpper")
    val bollingerUpper: Double? = null,
    @SerialName("bollingerMiddle")
    val bollingerMiddle: Double? = null,
    @SerialName("bollingerLower")
    val bollingerLower: Double? = null
)

@Serializable
data class MacroIndicator(
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: Double,
    @SerialName("changePercent")
    val changePercent: Double,
    @SerialName("changeAmount")
    val changeAmount: Double,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("unit")
    val unit: String = ""
)

@Serializable
data class FinancialStatement(
    @SerialName("period")
    val period: String,
    @SerialName("revenue")
    val revenue: Long,
    @SerialName("costOfRevenue")
    val costOfRevenue: Long,
    @SerialName("grossProfit")
    val grossProfit: Long,
    @SerialName("operatingExpense")
    val operatingExpense: Long,
    @SerialName("operatingIncome")
    val operatingIncome: Long,
    @SerialName("netIncome")
    val netIncome: Long
)

@Serializable
data class InvestmentMetrics(
    @SerialName("per")
    val per: Double,
    @SerialName("pbr")
    val pbr: Double,
    @SerialName("psr")
    val psr: Double,
    @SerialName("evEbitda")
    val evEbitda: Double,
    @SerialName("roe")
    val roe: Double,
    @SerialName("roa")
    val roa: Double,
    @SerialName("operatingMargin")
    val operatingMargin: Double,
    @SerialName("netMargin")
    val netMargin: Double,
    @SerialName("debtRatio")
    val debtRatio: Double,
    @SerialName("currentRatio")
    val currentRatio: Double,
    @SerialName("quickRatio")
    val quickRatio: Double
)

@Serializable
data class DividendInfo(
    @SerialName("dividendYield")
    val dividendYield: Double,
    @SerialName("annualDividend")
    val annualDividend: Double,
    @SerialName("payoutRatio")
    val payoutRatio: Double,
    @SerialName("dividendGrowth5Year")
    val dividendGrowth5Year: Double,
    @SerialName("consecutiveYears")
    val consecutiveYears: Int
)

@Serializable
data class ChartPredictionData(
    @SerialName("date")
    val date: String,
    @SerialName("actualPrice")
    val actualPrice: Double?,
    @SerialName("linearRegression")
    val linearRegression: Double?,
    @SerialName("randomForest")
    val randomForest: Double?,
    @SerialName("svm")
    val svm: Double?
)

@Serializable
data class ModelPerformance(
    @SerialName("modelName")
    val modelName: String,
    @SerialName("rmse")
    val rmse: Double,
    @SerialName("mae")
    val mae: Double,
    @SerialName("r2Score")
    val r2Score: Double
)

@Serializable
data class PredictionResult(
    @SerialName("predictions")
    val predictions: List<ChartPredictionData>,
    @SerialName("performances")
    val performances: List<ModelPerformance>,
    @SerialName("summary")
    val summary: PredictionSummary
)

@Serializable
data class PredictionSummary(
    @SerialName("currentPrice")
    val currentPrice: Double,
    @SerialName("predictedPrice")
    val predictedPrice: Double,
    @SerialName("expectedChange")
    val expectedChange: Double,
    @SerialName("confidence")
    val confidence: String,
    @SerialName("bestModel")
    val bestModel: String
)
