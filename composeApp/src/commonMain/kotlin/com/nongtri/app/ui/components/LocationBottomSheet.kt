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
                // Use geo-level hierarchy with fallback to legacy fields
                // Display format: "City, Country" or "Region, Country" or "Country"
                val locationText = buildString {
                    // Try geo-levels first (new approach)
                    val level3 = location.geoLevel3  // City/Locality
                    val level2 = location.geoLevel2  // Region/State
                    val level1 = location.geoLevel1  // Country

                    // Fallback to legacy fields if geo-levels not available
                    val city = level3 ?: location.city
                    val region = level2 ?: location.region
                    val country = level1 ?: location.country

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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    // Location object exists but has no usable data
                    Text(
                        text = "Unable to determine location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
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
