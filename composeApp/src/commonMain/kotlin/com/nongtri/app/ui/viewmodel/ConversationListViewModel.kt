package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.model.ConversationThread
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.l10n.LocalizationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationListUiState(
    val threads: List<ConversationThread> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConversationListViewModel(
    private val api: NongTriApi = NongTriApi()
) : ViewModel() {

    private val userPreferences by lazy { UserPreferences.getInstance() }
    private val userId: String by lazy { userPreferences.getDeviceId() }

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    // BATCH 1: Track retry attempts for success analytics
    private var retryAttempts = 0

    init {
        loadThreads()
    }

    /**
     * Load all conversation threads for user
     */
    fun loadThreads(includeInactive: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                api.getThreads(userId, includeInactive).fold(
                    onSuccess = { threads ->
                        // BATCH 1: Track retry success if this follows failed attempts
                        if (retryAttempts > 0) {
                            com.nongtri.app.analytics.Events.logFeatureRetrySucceeded(
                                featureName = "conversation_list_load",
                                retryAttempt = retryAttempts
                            )
                            retryAttempts = 0
                        }

                        _uiState.update {
                            it.copy(
                                threads = threads,
                                isLoading = false
                            )
                        }
                        println("✓ Loaded ${threads.size} threads")
                    },
                    onFailure = { error ->
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)

                        // BATCH 1: Increment retry attempts counter
                        retryAttempts++

                        // ROUND 11: Track feature failure
                        com.nongtri.app.analytics.Events.logFeatureFailed(
                            featureName = "conversation_list_load",
                            errorType = error.message?.take(50) ?: "unknown_error"
                        )

                        _uiState.update {
                            it.copy(
                                error = error.message ?: strings.errorFailedToLoadConversations,
                                isLoading = false
                            )
                        }
                        println("⚠ Failed to load threads: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)

                // BATCH 1: Increment retry attempts counter
                retryAttempts++

                // ROUND 11: Track feature failure
                com.nongtri.app.analytics.Events.logFeatureFailed(
                    featureName = "conversation_list_load",
                    errorType = e.message?.take(50) ?: "unknown_error"
                )

                _uiState.update {
                    it.copy(
                        error = e.message ?: strings.errorUnknown,
                        isLoading = false
                    )
                }
                println("⚠ Error loading threads: ${e.message}")
            }
        }
    }

    /**
     * Create a new conversation thread
     */
    fun createNewThread(title: String? = null, onSuccess: (ConversationThread) -> Unit) {
        viewModelScope.launch {
            try {
                api.createThread(userId, title).fold(
                    onSuccess = { thread ->
                        println("✓ Created new thread: ${thread.id}")
                        // Reload threads to include the new one
                        loadThreads()
                        onSuccess(thread)
                    },
                    onFailure = { error ->
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                        _uiState.update {
                            it.copy(error = error.message ?: strings.errorFailedToCreateConversation)
                        }
                        println("⚠ Failed to create thread: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _uiState.update {
                    it.copy(error = e.message ?: strings.errorUnknown)
                }
                println("⚠ Error creating thread: ${e.message}")
            }
        }
    }

    /**
     * Delete a thread
     */
    fun deleteThread(threadId: Int) {
        viewModelScope.launch {
            try {
                api.deleteThread(userId, threadId).fold(
                    onSuccess = {
                        println("✓ Deleted thread: $threadId")
                        // Reload threads to update the list
                        loadThreads()
                    },
                    onFailure = { error ->
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                        _uiState.update {
                            it.copy(error = error.message ?: strings.errorFailedToDeleteConversation)
                        }
                        println("⚠ Failed to delete thread: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _uiState.update {
                    it.copy(error = e.message ?: strings.errorUnknown)
                }
                println("⚠ Error deleting thread: ${e.message}")
            }
        }
    }

    /**
     * Archive a thread (mark as inactive)
     */
    fun archiveThread(threadId: Int) {
        viewModelScope.launch {
            try {
                api.updateThread(userId, threadId, isActive = false).fold(
                    onSuccess = {
                        println("✓ Archived thread: $threadId")
                        loadThreads()
                    },
                    onFailure = { error ->
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                        _uiState.update {
                            it.copy(error = error.message ?: strings.errorFailedToArchiveConversation)
                        }
                        println("⚠ Failed to archive thread: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _uiState.update {
                    it.copy(error = e.message ?: strings.errorUnknown)
                }
                println("⚠ Error archiving thread: ${e.message}")
            }
        }
    }
}
