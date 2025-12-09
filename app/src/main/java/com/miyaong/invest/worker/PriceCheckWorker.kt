package com.miyaong.invest.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miyaong.invest.MainActivity
import com.miyaong.invest.R
import com.miyaong.invest.data.local.PriceAlertDao
import com.miyaong.invest.data.model.Result
import com.miyaong.invest.data.repository.StockRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class PriceCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val priceAlertDao: PriceAlertDao,
    private val stockRepository: StockRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "price_check_work"
        private const val CHANNEL_ID = "price_alerts"
        private const val CHANNEL_NAME = "가격 알림"
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        try {
            // Get all active alerts
            val activeAlerts = priceAlertDao.getActiveAlerts().first()

            if (activeAlerts.isEmpty()) {
                return Result.success()
            }

            // Check each alert
            for (alert in activeAlerts) {
                try {
                    // Fetch current stock price
                    when (val result = stockRepository.getStockInfo(alert.ticker)) {
                        is com.miyaong.invest.data.model.Result.Success -> {
                            val currentPrice = result.data.currentPrice ?: continue

                            // Check if alert condition is met
                            val shouldTrigger = if (alert.isAbove) {
                                currentPrice >= alert.targetPrice
                            } else {
                                currentPrice <= alert.targetPrice
                            }

                            if (shouldTrigger) {
                                // Mark alert as triggered
                                priceAlertDao.markAsTriggered(alert.id)

                                // Send notification
                                sendNotification(
                                    alert.id,
                                    alert.stockName,
                                    alert.ticker,
                                    alert.targetPrice,
                                    currentPrice,
                                    alert.isAbove
                                )
                            }
                        }
                        else -> {
                            // Failed to fetch price, continue with next alert
                            continue
                        }
                    }
                } catch (e: Exception) {
                    // Log error but continue with other alerts
                    e.printStackTrace()
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun sendNotification(
        alertId: Int,
        stockName: String,
        ticker: String,
        targetPrice: Double,
        currentPrice: Double,
        isAbove: Boolean
    ) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alertId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val condition = if (isAbove) "이상" else "이하"
        val title = "🔔 목표가 도달!"
        val text = "$stockName ($ticker): $${String.format("%.2f", currentPrice)}\n" +
                   "목표가 $${String.format("%.2f", targetPrice)} $condition 도달"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + alertId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "주식 목표가 알림을 받습니다"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
