package com.abdallah.taskvault.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abdallah.taskvault.domain.repository.TodoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TodoRepository

    @Inject
    lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_MARK_DONE = "com.abdallah.taskvault.ACTION_MARK_DONE"
        const val EXTRA_TODO_ID = "extra_todo_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_DONE) return

        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        if (todoId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                repository.toggleCompletion(todoId, true)
                notificationManager.cancel(todoId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
