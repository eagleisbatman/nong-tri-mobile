package com.nongtri.app.ui.viewmodel

/**
 * State for voice recording UI
 */
sealed class VoiceRecordingState {
    /** Not recording */
    data object Idle : VoiceRecordingState()

    /** Recording in progress */
    data class Recording(
        val durationMs: Long = 0,
        val waveformAmplitudes: List<Float> = emptyList()
    ) : VoiceRecordingState()

    /** Transcribing recorded audio */
    data object Transcribing : VoiceRecordingState()

    /** Recording cancelled */
    data object Cancelled : VoiceRecordingState()

    /** Error during recording or transcription */
    data class Error(val message: String) : VoiceRecordingState()
}
