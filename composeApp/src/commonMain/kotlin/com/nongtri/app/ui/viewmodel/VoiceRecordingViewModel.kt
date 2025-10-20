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
