package com.nongtri.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    language: Language,
    messageContent: String,
    onDismiss: () -> Unit,
    onShareAsText: () -> Unit,
    onShareAsImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(language)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(TestTags.SHARE_BOTTOM_SHEET)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = strings.shareResponse,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Share as text
            ShareOption(
                icon = Icons.AutoMirrored.Filled.Send,
                title = strings.shareAsText,
                description = strings.shareViaMessaging,
                onClick = {
                    onShareAsText()
                    onDismiss()
                },
                testTag = TestTags.SHARE_AS_TEXT
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Share as image
            ShareOption(
                icon = Icons.Default.CameraAlt,
                title = strings.shareAsImage,
                description = strings.saveOrShareScreenshot,
                onClick = {
                    onShareAsImage()
                    onDismiss()
                },
                testTag = TestTags.SHARE_AS_IMAGE
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
