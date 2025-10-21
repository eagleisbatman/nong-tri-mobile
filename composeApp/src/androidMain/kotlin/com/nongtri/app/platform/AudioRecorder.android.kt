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
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile!!.absolutePath)

                prepare()
                start()
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
            if (!isRecording || mediaRecorder == null || audioFile == null) {
                return Result.failure(IllegalStateException("Not currently recording"))
            }

            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            Log.d(TAG, "Recording stopped: ${audioFile!!.absolutePath} (${audioFile!!.length()} bytes)")
            Result.success(audioFile!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
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
