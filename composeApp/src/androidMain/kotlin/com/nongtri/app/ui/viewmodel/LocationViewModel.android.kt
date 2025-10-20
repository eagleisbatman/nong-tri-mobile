package com.nongtri.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.nongtri.app.MainActivity
import com.nongtri.app.data.repository.LocationRepository
import com.nongtri.app.ui.components.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

actual class LocationViewModel actual constructor() : ViewModel() {
    private val _locationState = MutableStateFlow(LocationState())
    actual val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val locationRepository = LocationRepository.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var context: Context

    companion object {
        var permissionLauncher: ((Array<String>) -> Unit)? = null
        var permissionResultCallback: ((Boolean) -> Unit)? = null
    }

    fun initialize(context: Context) {
        this.context = context
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        loadCurrentLocation()
        loadSavedLocations()

        // Set up permission result callback
        permissionResultCallback = { granted -> onPermissionResult(granted) }
    }

    /**
     * Call this when app resumes from background to check if permission was granted in settings
     */
    fun onResume() {
        // Check if permission state has changed
        val shouldShowSettings = shouldShowSettingsButton()

        // Update state if it has changed
        if (_locationState.value.shouldShowSettings != shouldShowSettings) {
            _locationState.update {
                it.copy(
                    shouldShowSettings = shouldShowSettings,
                    error = if (shouldShowSettings) {
                        "Please enable location permission in Settings to share your location"
                    } else {
                        null
                    }
                )
            }
        }
    }

    actual fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.update { it.copy(isLoading = true) }

            try {
                val result = locationRepository.getCurrentLocation()
                result.onSuccess { location ->
                    _locationState.update {
                        it.copy(
                            currentLocation = location,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _locationState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    actual fun loadSavedLocations() {
        viewModelScope.launch {
            try {
                val result = locationRepository.getSavedLocations()
                result.onSuccess { locations ->
                    _locationState.update {
                        it.copy(
                            savedLocations = locations,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error loading saved locations: ${e.message}")
            }
        }
    }

    actual fun requestLocationPermission() {
        if (hasLocationPermission()) {
            // Permission already granted, get location directly
            shareCurrentLocation()
        } else {
            // Check if Android will show the permission dialog or if user has permanently denied
            if (shouldShowSettingsButton()) {
                // User has permanently denied (selected "Don't ask again"), open settings
                openLocationSettings()
            } else {
                // Android will show permission dialog, request it
                _locationState.update { it.copy(permissionRequested = true) }
                permissionLauncher?.invoke(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    actual fun onPermissionResult(granted: Boolean) {
        if (granted) {
            // Permission granted, reset state and share location
            _locationState.update {
                it.copy(
                    shouldShowSettings = false,
                    permissionRequested = false,
                    error = null
                )
            }
            shareCurrentLocation()
        } else {
            // Permission denied - check if we should now show settings button
            val shouldShowSettings = shouldShowSettingsButton()

            _locationState.update {
                it.copy(
                    shouldShowSettings = shouldShowSettings,
                    permissionRequested = false,
                    error = if (shouldShowSettings) {
                        "Please enable location permission in Settings to share your location"
                    } else {
                        "Location permission is needed to share your exact location"
                    }
                )
            }
        }
    }

    /**
     * Check if we should show the "Open Settings" button instead of requesting permission
     * Returns true if user has permanently denied permission (selected "Don't ask again")
     */
    private fun shouldShowSettingsButton(): Boolean {
        // If permission is already granted, no need for settings
        if (hasLocationPermission()) {
            return false
        }

        // Check if we should show rationale for FINE_LOCATION permission
        val activity = context as? ComponentActivity ?: return false

        // shouldShowRequestPermissionRationale returns:
        // - false: if user has NEVER been asked OR has selected "Don't ask again"
        // - true: if user has denied but can still be asked again
        val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // If we have requested permission before (permissionRequested was true at some point)
        // AND shouldShowRationale is false, it means user selected "Don't ask again"
        // We need to track if we've ever requested permission
        val hasEverRequested = _locationState.value.permissionRequested ||
                               !shouldShowRationale && !hasLocationPermission()

        // Show settings button if:
        // - We've requested before AND rationale should not be shown
        // - This means user permanently denied
        return hasEverRequested && !shouldShowRationale
    }

    actual fun openLocationSettings() {
        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            _locationState.update {
                it.copy(error = "Could not open settings: ${e.message}")
            }
        }
    }

    actual fun shareCurrentLocation() {
        if (!hasLocationPermission()) {
            _locationState.update { it.copy(error = "Location permission not granted") }
            return
        }

        viewModelScope.launch {
            _locationState.update { it.copy(isLoading = true) }

            try {
                // Get current GPS location
                val location = getCurrentGPSLocation()

                if (location != null) {
                    // Reverse geocode to get address
                    val addressInfo = reverseGeocode(location.latitude, location.longitude)

                    // Save to backend
                    val result = locationRepository.shareLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationName = addressInfo.locationName,
                        address = addressInfo.address,
                        city = addressInfo.city,
                        region = addressInfo.region,
                        country = addressInfo.country,
                        isPrimary = _locationState.value.savedLocations.isEmpty() // First location is primary
                    )

                    result.onSuccess { savedLocation ->
                        _locationState.update {
                            it.copy(
                                currentLocation = savedLocation,
                                isLoading = false,
                                error = null
                            )
                        }
                        // Reload saved locations
                        loadSavedLocations()
                    }.onFailure { error ->
                        _locationState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to save location: ${error.message}"
                            )
                        }
                    }
                } else {
                    _locationState.update {
                        it.copy(
                            isLoading = false,
                            error = "Could not get current location"
                        )
                    }
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    actual fun setPrimaryLocation(locationId: Int) {
        viewModelScope.launch {
            try {
                val result = locationRepository.setPrimaryLocation(locationId)
                result.onSuccess {
                    // Reload locations to reflect the change
                    loadSavedLocations()
                    loadCurrentLocation()
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(error = "Failed to set primary location: ${e.message}")
                }
            }
        }
    }

    actual fun deleteLocation(locationId: Int) {
        viewModelScope.launch {
            try {
                val result = locationRepository.deleteLocation(locationId)
                result.onSuccess {
                    // Reload locations
                    loadSavedLocations()
                    loadCurrentLocation()
                }
            } catch (e: Exception) {
                _locationState.update {
                    it.copy(error = "Failed to delete location: ${e.message}")
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getCurrentGPSLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: Exception) {
            println("Error getting GPS location: ${e.message}")
            null
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): AddressInfo {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                AddressInfo(
                    locationName = address.featureName ?: address.locality,
                    address = address.getAddressLine(0),
                    city = address.locality,
                    region = address.adminArea,
                    country = address.countryName
                )
            } else {
                AddressInfo()
            }
        } catch (e: Exception) {
            println("Reverse geocoding failed: ${e.message}")
            AddressInfo()
        }
    }

    data class AddressInfo(
        val locationName: String? = null,
        val address: String? = null,
        val city: String? = null,
        val region: String? = null,
        val country: String? = null
    )
}
