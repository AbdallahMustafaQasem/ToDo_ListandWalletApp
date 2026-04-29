package com.abdallah.taskvault.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.TransactionType
import com.abdallah.taskvault.domain.repository.WalletRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

private val Context.budgetAlertDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "budget_alerts")

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val walletRepository: WalletRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val budget = walletRepository.getBudget().first()?.monthlyBudget ?: 0.0
            if (budget <= 0.0) return Result.success()

            val transactions = walletRepository.getTransactions().first()
            val now = Calendar.getInstance()
            val spentThisMonth = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }
                .sumOf { it.amount }

            val percent = ((spentThisMonth / budget) * 100).toInt()
            val currentMonth = now.get(Calendar.YEAR) * 100 + now.get(Calendar.MONTH)

            val lastAlertKey = intPreferencesKey("last_alert_month_threshold")
            val lastSentKey  = intPreferencesKey("last_sent_month")
            val prefs = context.budgetAlertDataStore.data.first()
            val lastThreshold = prefs[lastAlertKey] ?: 0
            val lastMonth     = prefs[lastSentKey] ?: -1

            val threshold = when {
                percent >= 100 && (lastMonth != currentMonth || lastThreshold < 100) -> 100
                percent >= 90  && (lastMonth != currentMonth || lastThreshold < 90)  -> 90
                percent >= 75  && (lastMonth != currentMonth || lastThreshold < 75)  -> 75
                else -> 0
            }
            if (threshold == 0) return Result.success()

            context.budgetAlertDataStore.edit {
                it[lastAlertKey] = threshold
                it[lastSentKey]  = currentMonth
            }

            sendNotification(percent, spentThisMonth, budget, threshold)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(percent: Int, spent: Double, budget: Double, threshold: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.budget_alert_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.budget_alert_channel_desc) }
            notificationManager.createNotificationChannel(channel)
        }

        val title = when (threshold) {
            100  -> context.getString(R.string.budget_alert_exceeded_title)
            90   -> context.getString(R.string.budget_alert_90_title)
            else -> context.getString(R.string.budget_alert_75_title)
        }
        val body = context.getString(
            R.string.budget_alert_body,
            "%.0f".format(spent),
            "%.0f".format(budget),
            percent
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + threshold, notification)
    }

    companion object {
        private const val WORK_NAME      = "budget_alert"
        private const val CHANNEL_ID     = "budget_alerts"
        private const val NOTIFICATION_ID = 9000

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
