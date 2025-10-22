package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePermissionBottomSheet(
    hasCameraPermission: Boolean = false,
    hasStoragePermission: Boolean = false,
    shouldShowSettings: Boolean = false,  // Show "Open Settings" instead of "Grant Permission"
    onRequestCameraPermission: () -> Unit,
    onRequestStoragePermission: () -> Unit,
    onDismiss: () -> Unit,
    language: Language,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(language)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(TestTags.IMAGE_PERMISSION_SHEET),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.cameraPhotoPermissions,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(TestTags.CLOSE_BUTTON)
                ) {
                    Icon(Icons.Default.Close, contentDescription = strings.cdClose)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Camera Permission Card
            PermissionCard(
                title = strings.cameraAccess,
                description = if (shouldShowSettings && !hasCameraPermission) {
                    strings.cameraPermissionSettingsPrompt
                } else if (hasCameraPermission) {
                    strings.cameraPermissionGranted
                } else {
                    strings.cameraPermissionPrompt
                },
                icon = if (hasCameraPermission) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                isGranted = hasCameraPermission,
                shouldShowSettings = shouldShowSettings && !hasCameraPermission,
                onRequestPermission = onRequestCameraPermission,
                strings = strings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Storage Permission Card
            PermissionCard(
                title = strings.photoLibraryAccess,
                description = if (shouldShowSettings && !hasStoragePermission) {
                    strings.photoLibraryPermissionSettingsPrompt
                } else if (hasStoragePermission) {
                    strings.photoLibraryPermissionGranted
                } else {
                    strings.photoLibraryPermissionPrompt
                },
                icon = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.PhotoLibrary,
                isGranted = hasStoragePermission,
                shouldShowSettings = shouldShowSettings && !hasStoragePermission,
                onRequestPermission = onRequestStoragePermission,
                strings = strings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info text
            Text(
                text = strings.imageUploadInfoText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    shouldShowSettings: Boolean,
    onRequestPermission: () -> Unit,
    strings: com.nongtri.app.l10n.Strings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth().testTag(
                        if (title.contains("Camera")) TestTags.GRANT_CAMERA_BUTTON
                        else TestTags.GRANT_PERMISSION_BUTTON
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (shouldShowSettings) Icons.Default.Settings else icon,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (shouldShowSettings) strings.openSettings else strings.grantPermission)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = strings.cdClose,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.permissionGrantedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
