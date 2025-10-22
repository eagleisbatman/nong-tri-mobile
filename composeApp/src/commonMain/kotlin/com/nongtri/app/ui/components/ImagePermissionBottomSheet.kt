package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePermissionBottomSheet(
    hasCameraPermission: Boolean = false,
    hasStoragePermission: Boolean = false,
    shouldShowSettings: Boolean = false,  // Show "Open Settings" instead of "Grant Permission"
    onRequestCameraPermission: () -> Unit,
    onRequestStoragePermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
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
                    text = "Camera & Photo Permissions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Camera Permission Card
            PermissionCard(
                title = "Camera Access",
                description = if (shouldShowSettings && !hasCameraPermission) {
                    "Camera permission is required to capture plant photos. Please enable it in Settings."
                } else if (hasCameraPermission) {
                    "Camera access granted ✓"
                } else {
                    "Allow camera access to capture photos of your plants for AI diagnosis."
                },
                icon = if (hasCameraPermission) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                isGranted = hasCameraPermission,
                shouldShowSettings = shouldShowSettings && !hasCameraPermission,
                onRequestPermission = onRequestCameraPermission
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Storage Permission Card
            PermissionCard(
                title = "Photo Library Access",
                description = if (shouldShowSettings && !hasStoragePermission) {
                    "Photo library permission is required to select existing images. Please enable it in Settings."
                } else if (hasStoragePermission) {
                    "Photo library access granted ✓"
                } else {
                    "Allow photo library access to select existing plant images for diagnosis."
                },
                icon = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.PhotoLibrary,
                isGranted = hasStoragePermission,
                shouldShowSettings = shouldShowSettings && !hasStoragePermission,
                onRequestPermission = onRequestStoragePermission
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info text
            Text(
                text = "Images are sent to our AI for plant health diagnosis and treatment recommendations.",
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
                    modifier = Modifier.fillMaxWidth(),
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
                    Text(if (shouldShowSettings) "Open Settings" else "Grant Permission")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permission granted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
