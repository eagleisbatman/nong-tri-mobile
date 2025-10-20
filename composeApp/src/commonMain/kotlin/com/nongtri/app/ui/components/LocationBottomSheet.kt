package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class UserLocation(
    val id: Int,
    val locationName: String?,
    val city: String?,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val isPrimary: Boolean,
    val source: String, // "gps" or "ip"
    val sharedAt: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    currentLocation: UserLocation?,
    savedLocations: List<UserLocation>,
    isLoading: Boolean,
    shouldShowSettings: Boolean = false,  // Show "Open Settings" instead of "Share My Location"
    onShareLocation: () -> Unit,
    onSetPrimary: (Int) -> Unit,
    onDeleteLocation: (Int) -> Unit,
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
            // Header - more compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Location Card
            CurrentLocationCard(
                location = currentLocation,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Share Current Location Button OR Open Settings button
            Button(
                onClick = onShareLocation,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = if (shouldShowSettings) Icons.Default.Settings else Icons.Default.MyLocation,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (shouldShowSettings) "Open Settings" else "Share My Location")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Locations Section
            if (savedLocations.isNotEmpty()) {
                Text(
                    text = "Saved Locations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedLocations) { location ->
                        SavedLocationCard(
                            location = location,
                            onSetPrimary = { onSetPrimary(location.id) },
                            onDelete = { onDeleteLocation(location.id) }
                        )
                    }
                }
            } else {
                // Empty state - more compact
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved locations yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Share your location for accurate weather and farming advice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    location: UserLocation?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Location",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (location != null) {
                    LocationSourceBadge(source = location.source)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detecting location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else if (location != null) {
                // Location name and city (user-friendly, no coordinates)
                Text(
                    text = buildString {
                        if (location.city != null) {
                            append(location.city)
                            if (location.country != null) {
                                append(", ")
                                append(location.country)
                            }
                        } else {
                            append("Location detected")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "No location detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SavedLocationCard(
    location: UserLocation,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (location.isPrimary) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Primary location",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = location.locationName ?: "Unnamed Location",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (location.isPrimary) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    Text(
                        text = buildString {
                            if (location.city != null) {
                                append(location.city)
                                if (location.country != null) {
                                    append(", ")
                                    append(location.country)
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!location.isPrimary) {
                            DropdownMenuItem(
                                text = { Text("Set as primary") },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                },
                                onClick = {
                                    onSetPrimary()
                                    showMenu = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }

            if (location.sharedAt != null) {
                Text(
                    text = "Shared ${formatRelativeTime(location.sharedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationSourceBadge(source: String) {
    val (icon, text, color) = when (source) {
        "gps" -> Triple(
            Icons.Default.GpsFixed,
            "GPS",
            MaterialTheme.colorScheme.primary
        )
        else -> Triple(
            Icons.Default.WifiTethering,
            "IP-based",
            MaterialTheme.colorScheme.tertiary
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatRelativeTime(isoString: String): String {
    // TODO: Implement proper relative time formatting
    // For now, just return a simple format
    return "recently"
}
