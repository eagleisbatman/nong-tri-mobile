package com.nongtri.app.platform

import androidx.compose.runtime.staticCompositionLocalOf

val LocalVoiceMessagePlayer = staticCompositionLocalOf<VoiceMessagePlayer> {
    error("VoiceMessagePlayer not provided")
}
