package com.nongtri.app.platform

import android.content.Context
import android.media.MediaPlayer
import com.nongtri.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

actual class TextToSpeechManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val cacheDir = File(context.cacheDir, "tts")

    init {
        // Create cache directory
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    actual suspend fun speak(
        text: String,
        language: String,
        voice: String,
        tone: String
    ) = withContext(Dispatchers.IO) {
        try {
            // Stop any current playback
            stop()

            // Request TTS audio from backend
            val audioFile = requestTTS(text, language, voice, tone)

            // Play the audio
            withContext(Dispatchers.Main) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    setOnCompletionListener {
                        release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { _, _, _ ->
                        release()
                        mediaPlayer = null
                        true
                    }
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            println("TTS error: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun requestTTS(
        text: String,
        language: String,
        voice: String,
        tone: String
    ): File = withContext(Dispatchers.IO) {
        // Create request body
        val json = JSONObject().apply {
            put("text", text)
            put("language", language)
            put("voice", voice)
            put("tone", tone)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BuildConfig.API_URL}/api/tts")
            .post(requestBody)
            .build()

        // Execute request
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("TTS request failed: ${response.code}")
            }

            // Save audio to cache
            val audioBytes = response.body?.bytes()
                ?: throw Exception("Empty TTS response")

            val audioFile = File(cacheDir, "current_tts.mp3")
            audioFile.writeBytes(audioBytes)

            audioFile
        }
    }

    actual fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    actual fun isSpeaking(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    actual fun shutdown() {
        stop()

        // Clean up cache
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            println("Failed to clean TTS cache: ${e.message}")
        }
    }
}
