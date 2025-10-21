package com.nongtri.app.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

actual class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    actual fun startRecording(): Result<String> {
        // Check RECORD_AUDIO permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return Result.failure(SecurityException("Microphone permission not granted. Please enable it in settings."))
        }

        return try {
            // Create temp file for recording
            val tempDir = File(context.cacheDir, "voice")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            audioFile = File(tempDir, "voice_${System.currentTimeMillis()}.m4a")

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                // Use VOICE_RECOGNITION for better gain/processing
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile!!.absolutePath)

                // Enable audio gain for quiet inputs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // setMaxAmplitude is not available, but we can log it
                    Log.d(TAG, "Using VOICE_RECOGNITION audio source for better gain")
                }

                try {
                    prepare()
                    start()
                    Log.d(TAG, "MediaRecorder prepared and started successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing/starting MediaRecorder", e)
                    throw e
                }
            }

            isRecording = true
            Log.d(TAG, "Recording started: ${audioFile!!.absolutePath}")
            Result.success(audioFile!!.absolutePath)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start recording", e)
            Result.failure(e)
        }
    }

    actual fun stopRecording(): Result<File> {
        return try {
            if (!isRecording) {
                Log.e(TAG, "stopRecording called but not recording")
                return Result.failure(IllegalStateException("Not currently recording"))
            }

            if (mediaRecorder == null) {
                Log.e(TAG, "stopRecording called but mediaRecorder is null")
                isRecording = false
                return Result.failure(IllegalStateException("MediaRecorder is null"))
            }

            if (audioFile == null) {
                Log.e(TAG, "stopRecording called but audioFile is null")
                isRecording = false
                mediaRecorder = null
                return Result.failure(IllegalStateException("Audio file is null"))
            }

            Log.d(TAG, "Stopping recording...")
            val fileToReturn = audioFile!!
            val recorderToStop = mediaRecorder

            // Mark as not recording FIRST to prevent concurrent access
            isRecording = false
            mediaRecorder = null

            // Now stop and release the MediaRecorder
            try {
                recorderToStop?.stop()
                Log.d(TAG, "MediaRecorder stopped successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping MediaRecorder (might be already stopped)", e)
                // Continue anyway - file might still be valid
            }

            try {
                recorderToStop?.release()
                Log.d(TAG, "MediaRecorder released successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaRecorder", e)
            }

            Log.d(TAG, "Recording stopped: ${fileToReturn.absolutePath} (${fileToReturn.length()} bytes)")
            Result.success(fileToReturn)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            isRecording = false
            mediaRecorder = null
            Result.failure(e)
        }
    }

    actual fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }

            audioFile?.delete()
            audioFile = null
            Log.d(TAG, "Recording cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling recording", e)
        }
    }

    actual fun isRecording(): Boolean = isRecording

    /**
     * Get current recording amplitude (0-32767)
     * Returns 0 if not recording
     */
    actual fun getMaxAmplitude(): Int {
        return try {
            if (!isRecording || mediaRecorder == null) {
                return 0
            }

            val amplitude = mediaRecorder?.maxAmplitude ?: 0
            amplitude
        } catch (e: Exception) {
            Log.e(TAG, "Error getting amplitude", e)
            0
        }
    }

    /**
     * Clean up old voice recording files from cache
     * Should be called periodically or on app startup
     */
    fun cleanupOldRecordings(maxAgeMillis: Long = 24 * 60 * 60 * 1000) {
        try {
            val tempDir = File(context.cacheDir, "voice")
            if (tempDir.exists()) {
                val now = System.currentTimeMillis()
                tempDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > maxAgeMillis) {
                        file.delete()
                        Log.d(TAG, "Deleted old recording: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up recordings", e)
        }
    }

    /**
     * Release resources and cleanup
     */
    fun shutdown() {
        cancelRecording()
        // Clean up all cache files on shutdown
        try {
            val tempDir = File(context.cacheDir, "voice")
            tempDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e(TAG, "Error during shutdown cleanup", e)
        }
    }

    companion object {
        private const val TAG = "AudioRecorder"
    }
}
