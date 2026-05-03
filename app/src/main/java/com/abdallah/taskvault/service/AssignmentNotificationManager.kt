package com.abdallah.taskvault.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.abdallah.taskvault.MainActivity
import com.abdallah.taskvault.R
import com.abdallah.taskvault.di.AppModule
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val notificationManager: NotificationManager
) {
    private var listener: ListenerRegistration? = null

    fun startListening(userId: String) {
        stopListening()
        refreshFcmToken(userId)

        // Mark existing docs as "seen" baseline — only show NEW ones after this point
        var isFirstSnapshot = true

        listener = firestore.collection("users").document(userId)
            .collection("notifications")
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (isFirstSnapshot) {
                    isFirstSnapshot = false
                    return@addSnapshotListener
                }
                snapshot.documentChanges.forEach { change ->
                    val doc = change.document
                    val type  = doc.getString("type") ?: return@forEach
                    val title = doc.getString("title") ?: return@forEach
                    val from  = doc.getString("fromName") ?: ""

                    val (notifTitle, notifBody) = when (type) {
                        "task_assigned" ->
                            context.getString(R.string.notif_assigned_title) to
                            context.getString(R.string.notif_assigned_body, from, title)
                        "status_updated" -> {
                            val status = doc.getString("newStatus") ?: ""
                            context.getString(R.string.notif_status_title) to
                            context.getString(R.string.notif_status_body, from, title, status)
                        }
                        else -> return@forEach
                    }

                    showNotification(notifTitle, notifBody)

                    // Mark as read
                    doc.reference.update("read", true)
                }
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    private fun refreshFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
        }
    }

    private fun showNotification(title: String, body: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AppModule.ASSIGNMENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
