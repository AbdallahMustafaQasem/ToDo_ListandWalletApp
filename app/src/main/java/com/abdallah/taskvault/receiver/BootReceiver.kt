package com.abdallah.taskvault.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.abdallah.taskvault.data.worker.ReminderWorker
import com.abdallah.taskvault.widget.TodoWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        // Enqueue WorkManager task to reschedule all active alarms
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        TodoWidgetReceiver.ensureScheduled(context)
    }
}
