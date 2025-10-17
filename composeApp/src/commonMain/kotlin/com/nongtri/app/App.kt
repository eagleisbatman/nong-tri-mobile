package com.nongtri.app

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.l10n.Language
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
        AnimatedContent(
            targetState = hasCompletedOnboarding,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it } togetherWith
                        fadeOut() + slideOutHorizontally { -it }
            }
        ) { completed ->
            if (!completed) {
                LanguageSelectionScreen(
                    onLanguageSelected = { language ->
                        userPreferences.setLanguage(language)
                        userPreferences.completeOnboarding()
                    }
                )
            } else {
                val chatViewModel: ChatViewModel = viewModel { ChatViewModel() }
                ChatScreen(
                    viewModel = chatViewModel,
                    language = selectedLanguage,
                    onProfileClick = {
                        // TODO: Navigate to profile screen
                    }
                )
            }
        }
    }
}
