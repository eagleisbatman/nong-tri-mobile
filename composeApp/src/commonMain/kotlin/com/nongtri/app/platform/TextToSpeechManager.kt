package com.nongtri.app.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * TTS playback state
 */
enum class TtsState {
    IDLE,       // Not doing anything
    LOADING,    // Downloading audio
    PLAYING,    // Playing audio
    PAUSED,     // Paused (can resume)
    ERROR       // Error occurred
}

/**
 * Platform-specific Text-to-Speech functionality using OpenAI TTS API
 */
expect class TextToSpeechManager {
    /**
     * Current TTS state
     */
    val state: StateFlow<TtsState>

    /**
     * Speak the given text using OpenAI TTS
     * @param text The text to speak
     * @param language Language code (e.g., "en", "vi")
     * @param voice OpenAI voice (alloy, echo, fable, onyx, nova, shimmer)
     * @param tone Speaking tone (friendly, professional, empathetic, excited, calm, neutral)
     * @param cachedAudioUrl Optional cached audio URL to skip regeneration
     * @return The audio URL (for caching)
     */
    suspend fun speak(
        text: String,
        language: String = "en",
        voice: String = "alloy",
        tone: String = "friendly",
        cachedAudioUrl: String? = null,
        conversationId: Int? = null,
        messageId: String? = null
    ): String?

    /**
     * Pause current speech (can be resumed)
     */
    fun pause()

    /**
     * Resume paused speech
     */
    fun resume()

    /**
     * Stop any ongoing speech (releases resources, cannot resume)
     */
    fun stop()

    /**
     * Check if TTS is currently playing
     */
    fun isSpeaking(): Boolean

    /**
     * Release TTS resources - should be called when no longer needed
     */
    fun shutdown()
}
