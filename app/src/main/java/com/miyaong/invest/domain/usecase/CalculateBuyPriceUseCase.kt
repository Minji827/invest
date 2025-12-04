package com.miyaong.invest.domain.usecase

import com.miyaong.invest.data.model.ChartDataPoint
import com.miyaong.invest.data.model.InvestmentStrategy
import com.miyaong.invest.data.model.Prediction
import com.miyaong.invest.data.model.PricePrediction
import javax.inject.Inject

class CalculateBuyPriceUseCase @Inject constructor() {

    operator fun invoke(
        symbol: String,
        chartData: List<ChartDataPoint>,
        strategy: InvestmentStrategy = InvestmentStrategy.LONG_TERM
    ): Prediction {
        if (chartData.isEmpty()) {
            return createEmptyPrediction(symbol, strategy)
        }

        val closePrices = chartData.map { it.close }
        val currentPrice = closePrices.lastOrNull() ?: 0.0

        val targetPrice = when (strategy) {
            InvestmentStrategy.SHORT_TERM -> calculateShortTermTarget(closePrices, currentPrice)
            InvestmentStrategy.LONG_TERM -> calculateLongTermTarget(closePrices, currentPrice)
        }

        val recommendation = generateRecommendation(currentPrice, targetPrice, strategy)
        val predictions = generatePredictions(chartData, strategy)

        return Prediction(
            symbol = symbol,
            strategy = strategy,
            targetPrice = targetPrice,
            confidence = calculateConfidence(closePrices),
            predictions = predictions,
            recommendation = recommendation
        )
    }

    private fun calculateShortTermTarget(prices: List<Double>, currentPrice: Double): Double {
        // 단기: 최근 20일 이동평균 기준
        val ma20 = calculateMA(prices, 20)
        val ma5 = calculateMA(prices, 5)

        // 5일 평균이 20일 평균보다 높으면 상승 추세
        return if (ma5 > ma20) {
            // 현재가의 3-5% 아래를 매수 타겟으로 설정
            currentPrice * 0.97
        } else {
            // 현재가의 5-7% 아래를 매수 타겟으로 설정
            currentPrice * 0.95
        }
    }

    private fun calculateLongTermTarget(prices: List<Double>, currentPrice: Double): Double {
        // 장기: 50일, 200일 이동평균 기준
        val ma50 = calculateMA(prices, 50)
        val ma200 = calculateMA(prices, 200)

        // 골든 크로스 (50일 > 200일) 체크
        return if (ma50 > ma200) {
            // 상승 추세: 200일 이동평균 근처를 매수 타겟으로
            minOf(ma200, currentPrice * 0.95)
        } else {
            // 하락 또는 횡보: 더 낮은 가격을 매수 타겟으로
            currentPrice * 0.90
        }
    }

    private fun calculateMA(prices: List<Double>, period: Int): Double {
        if (prices.size < period) {
            return prices.average()
        }
        return prices.takeLast(period).average()
    }

    private fun calculateConfidence(prices: List<Double>): Double {
        // 변동성 기반 신뢰도 계산
        if (prices.size < 2) return 0.5

        val returns = prices.zipWithNext { a, b -> (b - a) / a }
        val volatility = returns.map { kotlin.math.abs(it) }.average()

        // 변동성이 낮을수록 신뢰도가 높음
        return (1.0 - (volatility * 10)).coerceIn(0.3, 0.9)
    }

    private fun generateRecommendation(
        currentPrice: Double,
        targetPrice: Double,
        strategy: InvestmentStrategy
    ): String {
        val diff = ((currentPrice - targetPrice) / targetPrice * 100)

        return when {
            diff > 10 -> "현재가가 목표가보다 ${String.format("%.1f", diff)}% 높습니다. 관망을 권장합니다."
            diff > 5 -> "목표가에 근접했습니다. 분할 매수를 고려하세요."
            diff > 0 -> "좋은 매수 구간입니다. ${strategy.name} 전략에 적합합니다."
            else -> "목표가 이하입니다. 적극 매수를 고려하세요."
        }
    }

    private fun generatePredictions(
        chartData: List<ChartDataPoint>,
        strategy: InvestmentStrategy
    ): List<PricePrediction> {
        if (chartData.isEmpty()) return emptyList()

        val currentPrice = chartData.last().close
        val currentTime = chartData.last().timestamp

        // 향후 30일 예측
        val predictions = mutableListOf<PricePrediction>()
        val daysToPredict = 30
        val dayInMillis = 86400000L

        for (i in 1..daysToPredict) {
            val predictedPrice = when (strategy) {
                InvestmentStrategy.SHORT_TERM -> {
                    // 단기: 작은 변동 예측
                    currentPrice * (1 + (kotlin.random.Random.nextDouble(-0.02, 0.02)))
                }
                InvestmentStrategy.LONG_TERM -> {
                    // 장기: 추세 기반 예측
                    val trend = calculateTrend(chartData.takeLast(30).map { it.close })
                    currentPrice * (1 + trend * i / 100.0)
                }
            }

            predictions.add(
                PricePrediction(
                    date = currentTime + (i * dayInMillis),
                    price = predictedPrice,
                    upperBound = predictedPrice * 1.05,
                    lowerBound = predictedPrice * 0.95
                )
            )
        }

        return predictions
    }

    private fun calculateTrend(prices: List<Double>): Double {
        if (prices.size < 2) return 0.0

        val firstPrice = prices.first()
        val lastPrice = prices.last()

        return ((lastPrice - firstPrice) / firstPrice) * 100
    }

    private fun createEmptyPrediction(symbol: String, strategy: InvestmentStrategy): Prediction {
        return Prediction(
            symbol = symbol,
            strategy = strategy,
            targetPrice = 0.0,
            confidence = 0.0,
            predictions = emptyList(),
            recommendation = "데이터가 부족합니다."
        )
    }
}
