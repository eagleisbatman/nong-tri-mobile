package com.nongtri.app.ui.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordView

/**
 * WhatsApp-style voice recording component using RecordView library
 *
 * This wraps the RecordView library (https://github.com/3llomi/RecordView) which provides:
 * - Hold-to-record gesture
 * - Slide-to-cancel with animation
 * - Lock feature for longer recordings
 * - Visual timer and waveform
 *
 * The library handles ALL the complex gesture detection, animations, and UI feedback.
 * We just need to implement the actual audio recording logic in the callbacks.
 *
 * @param onRecordingStart Called when user starts recording (after long-press threshold)
 * @param onRecordingFinish Called when recording completes successfully (returns duration in ms)
 * @param onRecordingCancel Called when recording is cancelled (slide away or too short)
 * @param onRecordingLocked Called when recording is locked for hands-free recording
 * @param modifier Modifier for the container
 */
@Composable
fun WhatsAppRecordButton(
    onRecordingStart: () -> Unit,
    onRecordingFinish: (recordTime: Long) -> Unit,
    onRecordingCancel: () -> Unit,
    onRecordingLocked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            // Create container layout
            val container = FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Create RecordView (shows slide-to-cancel UI during recording)
            val recordView = RecordView(ctx).apply {
                id = View.generateViewId()
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // Customize appearance
                setSlideToCancelText("← Slide to cancel")
                setCounterTimeColor(android.graphics.Color.BLACK)
                setSoundEnabled(false) // Disable beep sounds
                setLessThanSecondAllowed(false) // Reject < 1 second recordings
                setCancelBounds(8f) // Smaller cancel threshold

                // Set up recording callbacks
                setOnRecordListener(object : OnRecordListener {
                    override fun onStart() {
                        println("[RecordButton] Recording started")
                        onRecordingStart()
                    }

                    override fun onCancel() {
                        println("[RecordButton] Recording cancelled")
                        onRecordingCancel()
                    }

                    override fun onFinish(recordTime: Long, limitReached: Boolean) {
                        println("[RecordButton] Recording finished: ${recordTime}ms (limit: $limitReached)")
                        onRecordingFinish(recordTime)
                    }

                    override fun onLessThanSecond() {
                        println("[RecordButton] Recording too short (< 1 second)")
                        onRecordingCancel()
                    }

                    override fun onLock() {
                        println("[RecordButton] Recording locked")
                        onRecordingLocked()
                    }
                })

                setOnBasketAnimationEndListener {
                    println("[RecordButton] Cancel animation ended")
                }
            }

            // Create RecordButton (mic icon button)
            val recordButton = RecordButton(ctx)
            recordButton.id = View.generateViewId()
            recordButton.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            }
            recordButton.setImageResource(android.R.drawable.ic_btn_speak_now)

            // Add views to container
            container.addView(recordView)
            container.addView(recordButton as android.view.View)

            // Link RecordButton to RecordView (CRITICAL - must be done AFTER adding to container)
            // This makes the hold-to-record gestures work
            recordButton.setListenForRecord(true)
            recordButton.setRecordView(recordView)

            container
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}
