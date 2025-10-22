package com.nongtri.app.platform

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Firebase Cloud Messaging Service
 * Handles FCM token registration and updates
 */
class FCMService(private val context: Context) {
    private val tag = "FCMService"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val api = NongTriApi()
    private val userPreferences = UserPreferences.getInstance()

    /**
     * Initialize FCM and register token with backend
     */
    fun initialize() {
        scope.launch {
            try {
                // Get FCM token
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(tag, "FCM token obtained: ${token.take(20)}...")

                // Get device ID from UserPreferences
                val deviceId = userPreferences.getDeviceInfo().deviceId

                // Register with backend
                registerToken(deviceId, token)
            } catch (e: Exception) {
                Log.e(tag, "Failed to initialize FCM", e)
            }
        }
    }

    /**
     * Register FCM token with backend
     */
    private suspend fun registerToken(deviceId: String, fcmToken: String) {
        try {
            val deviceInfo = userPreferences.getDeviceInfo()

            api.registerFCMToken(
                deviceId = deviceId,
                fcmToken = fcmToken,
                platform = "android",
                appVersion = deviceInfo.clientVersion,
                osVersion = deviceInfo.deviceOsVersion
            )

            Log.d(tag, "FCM token registered successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to register FCM token", e)
        }
    }

    /**
     * Called when FCM token is refreshed
     * This should be called from MyFirebaseMessagingService.onNewToken()
     */
    fun onTokenRefresh(newToken: String) {
        scope.launch {
            try {
                val deviceId = userPreferences.getDeviceInfo().deviceId
                registerToken(deviceId, newToken)
                Log.d(tag, "FCM token refreshed and registered")
            } catch (e: Exception) {
                Log.e(tag, "Failed to register refreshed token", e)
            }
        }
    }
}
