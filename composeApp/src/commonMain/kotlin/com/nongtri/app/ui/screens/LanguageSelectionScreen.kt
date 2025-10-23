package com.nongtri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nongtri.app.analytics.Events
import com.nongtri.app.analytics.Funnels
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider
import com.nongtri.app.ui.components.TestTags
import java.util.Locale

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    val strings = LocalizationProvider.getStrings(selectedLanguage ?: Language.ENGLISH)

    // Track screen view time for analytics
    val screenDisplayTime = remember { System.currentTimeMillis() }

    // Detect device locale for analytics
    val deviceLocale = remember {
        try {
            Locale.getDefault().language
        } catch (e: Exception) {
            "en"
        }
    }

    // Log screen viewed event once
    LaunchedEffect(Unit) {
        val defaultSuggestion = when (deviceLocale) {
            "vi" -> "vi"
            else -> "en"
        }
        Events.logOnboardingLanguageScreenViewed(defaultSuggestion)
    }

    Surface(
        modifier = modifier.fillMaxSize().testTag(TestTags.LANGUAGE_SELECTION_SCREEN),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.appTagline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Language selection prompt
            Text(
                text = strings.selectLanguageBilingual,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Language options
            Language.entries.forEach { language ->
                LanguageOption(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = {
                        // Calculate time to decide
                        val timeToDecideMs = System.currentTimeMillis() - screenDisplayTime

                        // Check if matches device locale
                        val matchesDeviceLocale = language.code == deviceLocale

                        // Log language selected event
                        Events.logOnboardingLanguageSelected(
                            languageChosen = language.code,
                            matchesDeviceLocale = matchesDeviceLocale,
                            timeToDecideMs = timeToDecideMs
                        )

                        selectedLanguage = language
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue button
            Button(
                onClick = {
                    selectedLanguage?.let { language ->
                        // Log continue button click
                        Events.logOnboardingLanguageContinueClicked(language.code)

                        // Track onboarding funnel step 2: Language selected
                        Funnels.onboardingFunnel.step2_LanguageSelected(language.code)

                        // Log onboarding completed
                        Events.logOnboardingCompleted()

                        // Callback to parent
                        onLanguageSelected(language)
                    }
                },
                enabled = selectedLanguage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(TestTags.CONTINUE_BUTTON),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = strings.continue_,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(TestTags.languageCard(language.code)),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Country flag
                Text(
                    text = language.flag,
                    style = MaterialTheme.typography.headlineMedium
                )

                // Language name
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
