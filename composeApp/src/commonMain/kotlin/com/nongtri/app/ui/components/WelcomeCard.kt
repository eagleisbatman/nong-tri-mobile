package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.Strings
import com.nongtri.app.ui.theme.NongTriColors
import kotlinx.coroutines.launch

@Composable
fun WelcomeCard(
    strings: Strings,
    language: Language,
    deviceId: String,
    locationName: String? = null,
    onStarterQuestionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var starterQuestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingQuestions by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val api = remember { NongTriApi() }

    LaunchedEffect(language, deviceId) {
        scope.launch {
            isLoadingQuestions = true
            println("[WelcomeCard] Fetching starter questions for deviceId: $deviceId, language: $language")
            val result = api.getStarterQuestions(
                language = if (language == Language.VIETNAMESE) "vi" else "en",
                deviceId = deviceId
            )
            result.onSuccess { questions ->
                println("[WelcomeCard] Successfully fetched ${questions.size} starter questions: $questions")
                starterQuestions = questions
                isLoadingQuestions = false
            }.onFailure { error ->
                println("[WelcomeCard] Failed to fetch starter questions: ${error.message}")
                error.printStackTrace()
                isLoadingQuestions = false
            }
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Welcome greeting card with improved design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag(TestTags.WELCOME_CARD),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NongTriColors.Primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with background
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NongTriColors.Primary.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🌾",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Welcome message with better typography
                    Text(
                        text = strings.welcomeMessage,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Description with improved spacing
                    Text(
                        text = strings.welcomeDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.6,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Starter Questions or Loading Skeleton below the card
            if (isLoadingQuestions) {
                // Show loading skeleton
                StarterQuestionsLoadingSkeleton(
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else if (starterQuestions.isNotEmpty()) {
                StarterQuestions(
                    questions = starterQuestions,
                    language = language,
                    onQuestionClick = onStarterQuestionClick,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StarterQuestionsLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title with pulsing effect
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "💡",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Building your experience...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Skeleton question cards
        repeat(4) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Placeholder text with varying widths
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (it % 2 == 0) 0.8f else 0.6f)
                            .height(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}
