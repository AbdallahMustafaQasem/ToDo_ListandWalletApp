package com.abdallah.taskvault.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.repository.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyDigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val todoRepository: TodoRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val todos = todoRepository.getAllTodos().first()
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86_400_000L - 1L

        val dueTodayCount  = todos.count { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis in todayStart..todayEnd }
        val overdueCount   = todos.count { !it.isCompleted && it.dueDateMillis != null && it.dueDateMillis < todayStart }
        val activeTasks    = todos.count { !it.isCompleted }

        if (activeTasks == 0) return Result.success()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_digest"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, applicationContext.getString(R.string.notif_digest_channel), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val title = applicationContext.getString(R.string.notif_digest_title)
        val body = buildString {
            if (dueTodayCount > 0) append("$dueTodayCount ${applicationContext.getString(R.string.notif_digest_due_today)}. ")
            if (overdueCount > 0) append("$overdueCount ${applicationContext.getString(R.string.notif_digest_overdue)}. ")
            append("$activeTasks ${applicationContext.getString(R.string.notif_digest_active_total)}")
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()

        nm.notify(9001, notification)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_digest"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyDigestWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
