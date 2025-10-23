package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.l10n.LocalizationProvider
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

    private val userPreferences by lazy { UserPreferences.getInstance() }

    private val _state = MutableStateFlow<VoiceRecordingState>(VoiceRecordingState.Idle)
    val state: StateFlow<VoiceRecordingState> = _state.asStateFlow()

    // Real-time amplitude for waveform visualization (0-32767)
    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    // Background transcription state (reactive for UI)
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _transcriptionText = MutableStateFlow<String?>(null)
    val transcriptionText: StateFlow<String?> = _transcriptionText.asStateFlow()

    private var recordingJob: Job? = null
    private var amplitudeJob: Job? = null
    private var recordingStartTime: Long = 0
    private var lastRecordingDurationMs: Long = 0  // Store duration for analytics

    // Background transcription internal state
    private var backgroundTranscription: String? = null
    private var backgroundVoiceAudioUrl: String? = null

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
                startAmplitudePolling()
                println("[VoiceRecording] Started recording: $filePath")

                // Track voice funnel step 3: Recording started
                com.nongtri.app.analytics.Funnels.voiceAdoptionFunnel.step3_RecordingStarted()
            },
            onFailure = { error ->
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _state.value = VoiceRecordingState.Error(error.message ?: strings.errorFailedToStartRecording)
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
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                val durationMs = System.currentTimeMillis() - recordingStartTime
                lastRecordingDurationMs = durationMs  // Store for analytics
                if (durationMs < 500) {
                    _state.value = VoiceRecordingState.Error(strings.errorRecordingTooShort)
                    println("[VoiceRecording] Recording too short: ${durationMs}ms")
                    audioFile.delete()
                    resetToIdle()
                    return
                }

                // ✅ VALIDATION #2: Check file size (minimum 1KB)
                if (audioFile.length() < 1000) {
                    _state.value = VoiceRecordingState.Error(strings.errorRecordingEmpty)
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
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _state.value = VoiceRecordingState.Error(error.message ?: strings.errorFailedToStopRecording)
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
        stopAmplitudePolling()

        var savedFile: File? = null
        audioRecorder.stopRecording().fold(
            onSuccess = { audioFile ->
                val durationMs = System.currentTimeMillis() - recordingStartTime
                lastRecordingDurationMs = durationMs  // Store for analytics
                println("[VoiceRecording] Stopped for preview: ${audioFile.absolutePath} (${audioFile.length()} bytes, ${durationMs}ms)")

                // Track voice funnel step 4: Recording completed
                com.nongtri.app.analytics.Funnels.voiceAdoptionFunnel.step4_RecordingCompleted(durationMs)

                // ALWAYS return the file for preview - let user decide whether to accept/reject
                // Don't validate here - user should see the preview UI regardless
                savedFile = audioFile
                _state.value = VoiceRecordingState.Idle

                // ✅ START TRANSCRIPTION IN BACKGROUND IMMEDIATELY
                println("[VoiceRecording] Starting background transcription...")
                startBackgroundTranscription(userId, audioFile, language)
            },
            onFailure = { error ->
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _state.value = VoiceRecordingState.Error(error.message ?: strings.errorFailedToStopRecording)
                println("[VoiceRecording] Error stopping recording: ${error.message}")
                error.printStackTrace()
                resetToIdle()
            }
        )

        return savedFile
    }

    /**
     * Start transcription in background (non-blocking)
     */
    private fun startBackgroundTranscription(userId: String, audioFile: File, language: String) {
        _isTranscribing.value = true
        _transcriptionText.value = null
        backgroundTranscription = null
        backgroundVoiceAudioUrl = null

        println("[VoiceRecording] Background transcription started")
        // transcribeAndSaveVoiceMessage already launches its own coroutine
        transcribeAndSaveVoiceMessage(userId, audioFile, language) { transcription, voiceAudioUrl ->
            backgroundTranscription = transcription
            backgroundVoiceAudioUrl = voiceAudioUrl
            _isTranscribing.value = false
            _transcriptionText.value = transcription  // Update reactive state for UI
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
    fun isTranscriptionInProgress(): Boolean = _isTranscribing.value

    /**
     * Get last recording duration in milliseconds (for analytics)
     */
    fun getLastRecordingDurationMs(): Long = lastRecordingDurationMs

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
        stopAmplitudePolling()
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
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                        _state.value = VoiceRecordingState.Error(transcriptionResponse.error ?: strings.errorTranscriptionFailed)
                        resetToIdle()
                    }
                },
                onFailure = { error ->
                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                    _state.value = VoiceRecordingState.Error(error.message ?: strings.errorTranscriptionFailed)
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
     * Poll audio amplitude for real-time waveform visualization
     */
    private fun startAmplitudePolling() {
        println("[VoiceRecording] Starting amplitude polling...")
        amplitudeJob = viewModelScope.launch {
            var sampleCount = 0
            while (isActive && audioRecorder.isRecording()) {
                val amplitude = audioRecorder.getMaxAmplitude()
                _amplitude.value = amplitude

                // Log every 10th sample to avoid spam
                if (sampleCount % 10 == 0) {
                    println("[VoiceRecording] Amplitude: $amplitude (isRecording: ${audioRecorder.isRecording()})")
                }
                sampleCount++

                delay(50) // Poll every 50ms for smooth visualization
            }
            println("[VoiceRecording] Amplitude polling stopped")
            _amplitude.value = 0 // Reset when stopped
        }
    }

    /**
     * Stop amplitude polling
     */
    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }

    /**
     * Reset to idle state after error or cancellation
     */
    private fun resetToIdle() {
        viewModelScope.launch {
            // Reset transcription state immediately
            _isTranscribing.value = false
            _transcriptionText.value = null

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
