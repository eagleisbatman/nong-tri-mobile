package com.nongtri.app.analytics

/**
 * Funnel tracking for multi-step user journeys
 *
 * Funnels track time-to-completion and drop-off rates for key user flows.
 * Each funnel tracks the time since the funnel started for each step.
 *
 * Usage:
 * ```kotlin
 * // Start the onboarding funnel
 * Funnels.onboardingFunnel.step1_AppLaunched()
 *
 * // Track each step
 * Funnels.onboardingFunnel.step2_LanguageSelected("vi")
 * Funnels.onboardingFunnel.step3_FirstMessageSent()
 * Funnels.onboardingFunnel.step4_FirstResponseReceived(1500)
 * ```
 */
object Funnels {

    /**
     * Onboarding Funnel: App Launch → First Message → First Response
     *
     * Steps:
     * 1. app_first_launch
     * 2. onboarding_language_selected
     * 3. chat_first_message_sent
     * 4. chat_first_response_received
     *
     * Success Metric: 60%+ complete within 5 minutes
     */
    val onboardingFunnel = OnboardingFunnel()

    /**
     * Voice Feature Adoption Funnel
     *
     * Steps:
     * 1. voice_button_clicked
     * 2. voice_permission_granted
     * 3. voice_recording_started
     * 4. voice_recording_completed
     * 5. voice_message_sent
     *
     * Success Metric: 70%+ completion rate
     */
    val voiceAdoptionFunnel = VoiceAdoptionFunnel()

    /**
     * Image Diagnosis Funnel
     *
     * Steps:
     * 1. image_button_clicked
     * 2. image_permission_granted
     * 3. image_source_selected
     * 4. image_captured / image_selected_from_gallery
     * 5. diagnosis_submission_started
     * 6. diagnosis_completed
     *
     * Success Metric: 60%+ completion, <3 min processing time
     */
    val imageDiagnosisFunnel = ImageDiagnosisFunnel()
}

/**
 * Onboarding Funnel Tracker
 */
class OnboardingFunnel {
    private var funnelStartTime = 0L

    /**
     * Step 1: App launched for first time
     */
    fun step1_AppLaunched() {
        try {
            funnelStartTime = System.currentTimeMillis()

            AnalyticsService.logEvent("funnel_onboarding_started", emptyMap())

            println("[Funnel] Onboarding funnel started")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in onboarding step 1: ${e.message}")
        }
    }

    /**
     * Step 2: User selected language
     *
     * @param language The chosen language ("vi" or "en")
     */
    fun step2_LanguageSelected(language: String) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_onboarding_language", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "language" to language
            ))

            println("[Funnel] Onboarding step 2: Language selected ($language) after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in onboarding step 2: ${e.message}")
        }
    }

    /**
     * Step 3: User sent first message
     */
    fun step3_FirstMessageSent() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_onboarding_first_message", mapOf(
                "time_since_start_ms" to timeSinceStart
            ))

            println("[Funnel] Onboarding step 3: First message sent after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in onboarding step 3: ${e.message}")
        }
    }

    /**
     * Step 4: User received first response from AI (FUNNEL COMPLETE)
     *
     * @param responseTimeMs Backend response time
     */
    fun step4_FirstResponseReceived(responseTimeMs: Long) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_onboarding_completed", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "response_time_ms" to responseTimeMs,
                "completed_successfully" to true
            ))

            println("[Funnel] ✅ Onboarding funnel completed in ${timeSinceStart}ms")

            // Reset for next session (shouldn't happen, but defensive)
            funnelStartTime = 0L

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in onboarding step 4: ${e.message}")
        }
    }
}

/**
 * Voice Feature Adoption Funnel Tracker
 */
class VoiceAdoptionFunnel {
    private var funnelStartTime = 0L

    fun step1_VoiceButtonClicked() {
        try {
            funnelStartTime = System.currentTimeMillis()

            AnalyticsService.logEvent("funnel_voice_started", emptyMap())

            println("[Funnel] Voice adoption funnel started")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in voice step 1: ${e.message}")
        }
    }

    fun step2_PermissionGranted() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_voice_permission", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "granted" to true
            ))

            println("[Funnel] Voice step 2: Permission granted after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in voice step 2: ${e.message}")
        }
    }

    fun step3_RecordingStarted() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_voice_recording_started", mapOf(
                "time_since_start_ms" to timeSinceStart
            ))

            println("[Funnel] Voice step 3: Recording started after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in voice step 3: ${e.message}")
        }
    }

    fun step4_RecordingCompleted(durationMs: Long) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_voice_recording_completed", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "recording_duration_ms" to durationMs
            ))

            println("[Funnel] Voice step 4: Recording completed after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in voice step 4: ${e.message}")
        }
    }

    fun step5_MessageSent() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_voice_completed", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "completed_successfully" to true
            ))

            println("[Funnel] ✅ Voice adoption funnel completed in ${timeSinceStart}ms")

            funnelStartTime = 0L

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in voice step 5: ${e.message}")
        }
    }

    fun aborted(reason: String) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_voice_aborted", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "abort_reason" to reason
            ))

            println("[Funnel] ❌ Voice funnel aborted: $reason")

            funnelStartTime = 0L

        } catch (e: Exception) {
            println("[Funnel] ❌ Error logging voice abort: ${e.message}")
        }
    }
}

/**
 * Image Diagnosis Funnel Tracker
 */
class ImageDiagnosisFunnel {
    private var funnelStartTime = 0L

    fun step1_ImageButtonClicked() {
        try {
            funnelStartTime = System.currentTimeMillis()

            AnalyticsService.logEvent("funnel_image_diagnosis_started", emptyMap())

            println("[Funnel] Image diagnosis funnel started")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 1: ${e.message}")
        }
    }

    fun step2_PermissionGranted() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_permission", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "granted" to true
            ))

            println("[Funnel] Image step 2: Permission granted after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 2: ${e.message}")
        }
    }

    fun step3_SourceSelected(source: String) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_source_selected", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "source" to source
            ))

            println("[Funnel] Image step 3: Source selected ($source) after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 3: ${e.message}")
        }
    }

    fun step4_ImageCaptured() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_captured", mapOf(
                "time_since_start_ms" to timeSinceStart
            ))

            println("[Funnel] Image step 4: Image captured after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 4: ${e.message}")
        }
    }

    fun step5_DiagnosisSubmitted() {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_diagnosis_submitted", mapOf(
                "time_since_start_ms" to timeSinceStart
            ))

            println("[Funnel] Image step 5: Diagnosis submitted after ${timeSinceStart}ms")

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 5: ${e.message}")
        }
    }

    fun step6_DiagnosisCompleted(processingTimeMs: Long) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_diagnosis_completed", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "processing_time_ms" to processingTimeMs,
                "completed_successfully" to true
            ))

            println("[Funnel] ✅ Image diagnosis funnel completed in ${timeSinceStart}ms (processing: ${processingTimeMs}ms)")

            funnelStartTime = 0L

        } catch (e: Exception) {
            println("[Funnel] ❌ Error in image step 6: ${e.message}")
        }
    }

    fun aborted(reason: String) {
        try {
            val timeSinceStart = System.currentTimeMillis() - funnelStartTime

            AnalyticsService.logEvent("funnel_image_diagnosis_aborted", mapOf(
                "time_since_start_ms" to timeSinceStart,
                "abort_reason" to reason
            ))

            println("[Funnel] ❌ Image diagnosis funnel aborted: $reason")

            funnelStartTime = 0L

        } catch (e: Exception) {
            println("[Funnel] ❌ Error logging image abort: ${e.message}")
        }
    }
}
