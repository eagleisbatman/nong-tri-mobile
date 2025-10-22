package com.nongtri.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.nongtri.app.data.preferences.ThemeMode
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.ui.screens.ChatScreen
import com.nongtri.app.ui.screens.ConversationListScreen
import com.nongtri.app.ui.screens.LanguageSelectionScreen
import com.nongtri.app.ui.theme.NongTriTheme
import com.nongtri.app.ui.viewmodel.ChatViewModel
import com.nongtri.app.ui.viewmodel.ConversationListViewModel

@Composable
fun App() {
    val userPreferences = remember { UserPreferences.getInstance() }
    val hasCompletedOnboarding by userPreferences.hasCompletedOnboarding.collectAsState()
    val selectedLanguage by userPreferences.language.collectAsState()
    val themeMode by userPreferences.themeMode.collectAsState()

    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    NongTriTheme(darkTheme = darkTheme) {
        if (!hasCompletedOnboarding) {
            // First launch - show language selection
            LanguageSelectionScreen(
                onLanguageSelected = { language ->
                    userPreferences.setLanguage(language)
                    userPreferences.completeOnboarding()
                }
            )
        } else {
            // After onboarding - navigation between chat and conversation list
            var showConversationList by remember { mutableStateOf(false) }
            val chatViewModel = remember { ChatViewModel() }
            val conversationListViewModel = remember { ConversationListViewModel() }

            if (showConversationList) {
                ConversationListScreen(
                    viewModel = conversationListViewModel,
                    onThreadSelected = { threadId, threadTitle ->
                        chatViewModel.switchToThread(threadId, threadTitle)
                        showConversationList = false
                    },
                    onNavigateBack = {
                        showConversationList = false
                    },
                    onNewConversation = { threadId ->
                        // Thread already switched in ViewModel
                        showConversationList = false
                    },
                    language = selectedLanguage
                )
            } else {
                ChatScreen(
                    viewModel = chatViewModel,
                    language = selectedLanguage,
                    onLanguageChange = { language ->
                        userPreferences.setLanguage(language)
                    },
                    onClearHistory = {
                        chatViewModel.clearHistory()
                    },
                    onViewConversations = {
                        conversationListViewModel.loadThreads()
                        showConversationList = true
                    },
                    onThemeModeChange = { mode ->
                        userPreferences.setThemeMode(mode)
                    },
                    currentThemeMode = themeMode
                )
            }
        }
    }
}
