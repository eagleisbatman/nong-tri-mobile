package com.nongtri.app.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback helper for Android
 * Provides simple API for different haptic patterns
 * Handles Android version compatibility (API 24+)
 * Respects user preference for haptics
 */
actual class HapticFeedback(private val context: Context) {

    private val userPreferences by lazy { com.nongtri.app.data.preferences.UserPreferences.getInstance() }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Light tap for subtle confirmations
     * Use for: Keyboard tap, selection, minor actions
     */
    actual fun tick() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, 50))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Medium tap for standard actions
     * Use for: Button clicks, message sent, standard confirmations
     */
    actual fun click() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Strong tap for important actions
     * Use for: Recording start, important confirmations, destructive actions
     */
    actual fun heavyClick() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, 200))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Double tap for success/completion
     * Use for: Voice message sent, upload complete, task completed
     */
    actual fun doubleTap() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 30, 50, 30)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 30, 50, 30)
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Error pattern (3 short buzzes)
     * Use for: Recording error, upload failed, validation failed
     */
    actual fun error() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Success pattern (short-long)
     * Use for: Upload success, operation completed successfully
     */
    actual fun success() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 30, 100, 60)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 30, 100, 60)
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Very gentle tap for subtle feedback
     * Use for: Response started, minimal confirmations
     */
    actual fun gentleTick() {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, 40))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (e: SecurityException) {
            println("[HapticFeedback] VIBRATE permission denied: ${e.message}")
        }
    }

    /**
     * Check if device has vibrator AND user has haptics enabled
     */
    private fun hasVibrator(): Boolean {
        return (vibrator?.hasVibrator() ?: false) && userPreferences.hapticsEnabled.value
    }
}
