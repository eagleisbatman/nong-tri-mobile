package com.nongtri.app.platform

/**
 * Platform-specific Text-to-Speech functionality using OpenAI TTS API
 */
expect class TextToSpeechManager {
    /**
     * Speak the given text using OpenAI TTS
     * @param text The text to speak
     * @param language Language code (e.g., "en", "vi")
     * @param voice OpenAI voice (alloy, echo, fable, onyx, nova, shimmer)
     * @param tone Speaking tone (friendly, professional, empathetic, excited, calm, neutral)
     */
    suspend fun speak(
        text: String,
        language: String = "en",
        voice: String = "alloy",
        tone: String = "friendly"
    )

    /**
     * Stop any ongoing speech
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
