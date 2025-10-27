package com.nongtri.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.preferences.ThemeMode
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.data.repository.TranslationRepository
import com.nongtri.app.l10n.LocalizationProvider
import com.nongtri.app.ui.screens.ChatScreen
import com.nongtri.app.ui.screens.ConversationListScreen
import com.nongtri.app.ui.screens.LanguageSelectionScreen
import com.nongtri.app.ui.theme.NongTriTheme
import com.nongtri.app.ui.viewmodel.ChatViewModel
import com.nongtri.app.ui.viewmodel.ConversationListViewModel
import kotlinx.coroutines.launch

@Composable
fun App() {
    val userPreferences = remember { UserPreferences.getInstance() }
    val hasCompletedOnboarding by userPreferences.hasCompletedOnboarding.collectAsState()
    val selectedLanguage by userPreferences.language.collectAsState()
    val themeMode by userPreferences.themeMode.collectAsState()

    // Translation repository for loading translations from API
    val translationRepository = remember {
        TranslationRepository(
            api = NongTriApi.getInstance(),
            userPreferences = userPreferences
        )
    }

    // Load translations on app start and when language changes
    // Uses version checking to only fetch if translations were updated
    LaunchedEffect(selectedLanguage) {
        launch {
            try {
                println("🌐 Checking translations for ${selectedLanguage.displayName}...")
                val translations = translationRepository.getTranslationsWithVersionCheck(selectedLanguage.code)

                if (translations.isNotEmpty()) {
                    // Update LocalizationProvider with API translations
                    LocalizationProvider.setApiTranslations(selectedLanguage, translations)
                    println("✅ Loaded ${translations.size} translations for ${selectedLanguage.displayName}")
                } else {
                    println("⚠️ No translations loaded, using hardcoded fallbacks")
                }
            } catch (e: Exception) {
                println("❌ Failed to load translations: ${e.message}")
                println("⚠️ Using hardcoded fallback translations")
            }
        }
    }

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
            val hapticFeedback = com.nongtri.app.platform.LocalHapticFeedback.current
            val chatViewModel = remember { ChatViewModel(hapticFeedback = hapticFeedback) }
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
                        println("[App] Language changed to: $language")
                        userPreferences.setLanguage(language)
                        // CRITICAL: Clear conversation when language changes
                        // Don't create new thread yet - wait until user sends first message
                        // This prevents empty conversations from accumulating
                        println("[App] Clearing conversation after language change")
                        chatViewModel.clearHistory()
                    },
                    onClearHistory = {
                        chatViewModel.clearHistory()
                    },
                    onViewConversations = {
                        conversationListViewModel.loadThreads()
                        showConversationList = true
                    },
                    onThemeModeChange = { mode ->
                        // ROUND 10: Track theme change
                        com.nongtri.app.analytics.Events.logThemeChanged(
                            fromTheme = themeMode.name.lowercase(),
                            toTheme = mode.name.lowercase()
                        )
                        userPreferences.setThemeMode(mode)
                    },
                    currentThemeMode = themeMode
                )
            }
        }
    }
}
