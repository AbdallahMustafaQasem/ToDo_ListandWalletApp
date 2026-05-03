package com.abdallah.taskvault.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.abdallah.taskvault.MainActivity
import com.abdallah.taskvault.R
import com.abdallah.taskvault.di.AppModule
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BillReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationManager

    companion object {
        const val EXTRA_BILL_ID     = "extra_bill_id"
        const val EXTRA_BILL_NAME   = "extra_bill_name"
        const val EXTRA_BILL_AMOUNT = "extra_bill_amount"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val billId   = intent.getLongExtra(EXTRA_BILL_ID, -1L).takeIf { it != -1L } ?: return
        val name     = intent.getStringExtra(EXTRA_BILL_NAME) ?: "Bill Reminder"
        val amount   = intent.getDoubleExtra(EXTRA_BILL_AMOUNT, 0.0)
        val body     = context.getString(R.string.bill_reminder_body, name, amount)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context, (billId + 600_000).toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AppModule.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.bill_reminder_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPi)
            .build()

        notificationManager.notify((billId + 600_000).toInt(), notification)
    }
}
