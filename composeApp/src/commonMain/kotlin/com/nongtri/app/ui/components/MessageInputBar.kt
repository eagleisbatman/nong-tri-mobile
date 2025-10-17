package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings
import com.nongtri.app.ui.theme.NongTriColors

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    strings: Strings,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hasText = value.isNotEmpty()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.INPUT_AREA),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text input field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.TEXT_FIELD),
                    enabled = isEnabled,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = SolidColor(NongTriColors.Primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = strings.typeMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Send button with animation
            AnimatedContent(
                targetState = hasText,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                }
            ) { showSend ->
                if (showSend) {
                    SendButton(
                        onClick = onSend,
                        enabled = isEnabled && hasText,
                        modifier = Modifier.testTag(TestTags.SEND_BUTTON)
                    )
                } else {
                    // Placeholder for voice/image buttons (to be added later)
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun SendButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = if (enabled) NongTriColors.Primary else MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Text(
            text = "➤",
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
