package com.nongtri.app.platform

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

actual class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    actual fun startRecording(): Result<String> {
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

    companion object {
        private const val TAG = "AudioRecorder"
    }
}
