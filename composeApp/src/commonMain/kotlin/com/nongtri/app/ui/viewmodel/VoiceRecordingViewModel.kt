package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.platform.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for voice recording and transcription
 * Manages WhatsApp-style hold-to-record functionality
 */
class VoiceRecordingViewModel(
    private val audioRecorder: AudioRecorder,
    private val api: NongTriApi = NongTriApi()
) : ViewModel() {

    private val _state = MutableStateFlow<VoiceRecordingState>(VoiceRecordingState.Idle)
    val state: StateFlow<VoiceRecordingState> = _state.asStateFlow()

    private var recordingJob: Job? = null
    private var recordingStartTime: Long = 0

    // Background transcription state
    private var backgroundTranscription: String? = null
    private var backgroundVoiceAudioUrl: String? = null
    private var isTranscribing = false

    /**
     * Start recording audio
     */
    fun startRecording() {
        if (_state.value !is VoiceRecordingState.Idle) return

        audioRecorder.startRecording().fold(
            onSuccess = { filePath ->
                recordingStartTime = System.currentTimeMillis()
                _state.value = VoiceRecordingState.Recording()
                startRecordingTimer()
                println("[VoiceRecording] Started recording: $filePath")
            },
            onFailure = { error ->
                _state.value = VoiceRecordingState.Error(error.message ?: "Failed to start recording")
                println("[VoiceRecording] Error starting recording: ${error.message}")
            }
        )
    }

    /**
     * Stop recording and transcribe
     * @param userId Device ID for saving voice message
     * @param language Language code for transcription (e.g., "en", "vi")
     * @param onTranscribed Callback with transcribed text and voice audio URL
     */
    fun stopRecording(
        userId: String,
        language: String = "en",
        onTranscribed: (transcription: String, voiceAudioUrl: String?) -> Unit
    ) {
        recordingJob?.cancel()
        recordingJob = null

        audioRecorder.stopRecording().fold(
            onSuccess = { audioFile ->
                println("[VoiceRecording] Stopped recording: ${audioFile.absolutePath} (${audioFile.length()} bytes)")

                // ✅ VALIDATION #1: Check recording duration (minimum 0.5 seconds)
                val durationMs = System.currentTimeMillis() - recordingStartTime
                if (durationMs < 500) {
                    _state.value = VoiceRecordingState.Error("Recording too short. Please hold longer.")
                    println("[VoiceRecording] Recording too short: ${durationMs}ms")
                    audioFile.delete()
                    resetToIdle()
                    return
                }

                // ✅ VALIDATION #2: Check file size (minimum 1KB)
                if (audioFile.length() < 1000) {
                    _state.value = VoiceRecordingState.Error("Recording is empty. Please try again.")
                    println("[VoiceRecording] File too small: ${audioFile.length()} bytes")
                    audioFile.delete()
                    resetToIdle()
                    return
                }

                // ✅ VALIDATIONS PASSED - Proceed with transcription
                _state.value = VoiceRecordingState.Transcribing
                transcribeAndSaveVoiceMessage(userId, audioFile, language, onTranscribed)
            },
            onFailure = { error ->
                _state.value = VoiceRecordingState.Error(error.message ?: "Failed to stop recording")
                println("[VoiceRecording] Error stopping recording: ${error.message}")
                resetToIdle()
            }
        )
    }

    /**
     * Stop recording, save file for preview, and start transcription in background
     * Returns the audio file path if successful
     */
    fun stopForPreview(userId: String, language: String = "en"): File? {
        recordingJob?.cancel()
        recordingJob = null

        var savedFile: File? = null
        audioRecorder.stopRecording().fold(
            onSuccess = { audioFile ->
                println("[VoiceRecording] Stopped for preview: ${audioFile.absolutePath} (${audioFile.length()} bytes)")

                // Validation: Check recording duration (minimum 0.5 seconds)
                val durationMs = System.currentTimeMillis() - recordingStartTime
                if (durationMs < 500) {
                    _state.value = VoiceRecordingState.Error("Recording too short. Please record longer.")
                    println("[VoiceRecording] Recording too short: ${durationMs}ms")
                    audioFile.delete()
                    resetToIdle()
                    return null
                }

                // Validation: Check file size (minimum 1KB)
                if (audioFile.length() < 1000) {
                    _state.value = VoiceRecordingState.Error("Recording is empty. Please try again.")
                    println("[VoiceRecording] File too small: ${audioFile.length()} bytes")
                    audioFile.delete()
                    resetToIdle()
                    return null
                }

                // Save file for preview
                savedFile = audioFile
                _state.value = VoiceRecordingState.Idle

                // ✅ START TRANSCRIPTION IN BACKGROUND IMMEDIATELY
                println("[VoiceRecording] Starting background transcription...")
                startBackgroundTranscription(userId, audioFile, language)
            },
            onFailure = { error ->
                _state.value = VoiceRecordingState.Error(error.message ?: "Failed to stop recording")
                println("[VoiceRecording] Error stopping recording: ${error.message}")
                resetToIdle()
            }
        )

        return savedFile
    }

    /**
     * Start transcription in background (non-blocking)
     */
    private fun startBackgroundTranscription(userId: String, audioFile: File, language: String) {
        isTranscribing = true
        backgroundTranscription = null
        backgroundVoiceAudioUrl = null

        println("[VoiceRecording] Background transcription started")
        // transcribeAndSaveVoiceMessage already launches its own coroutine
        transcribeAndSaveVoiceMessage(userId, audioFile, language) { transcription, voiceAudioUrl ->
            backgroundTranscription = transcription
            backgroundVoiceAudioUrl = voiceAudioUrl
            isTranscribing = false
            println("[VoiceRecording] Background transcription complete: $transcription")
            println("[VoiceRecording] Voice file uploaded to MinIO: $voiceAudioUrl")
        }
    }

    /**
     * Get transcription result (may be null if still transcribing)
     * Returns: Pair(transcription, voiceAudioUrl) or null if not ready
     */
    fun getTranscriptionResult(): Pair<String, String?>? {
        return if (backgroundTranscription != null) {
            Pair(backgroundTranscription!!, backgroundVoiceAudioUrl)
        } else {
            null
        }
    }

    /**
     * Check if transcription is still in progress
     */
    fun isTranscriptionInProgress(): Boolean = isTranscribing

    /**
     * Transcribe a saved audio file
     * Use this after stopForPreview() when user accepts the recording
     */
    fun transcribeFile(
        audioFile: File,
        userId: String,
        language: String = "en",
        onTranscribed: (transcription: String, voiceAudioUrl: String?) -> Unit
    ) {
        _state.value = VoiceRecordingState.Transcribing
        transcribeAndSaveVoiceMessage(userId, audioFile, language, onTranscribed)
    }

    /**
     * Cancel recording
     */
    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecorder.cancelRecording()
        _state.value = VoiceRecordingState.Cancelled
        println("[VoiceRecording] Cancelled recording")
        resetToIdle()
    }

    /**
     * Transcribe audio file and save voice message to backend
     */
    private fun transcribeAndSaveVoiceMessage(
        userId: String,
        audioFile: File,
        language: String,
        onTranscribed: (transcription: String, voiceAudioUrl: String?) -> Unit
    ) {
        viewModelScope.launch {
            // Step 1: Transcribe audio
            api.transcribeAudio(audioFile, language).fold(
                onSuccess = { transcriptionResponse ->
                    if (transcriptionResponse.success) {
                        val transcription = transcriptionResponse.text
                        println("[VoiceRecording] Transcription successful: $transcription")

                        // Step 2: Save voice message to backend
                        viewModelScope.launch {
                            api.saveVoiceMessage(userId, audioFile, transcription, language).fold(
                                onSuccess = { voiceMessageResponse ->
                                    val voiceAudioUrl = voiceMessageResponse.conversation?.voiceAudioUrl
                                    println("[VoiceRecording] Voice message saved: $voiceAudioUrl")
                                    onTranscribed(transcription, voiceAudioUrl)
                                    _state.value = VoiceRecordingState.Idle
                                },
                                onFailure = { error ->
                                    // Still send transcription even if save fails
                                    println("[VoiceRecording] Failed to save voice message: ${error.message}")
                                    onTranscribed(transcription, null)
                                    _state.value = VoiceRecordingState.Idle
                                }
                            )
                        }
                    } else {
                        _state.value = VoiceRecordingState.Error(transcriptionResponse.error ?: "Transcription failed")
                        resetToIdle()
                    }
                },
                onFailure = { error ->
                    _state.value = VoiceRecordingState.Error(error.message ?: "Transcription failed")
                    println("[VoiceRecording] Transcription error: ${error.message}")
                    resetToIdle()
                }
            )
        }
    }

    /**
     * Start timer to update recording duration
     */
    private fun startRecordingTimer() {
        recordingJob = viewModelScope.launch {
            while (isActive) {
                delay(100) // Update every 100ms
                val currentState = _state.value
                if (currentState is VoiceRecordingState.Recording) {
                    val durationMs = System.currentTimeMillis() - recordingStartTime
                    // TODO: Get actual waveform amplitudes from AudioRecorder
                    val mockAmplitudes = currentState.waveformAmplitudes + (Math.random().toFloat() * 0.5f + 0.5f)
                    _state.value = VoiceRecordingState.Recording(
                        durationMs = durationMs,
                        waveformAmplitudes = mockAmplitudes.takeLast(50) // Keep last 50 samples
                    )
                }
            }
        }
    }

    /**
     * Reset to idle state after error or cancellation
     */
    private fun resetToIdle() {
        viewModelScope.launch {
            delay(2000)
            if (_state.value !is VoiceRecordingState.Recording) {
                _state.value = VoiceRecordingState.Idle
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingJob?.cancel()
        audioRecorder.cancelRecording()
    }
}
