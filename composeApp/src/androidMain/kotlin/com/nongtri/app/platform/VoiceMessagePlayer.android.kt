package com.nongtri.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages playback of user voice messages
 * Simpler than TTS - just plays audio URLs directly
 */
class VoiceMessagePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0) // milliseconds
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _position = MutableStateFlow(0) // milliseconds
    val position: StateFlow<Int> = _position.asStateFlow()

    /**
     * Play or resume voice message from URL
     * @param audioUrl MinIO URL of voice recording
     */
    fun play(audioUrl: String) {
        try {
            // If same URL and paused, resume
            if (currentUrl == audioUrl && mediaPlayer != null && !_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                Log.d(TAG, "Resumed playback: $audioUrl")
                return
            }

            // Stop any current playback
            stop()

            // Create new MediaPlayer
            currentUrl = audioUrl
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    Log.d(TAG, "Started playback: $audioUrl (${mp.duration}ms)")
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _position.value = 0
                    Log.d(TAG, "Playback completed")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error - what: $what, extra: $extra")
                    stop()
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing voice message", e)
            stop()
        }
    }

    /**
     * Pause playback
     */
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _position.value = it.currentPosition
                _isPlaying.value = false
                Log.d(TAG, "Paused at ${it.currentPosition}ms")
            }
        }
    }

    /**
     * Stop playback and release resources
     */
    fun stop() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
        currentUrl = null
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }

    /**
     * Get current playback position as percentage (0.0 to 1.0)
     */
    fun getPositionPercent(): Float {
        val dur = _duration.value
        val pos = mediaPlayer?.currentPosition ?: 0
        return if (dur > 0) pos.toFloat() / dur else 0f
    }

    /**
     * Cleanup resources
     */
    fun shutdown() {
        stop()
    }

    companion object {
        private const val TAG = "VoiceMessagePlayer"
    }
}
