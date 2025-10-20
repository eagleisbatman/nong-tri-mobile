package com.nongtri.app.platform

import java.io.File

/**
 * Audio recorder for voice input (Whisper integration)
 * Records audio in AAC format (.m4a) for transcription
 */
expect class AudioRecorder {
    /**
     * Start recording audio
     * @return Result with file path on success
     */
    fun startRecording(): Result<String>

    /**
     * Stop recording and return the audio file
     * @return Result with File on success
     */
    fun stopRecording(): Result<File>

    /**
     * Cancel recording and delete temp file
     */
    fun cancelRecording()

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean
}
