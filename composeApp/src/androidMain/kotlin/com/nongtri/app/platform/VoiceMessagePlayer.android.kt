package com.nongtri.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages playback of user voice messages
 * Simpler than TTS - just plays audio URLs directly
 */
actual class VoiceMessagePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    // CoroutineScope for position updates
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val _currentUrl = MutableStateFlow<String?>(null)
    actual val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    actual val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0) // milliseconds
    actual val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _position = MutableStateFlow(0) // milliseconds
    actual val position: StateFlow<Int> = _position.asStateFlow()

    /**
     * Start position update loop - updates position every 100ms during playback
     */
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _position.value = player.currentPosition
                    }
                }
                delay(100) // Update every 100ms for smooth progress
            }
        }
    }

    /**
     * Play or resume voice message from URL
     * @param audioUrl MinIO URL of voice recording
     */
    actual fun play(audioUrl: String) {
        try {
            // If same URL and paused, resume
            if (_currentUrl.value == audioUrl && mediaPlayer != null && !_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startPositionUpdates()  // Start position updates on resume
                Log.d(TAG, "Resumed playback: $audioUrl")
                return
            }

            // Stop any current playback
            stop()

            // Create new MediaPlayer
            _currentUrl.value = audioUrl
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
                    _position.value = 0  // Reset position for new playback
                    mp.start()
                    _isPlaying.value = true
                    startPositionUpdates()  // Start position updates on playback
                    Log.d(TAG, "Started playback: $audioUrl (${mp.duration}ms)")

                    // ROUND 6: Track voice message playback started (use audioUrl as identifier)
                    com.nongtri.app.analytics.Events.logVoiceMessagePlaybackStarted(
                        messageId = audioUrl,
                        durationMs = mp.duration.toLong()
                    )
                }
                setOnCompletionListener {
                    positionUpdateJob?.cancel()  // Stop position updates
                    _isPlaying.value = false

                    // ROUND 6: Track voice message playback completed (use currentUrl as identifier)
                    com.nongtri.app.analytics.Events.logVoiceMessagePlaybackCompleted(
                        messageId = _currentUrl.value ?: "unknown",
                        playbackDurationMs = _duration.value.toLong(),
                        listenedToEnd = true  // OnCompletionListener only fires when fully completed
                    )

                    _position.value = 0
                    Log.d(TAG, "Playback completed")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error - what: $what, extra: $extra")
                    positionUpdateJob?.cancel()  // Stop position updates on error
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
    actual fun pause() {
        positionUpdateJob?.cancel()  // Stop position updates when pausing
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
    actual fun stop() {
        positionUpdateJob?.cancel()  // Stop position updates when stopping
        mediaPlayer?.let {
            try {
                // Only stop if actually playing - avoid state errors
                // MediaPlayer.stop() can only be called from Started, Paused, Prepared, or PlaybackCompleted states
                // Calling stop() from Idle state causes error -38 (MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK)
                if (it.isPlaying) {
                    it.stop()
                    Log.d(TAG, "Stopped playback")
                }
                // Don't call reset() - it puts player in idle state, then release() is enough
                it.release()
                Log.d(TAG, "Released MediaPlayer")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaPlayer: ${e.message}")
                // Try to release anyway
                try {
                    it.release()
                } catch (releaseError: Exception) {
                    Log.e(TAG, "Error releasing MediaPlayer: ${releaseError.message}")
                }
            }
        }
        mediaPlayer = null
        _currentUrl.value = null
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }

    /**
     * Get current playback position as percentage (0.0 to 1.0)
     */
    actual fun getPositionPercent(): Float {
        val dur = _duration.value
        val pos = mediaPlayer?.currentPosition ?: 0
        return if (dur > 0) pos.toFloat() / dur else 0f
    }

    /**
     * Cleanup resources
     */
    actual fun shutdown() {
        positionUpdateJob?.cancel()  // Cancel any running position updates
        stop()
        scope.cancel()  // Cancel the coroutine scope
    }

    companion object {
        private const val TAG = "VoiceMessagePlayer"
    }
}
