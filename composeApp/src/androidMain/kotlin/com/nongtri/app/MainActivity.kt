package com.nongtri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.nongtri.app.platform.LocalShareManager
import com.nongtri.app.platform.LocalTextToSpeechManager
import com.nongtri.app.platform.ShareManager
import com.nongtri.app.platform.TextToSpeechManager

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TextToSpeechManager(applicationContext)

        setContent {
            CompositionLocalProvider(
                LocalShareManager provides ShareManager(applicationContext),
                LocalTextToSpeechManager provides ttsManager
            ) {
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
