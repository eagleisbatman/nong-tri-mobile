package com.nongtri.app.data.provider

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.nongtri.app.data.model.DeviceInfo
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

actual class DeviceInfoProvider(private val context: Context) {

    private val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_UUID = "uuid"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val CLIENT_VERSION = "1.0.0" // Should come from BuildConfig
        private const val CLIENT_BUILD_NUMBER = "1" // Should come from BuildConfig
        private const val CLIENT_SOURCE = "android_app"
    }

    actual fun getDeviceInfo(): DeviceInfo {
        val deviceId = getDeviceId()
        val uuid = getUuid()

        // Get screen dimensions
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // Get timezone offset in minutes
        val timezoneOffset = TimeZone.getDefault().rawOffset / (1000 * 60)

        // Get device language
        val deviceLanguage = Locale.getDefault().language

        return DeviceInfo(
            deviceId = deviceId,
            uuid = uuid,
            deviceType = getDeviceType(),
            deviceOs = "Android",
            deviceOsVersion = Build.VERSION.RELEASE,
            deviceManufacturer = Build.MANUFACTURER,
            deviceModel = Build.MODEL,
            deviceBrand = Build.BRAND,
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            screenDensity = displayMetrics.density,
            clientSource = CLIENT_SOURCE,
            clientVersion = CLIENT_VERSION,
            clientBuildNumber = CLIENT_BUILD_NUMBER,
            timezoneOffset = timezoneOffset,
            deviceLanguage = deviceLanguage
        )
    }

    actual fun getDeviceId(): String {
        // Use Android ID as the device identifier
        // This persists across app installs but resets on factory reset
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    actual fun getUuid(): String {
        // Check if UUID already exists
        val existingUuid = prefs.getString(KEY_UUID, null)

        if (existingUuid != null) {
            return existingUuid
        }

        // Generate new UUID for this installation
        val newUuid = UUID.randomUUID().toString()

        prefs.edit().apply {
            putString(KEY_UUID, newUuid)
            putBoolean(KEY_FIRST_LAUNCH, false)
            apply()
        }

        return newUuid
    }

    actual fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    private fun getDeviceType(): String {
        // Determine if device is phone or tablet based on screen size
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val heightDp = displayMetrics.heightPixels / displayMetrics.density
        val smallestWidth = minOf(widthDp, heightDp)

        return if (smallestWidth >= 600) "tablet" else "phone"
    }
}
