package com.nongtri.app

import androidx.compose.runtime.*
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.ui.screens.ChatScreen
import com.nongtri.app.ui.screens.LanguageSelectionScreen
import com.nongtri.app.ui.theme.NongTriTheme
import com.nongtri.app.ui.viewmodel.ChatViewModel

@Composable
fun App() {
    val userPreferences = remember { UserPreferences.getInstance() }
    val hasCompletedOnboarding by userPreferences.hasCompletedOnboarding.collectAsState()
    val selectedLanguage by userPreferences.language.collectAsState()

    NongTriTheme {
        if (!hasCompletedOnboarding) {
            // First launch - show language selection
            LanguageSelectionScreen(
                onLanguageSelected = { language ->
                    userPreferences.setLanguage(language)
                    userPreferences.completeOnboarding()
                }
            )
        } else {
            // After onboarding - show chat
            val chatViewModel = remember { ChatViewModel() }
            ChatScreen(
                viewModel = chatViewModel,
                language = selectedLanguage,
                onLanguageChange = { language ->
                    userPreferences.setLanguage(language)
                },
                onClearHistory = {
                    chatViewModel.clearHistory()
                }
            )
        }
    }
}
