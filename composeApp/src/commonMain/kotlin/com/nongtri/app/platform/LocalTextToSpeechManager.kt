package com.nongtri.app.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide TextToSpeechManager throughout the app
 */
val LocalTextToSpeechManager = staticCompositionLocalOf<TextToSpeechManager> {
    error("TextToSpeechManager not provided")
}
