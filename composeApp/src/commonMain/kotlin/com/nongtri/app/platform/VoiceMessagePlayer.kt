package com.nongtri.app.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Voice message player for playback of user voice recordings
 * Expect/actual pattern for platform-specific MediaPlayer
 */
expect class VoiceMessagePlayer {
    val isPlaying: StateFlow<Boolean>
    val duration: StateFlow<Int>  // milliseconds
    val position: StateFlow<Int>  // milliseconds

    fun play(audioUrl: String)
    fun pause()
    fun stop()
    fun getPositionPercent(): Float
    fun shutdown()
}
