package com.example.todoapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.data.alarm.AlarmScheduler
import com.example.todoapp.data.local.TodoDao
import com.example.todoapp.data.mapper.toDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker used to reschedule all active alarms after device reboot.
 * This acts as a fallback if the BroadcastReceiver is killed before completing.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val todoDao: TodoDao,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val activeReminders = todoDao.getActiveReminders(now)
            activeReminders.forEach { entity ->
                alarmScheduler.schedule(entity.toDomain())
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
