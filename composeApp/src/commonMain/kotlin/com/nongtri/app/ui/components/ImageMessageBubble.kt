package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.ui.components.TestTags

/**
 * Message bubble for user image messages (plant photos)
 * Displays image thumbnail with question text below
 * Shows loading overlay during upload/analysis
 */
@Composable
fun ImageMessageBubble(
    message: ChatMessage,
    strings: com.nongtri.app.l10n.Strings,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Match MessageBubble layout - LEFT-aligned like chat messages
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start  // LEFT-aligned for consistency
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start  // LEFT-aligned
        ) {
            // Sender label and timestamp at top
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.userLabel,  // "You" label first
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        // Image thumbnail (clickable for fullscreen)
        Card(
            onClick = {
                message.imageUrl?.let { onImageClick(it) }
            },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.IMAGE_CARD),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Image - Use AsyncImage to load images (base64 will be handled by platform-specific ImageLoader)
                if (message.imageUrl != null) {
                    // Debug: Log the image URL type
                    val imageUrl = message.imageUrl
                    println("[ImageMessageBubble] Loading image - isBase64: ${imageUrl.startsWith("data:image/")}, length: ${imageUrl.length}, prefix: ${imageUrl.take(50)}")

                    coil3.compose.SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = strings.plantImage,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            // Show placeholder on error
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.BrokenImage,
                                    contentDescription = strings.noImage,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    )
                } else {
                    // Fallback: Show placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BrokenImage,
                            contentDescription = strings.noImage,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Loading overlay during upload/analysis
                if (message.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                strings.analyzing,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Question text below image
        if (message.content.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        }
    }
}

/**
 * Format timestamp for message display
 * Note: This is a simplified version - ideally should be passed strings parameter
 */
private fun formatTimestamp(timestamp: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - timestamp

    return when {
        duration.inWholeMinutes < 1 -> "Just now"  // TODO: Use strings.justNow
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"  // TODO: Use strings.minutesAgo
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"  // TODO: Use strings.hoursAgo
        else -> {
            // Format as date
            val local = timestamp.toString()
            local.substring(0, 16).replace("T", " ")
        }
    }
}
