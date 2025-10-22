package com.nongtri.app.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nongtri.app.MainActivity
import com.nongtri.app.R
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.l10n.LocalizationProvider

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and token refresh
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag = "FCMService"
    private val userPreferences by lazy { UserPreferences.getInstance() }
    private val strings get() = LocalizationProvider.getStrings(userPreferences.language.value)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token: ${token.take(20)}...")

        // Notify FCMService to register new token
        val fcmService = FCMService(applicationContext)
        fcmService.onTokenRefresh(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(tag, "Message received from: ${message.from}")

        // Get notification data
        val notification = message.notification
        val data = message.data

        // Extract job ID from data payload
        val jobId = data["jobId"]
        val type = data["type"]

        Log.d(tag, "Notification type: $type, jobId: $jobId")

        // Show notification
        showNotification(
            title = notification?.title ?: strings.notificationDiagnosisTitle,
            body = notification?.body ?: strings.notificationDiagnosisBody,
            jobId = jobId
        )
    }

    private fun showNotification(title: String, body: String, jobId: String?) {
        val channelId = "diagnosis_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                strings.notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = strings.notificationChannelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pass jobId to MainActivity
            putExtra("jobId", jobId)
            putExtra("openDiagnosis", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification with brand colors
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // Default Android icon for now
            .setContentTitle(title)
            .setContentText(body)
            .setColor(resources.getColor(com.nongtri.app.R.color.brand_green_dark, null)) // Nông Trí brand green
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(tag, "Notification shown: $title")
    }
}
