package com.nongtri.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nongtri.app.ui.ChatScreen
import com.nongtri.app.ui.ChatViewModel

@Composable
fun App() {
    MaterialTheme {
        val viewModel: ChatViewModel = viewModel { ChatViewModel() }
        ChatScreen(viewModel = viewModel)
    }
}
