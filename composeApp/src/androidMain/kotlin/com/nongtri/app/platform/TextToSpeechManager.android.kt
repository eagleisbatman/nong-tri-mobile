package com.nongtri.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import com.nongtri.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

actual class TextToSpeechManager(private val context: Context) {
    private val _state = MutableStateFlow(TtsState.IDLE)
    actual val state: StateFlow<TtsState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    private var isProcessing = false

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
        // Prevent multiple simultaneous TTS requests
        if (isProcessing) {
            Log.d(TAG, "TTS: Already processing, ignoring request")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Speech already playing", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        isProcessing = true
        _state.value = TtsState.LOADING

        try {
            Log.d(TAG, "TTS: Starting speech generation for text: ${text.take(50)}...")

            // Stop any current playback
            stopInternal()

            // Request TTS audio from backend
            Log.d(TAG, "TTS: Requesting audio from backend")
            val audioFile = requestTTS(text, language, voice, tone)

            Log.d(TAG, "TTS: Audio file received, size: ${audioFile.length()} bytes")
            _state.value = TtsState.PLAYING

            // Play the audio
            withContext(Dispatchers.Main) {
                mediaPlayer = MediaPlayer().apply {
                    // Set audio attributes to use MEDIA stream with proper content type
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )

                    setDataSource(audioFile.absolutePath)
                    setOnCompletionListener {
                        Log.d(TAG, "TTS: Playback completed")
                        release()
                        mediaPlayer = null
                        isProcessing = false
                        _state.value = TtsState.IDLE
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "TTS: MediaPlayer error - what: $what, extra: $extra")
                        release()
                        mediaPlayer = null
                        isProcessing = false
                        _state.value = TtsState.ERROR
                        true
                    }

                    // Set volume to maximum to ensure audibility
                    setVolume(1.0f, 1.0f)

                    prepare()
                    start()
                    Log.d(TAG, "TTS: Playback started with audio attributes and max volume")
                }
            }
        } catch (e: Exception) {
            isProcessing = false
            _state.value = TtsState.ERROR
            Log.e(TAG, "TTS error: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "TTS Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            // Reset to IDLE after showing error
            kotlinx.coroutines.delay(3000)
            _state.value = TtsState.IDLE
        }
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
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

        // Execute request to get audio URL from backend
        Log.d(TAG, "TTS: Sending request to ${BuildConfig.API_URL}/api/tts")
        val audioUrl = client.newCall(request).execute().use { response ->
            Log.d(TAG, "TTS: Backend response code: ${response.code}")

            if (!response.isSuccessful) {
                throw Exception("TTS request failed: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string()
                ?: throw Exception("Empty TTS response")

            Log.d(TAG, "TTS: Response body: $responseBody")

            // Parse JSON response to get audio URL from MinIO
            val responseJson = JSONObject(responseBody)

            if (!responseJson.getBoolean("success")) {
                val error = responseJson.optString("error", "Unknown error")
                Log.e(TAG, "TTS: Generation failed - $error")
                throw Exception("TTS generation failed: $error")
            }

            val url = responseJson.getString("audioUrl")
            Log.d(TAG, "TTS: Got audio URL: $url")
            url
        }

        // Download audio file from MinIO URL
        Log.d(TAG, "TTS: Downloading from MinIO: $audioUrl")
        val audioRequest = Request.Builder()
            .url(audioUrl)
            .get()
            .build()

        client.newCall(audioRequest).execute().use { audioResponse ->
            Log.d(TAG, "TTS: MinIO response code: ${audioResponse.code}")

            if (!audioResponse.isSuccessful) {
                throw Exception("Failed to download audio: ${audioResponse.code} - ${audioResponse.message}")
            }

            val audioBytes = audioResponse.body?.bytes()
                ?: throw Exception("Empty audio response")

            Log.d(TAG, "TTS: Downloaded ${audioBytes.size} bytes")

            val audioFile = File(cacheDir, "current_tts.mp3")
            audioFile.writeBytes(audioBytes)

            Log.d(TAG, "TTS: Saved to ${audioFile.absolutePath}")
            audioFile
        }
    }

    private fun stopInternal() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            } catch (e: Exception) {
                Log.w(TAG, "TTS: Error stopping MediaPlayer: ${e.message}")
            } finally {
                mediaPlayer = null
            }
        }
    }

    actual fun stop() {
        stopInternal()
        isProcessing = false
        _state.value = TtsState.IDLE
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
