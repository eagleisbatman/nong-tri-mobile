package com.nongtri.app.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide HapticFeedback throughout the app
 */
val LocalHapticFeedback = staticCompositionLocalOf<HapticFeedback> {
    error("HapticFeedback not provided")
}
