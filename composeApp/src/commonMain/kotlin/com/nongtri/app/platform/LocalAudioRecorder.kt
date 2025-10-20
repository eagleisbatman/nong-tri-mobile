package com.nongtri.app.platform

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAudioRecorder = staticCompositionLocalOf<AudioRecorder> {
    error("AudioRecorder not provided")
}
