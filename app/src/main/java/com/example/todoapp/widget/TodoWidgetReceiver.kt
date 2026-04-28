package com.example.todoapp.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TICK) {
            Log.d(TAG, "tick received — refreshing widget")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    TodoWidget().updateAll(context.applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "tick updateAll failed", e)
                }
            }
            scheduleNextTick(context.applicationContext)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextTick(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelTick(context.applicationContext)
    }

    companion object {
        private const val TAG = "TodoWidgetReceiver"
        const val ACTION_TICK = "com.example.todoapp.widget.ACTION_TICK"
        private const val REQUEST_CODE = 2041
        private const val INTERVAL_MS = 60_000L

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, TodoWidgetReceiver::class.java).apply {
                action = ACTION_TICK
            }
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun scheduleNextTick(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS
            try {
                am.setExact(AlarmManager.ELAPSED_REALTIME, triggerAt, tickPendingIntent(context))
            } catch (_: SecurityException) {
                am.set(AlarmManager.ELAPSED_REALTIME, triggerAt, tickPendingIntent(context))
            }
        }

        fun cancelTick(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            am.cancel(tickPendingIntent(context))
        }

        fun ensureScheduled(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                android.content.ComponentName(context, TodoWidgetReceiver::class.java)
            )
            if (ids.isNotEmpty()) scheduleNextTick(context)
        }
    }
}
