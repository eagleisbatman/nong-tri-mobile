package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StarterQuestions(
    questions: List<String>,
    language: Language,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(language)

    // ROUND 10: Track starter questions displayed
    LaunchedEffect(questions.size) {
        if (questions.isNotEmpty()) {
            com.nongtri.app.analytics.Events.logStarterQuestionsDisplayed(
                count = questions.size
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Section header with icon
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "💡",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (language == Language.VIETNAMESE) {
                    strings.starterQuestionsHeaderVi
                } else {
                    strings.starterQuestionsHeaderEn
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            questions.forEachIndexed { index, question ->
                SuggestionChip(
                    onClick = {
                        // ROUND 10: Track starter question clicked
                        com.nongtri.app.analytics.Events.logStarterQuestionClicked(
                            questionIndex = index,
                            questionText = question
                        )
                        onQuestionClick(question)
                    },
                    label = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(start = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.starterQuestion(index))
                )
            }
        }
    }
}
