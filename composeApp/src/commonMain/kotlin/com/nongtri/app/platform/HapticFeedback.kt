package com.nongtri.app.platform

/**
 * Haptic feedback helper for providing tactile responses
 * Provides different vibration patterns for user interactions
 */
expect class HapticFeedback {
    /**
     * Light tap for subtle confirmations
     * Use for: Keyboard tap, selection, minor actions
     */
    fun tick()

    /**
     * Medium tap for standard actions
     * Use for: Button clicks, message sent, standard confirmations
     */
    fun click()

    /**
     * Strong tap for important actions
     * Use for: Recording start, important confirmations, destructive actions
     */
    fun heavyClick()

    /**
     * Double tap for success/completion
     * Use for: Voice message sent, upload complete, task completed
     */
    fun doubleTap()

    /**
     * Error pattern (3 short buzzes)
     * Use for: Recording error, upload failed, validation failed
     */
    fun error()

    /**
     * Success pattern (short-long)
     * Use for: Upload success, operation completed successfully
     */
    fun success()

    /**
     * Very gentle tap for subtle feedback
     * Use for: Response started, minimal confirmations
     */
    fun gentleTick()
}
