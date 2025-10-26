package com.nongtri.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Informative card shown when plant diagnosis is being processed asynchronously
 * Explains to the farmer what's happening and when they'll get results
 */
@Composable
fun DiagnosisPendingCard(
    imageUrl: String,
    jobId: String,
    strings: com.nongtri.app.l10n.Strings,
    modifier: Modifier = Modifier
) {
    // ROUND 7: Track pending card viewed
    LaunchedEffect(jobId) {
        com.nongtri.app.analytics.Events.logDiagnosisPendingCardViewed(
            jobId = jobId
        )
    }

    // Simple loading indicator - no background, no large image
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = strings.analyzingYourCrop,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = strings.notificationWhenReady,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
