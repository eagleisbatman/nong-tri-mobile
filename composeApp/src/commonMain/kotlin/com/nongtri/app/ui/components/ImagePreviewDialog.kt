package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.nongtri.app.ui.components.TestTags
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider

/**
 * Dialog for previewing selected image before sending for diagnosis
 * Shows image preview with question input field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewDialog(
    language: Language,
    imageUri: String,
    onDismiss: () -> Unit,
    onConfirm: (question: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(language)
    var question by remember { mutableStateOf(strings.defaultPlantQuestion) }
    val initialQuestion = remember { strings.defaultPlantQuestion }

    // ROUND 6 TODO: Track image preview displayed
    // Requires: fileSizeKb, imageWidth, imageHeight
    // Need to extract image metadata from imageUri
    // LaunchedEffect(Unit) {
    //     com.nongtri.app.analytics.Events.logImagePreviewDisplayed(fileSizeKb, width, height)
    // }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag(TestTags.IMAGE_PREVIEW_DIALOG),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with close button
            TopAppBar(
                title = {
                    Text(
                        strings.confirmImage,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = strings.cdCancel,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Image preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = strings.cdSelectedPlantImage,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Question input and send button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = strings.askQuestionAboutPlant,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question,
                        onValueChange = { newValue ->
                            // ROUND 6: Track question edited with both lengths
                            if (newValue != question && newValue != initialQuestion) {
                                com.nongtri.app.analytics.Events.logImageQuestionEdited(
                                    originalLength = question.length,
                                    newLength = newValue.length
                                )
                            }
                            question = newValue
                        },
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.QUESTION_INPUT),
                        placeholder = { Text(strings.defaultPlantQuestion) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).testTag(TestTags.CANCEL_BUTTON)
                        ) {
                            Text(strings.cancel)
                        }

                        Button(
                            onClick = {
                                val finalQuestion = question.ifBlank { strings.defaultPlantQuestion }
                                onConfirm(finalQuestion)
                            },
                            modifier = Modifier.weight(1f).testTag(TestTags.SEND_DIAGNOSIS_BUTTON),
                            enabled = question.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.sendForDiagnosis)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = strings.diagnosisInfoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
