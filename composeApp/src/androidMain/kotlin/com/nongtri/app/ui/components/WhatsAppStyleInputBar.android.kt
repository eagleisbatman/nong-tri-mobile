package com.nongtri.app.ui.components

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordView
import com.nongtri.app.l10n.Strings

/**
 * Android-specific WhatsApp-style input bar with RecordView library integration
 *
 * This replaces the broken custom gesture handling with the battle-tested RecordView library.
 */
@Composable
actual fun PlatformVoiceButton(
    onRecordingStart: () -> Unit,
    onRecordingFinish: () -> Unit,
    onRecordingCancel: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current

    // RecordView needs to be in a container that allows it to show the slide-to-cancel UI
    // We'll use AndroidView to embed both RecordView and RecordButton
    Box(modifier = modifier.size(48.dp)) {
        AndroidView(
            factory = { ctx ->
                // Create container
                val container = FrameLayout(ctx)

                // Create RecordView (slide-to-cancel UI overlay)
                val recordView = RecordView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Customize appearance
                    setSlideToCancelText("← Slide to cancel")
                    setCounterTimeColor(android.graphics.Color.BLACK)
                    setSoundEnabled(false)
                    setLessThanSecondAllowed(false)
                    setCancelBounds(8f)

                    // Set callbacks
                    setOnRecordListener(object : OnRecordListener {
                        override fun onStart() {
                            println("[RecordView] Recording started")
                            onRecordingStart()
                        }

                        override fun onCancel() {
                            println("[RecordView] Recording cancelled")
                            onRecordingCancel()
                        }

                        override fun onFinish(recordTime: Long, limitReached: Boolean) {
                            println("[RecordView] Recording finished: ${recordTime}ms")
                            onRecordingFinish()
                        }

                        override fun onLessThanSecond() {
                            println("[RecordView] Too short (< 1s)")
                            onRecordingCancel()
                        }

                        override fun onLock() {
                            println("[RecordView] Recording locked")
                        }
                    })
                }

                // Create RecordButton (mic icon)
                val recordButton = RecordButton(ctx)
                recordButton.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
                recordButton.setImageResource(android.R.drawable.ic_btn_speak_now)
                recordButton.isEnabled = isEnabled

                // Add views to container
                container.addView(recordView)
                container.addView(recordButton as View)

                // Link them together (CRITICAL)
                recordButton.setListenForRecord(true)
                recordButton.setRecordView(recordView)

                container
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
