package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val latitude: Double,
    val longitude: Double,
    val isPrimary: Boolean,
    val source: String, // "gps" or "ip"
    val sharedAt: String?,
    // Geo-level hierarchy (universal, works for any country)
    val geoLevel1: String? = null,  // Country
    val geoLevel2: String? = null,  // Region/State
    val geoLevel3: String? = null,  // City/Locality
    // Legacy fields (for backward compatibility, will be removed later)
    val city: String? = null,
    val region: String? = null,
    val country: String? = null
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
    modifier: Modifier = Modifier,
    // New parameters for dual location display
    ipLocation: UserLocation? = null,
    gpsLocation: UserLocation? = null
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

            // IP Location Card (always shown if available)
            if (ipLocation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Detected Location (IP)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val locationText = buildString {
                            val level3 = ipLocation.geoLevel3
                            val level2 = ipLocation.geoLevel2
                            val level1 = ipLocation.geoLevel1

                            val city = level3 ?: ipLocation.city
                            val region = level2 ?: ipLocation.region
                            val country = level1 ?: ipLocation.country

                            when {
                                city != null && city != "null" -> {
                                    append(city)
                                    if (country != null && country != "null") {
                                        append(", ")
                                        append(country)
                                    }
                                }
                                region != null && region != "null" -> {
                                    append(region)
                                    if (country != null && country != "null") {
                                        append(", ")
                                        append(country)
                                    }
                                }
                                country != null && country != "null" -> {
                                    append(country)
                                }
                                else -> {
                                    append("")
                                }
                            }
                        }

                        if (locationText.isNotEmpty()) {
                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Unable to determine location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // GPS Location Card OR Share Location Button
            if (gpsLocation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "My Shared Location (GPS)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val locationText = buildString {
                            val level3 = gpsLocation.geoLevel3
                            val level2 = gpsLocation.geoLevel2
                            val level1 = gpsLocation.geoLevel1

                            val city = level3 ?: gpsLocation.city
                            val region = level2 ?: gpsLocation.region
                            val country = level1 ?: gpsLocation.country

                            when {
                                city != null && city != "null" -> {
                                    append(city)
                                    if (country != null && country != "null") {
                                        append(", ")
                                        append(country)
                                    }
                                }
                                region != null && region != "null" -> {
                                    append(region)
                                    if (country != null && country != "null") {
                                        append(", ")
                                        append(country)
                                    }
                                }
                                country != null && country != "null" -> {
                                    append(country)
                                }
                                else -> {
                                    append("")
                                }
                            }
                        }

                        if (locationText.isNotEmpty()) {
                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "Unable to determine location",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Update Location Button - Prominent green button
                        Button(
                            onClick = onShareLocation,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Location")
                        }
                    }
                }
            } else {
                // Share Location Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Share GPS Location",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Share your precise location for more accurate weather forecasts and farming advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onShareLocation,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (shouldShowSettings) Icons.Default.Settings else Icons.Default.MyLocation,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (shouldShowSettings) "Open Settings" else "Share My Location")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info text about location usage
            Text(
                text = "Your location helps provide accurate weather forecasts and farming advice for your area.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

