package com.abdallah.taskvault.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.abdallah.taskvault.R
import com.abdallah.taskvault.di.AppModule
import com.abdallah.taskvault.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_TODO_TITLE = "extra_todo_title"
        const val EXTRA_TODO_DESCRIPTION = "extra_todo_description"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        if (todoId == -1L) return

        val title = intent.getStringExtra(EXTRA_TODO_TITLE) ?: "Todo Reminder"
        val description = intent.getStringExtra(EXTRA_TODO_DESCRIPTION)
            ?.takeIf { it.isNotBlank() } ?: "No description added."

        // Open app / deep-link intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_DETAIL_TODO_ID, todoId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            todoId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark done action intent
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_DONE
            putExtra(NotificationActionReceiver.EXTRA_TODO_ID, todoId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            todoId.toInt() + 10000,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AppModule.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, context.getString(R.string.notif_mark_done), markDonePendingIntent)
            .addAction(0, context.getString(R.string.notif_open_app), openAppPendingIntent)
            .build()

        notificationManager.notify(todoId.toInt(), notification)
    }
}
