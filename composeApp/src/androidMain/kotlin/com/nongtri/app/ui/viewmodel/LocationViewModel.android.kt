package com.nongtri.app.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
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
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.data.repository.LocationRepository
import com.nongtri.app.l10n.LocalizationProvider
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
    private val userPreferences = UserPreferences.getInstance()
    private val strings get() = LocalizationProvider.getStrings(userPreferences.language.value)

    // Permission tracking for analytics
    private var permissionRequestTime = 0L
    private var denialCount = 0

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

        // Check initial permission state on startup
        checkInitialPermissionState()
    }

    /**
     * Check permission state when app starts to show correct button immediately
     */
    private fun checkInitialPermissionState() {
        if (hasLocationPermission()) {
            // Permission already granted, nothing to show
            return
        }

        val activity = context as? ComponentActivity ?: return

        // Check SharedPreferences to see if we've ever requested permission before
        val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        val hasEverRequested = prefs.getBoolean("permission_requested", false)

        if (!hasEverRequested) {
            // First time user, show "Share My Location" button
            return
        }

        // User has requested before, check if they can still see the permission dialog
        val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        println("Initial permission check: hasPermission=false, shouldShowRationale=$shouldShowRationale, hasEverRequested=$hasEverRequested")

        // If shouldShowRationale=false AND hasEverRequested=true,
        // user has denied twice and exhausted the limit
        if (!shouldShowRationale) {
            println("User has exhausted permission requests - showing Settings button")
            _locationState.update {
                it.copy(
                    shouldShowSettings = true,
                    permissionRequested = true,
                    error = strings.permissionLocationDeniedSettings
                )
            }
        }
    }

    /**
     * Check permission state - call when returning from settings or when bottom sheet is shown
     */
    actual fun checkPermissionState() {
        println("Checking permission state...")

        // If we were showing settings button but permission is now granted, reset state
        if (_locationState.value.shouldShowSettings && hasLocationPermission()) {
            println("Permission granted in settings! Resetting to 'Share My Location'")
            _locationState.update {
                it.copy(
                    shouldShowSettings = false,
                    error = null
                )
            }
        }
    }

    actual fun loadCurrentLocation() {
        viewModelScope.launch {
            _locationState.update { it.copy(isLoading = true) }

            try {
                val result = locationRepository.getCurrentLocations()
                result.onSuccess { (ipLocation, gpsLocation) ->
                    _locationState.update {
                        it.copy(
                            ipLocation = ipLocation,
                            gpsLocation = gpsLocation,
                            currentLocation = gpsLocation ?: ipLocation,  // Deprecated field for backward compatibility
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
            // Check if we should show settings button
            if (_locationState.value.shouldShowSettings) {
                // User has exhausted permission requests, open settings
                println("Opening settings - user exhausted permission requests")
                openLocationSettings()
            } else {
                // Save to SharedPreferences that we've requested permission
                val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("permission_requested", true).apply()

                _locationState.update { it.copy(permissionRequested = true) }

                // Track permission request for analytics
                permissionRequestTime = System.currentTimeMillis()
                com.nongtri.app.analytics.Events.logLocationPermissionRequested("location_button")

                println("Requesting location permission...")
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
        println("Permission result: granted=$granted")

        val timeToGrantMs = if (permissionRequestTime > 0) {
            System.currentTimeMillis() - permissionRequestTime
        } else 0L

        if (granted) {
            // Track permission granted event
            val permissionType = "fine_and_coarse"
            com.nongtri.app.analytics.Events.logLocationPermissionGranted(
                permissionType = permissionType,
                timeToGrantMs = timeToGrantMs
            )

            // Permission granted, reset state and share location
            _locationState.update {
                it.copy(
                    shouldShowSettings = false,
                    permissionRequested = true,
                    error = null
                )
            }
            shareCurrentLocation()
        } else {
            // Track denial
            denialCount++

            // Permission denied - check if user has exhausted requests
            val shouldShowRationale = shouldShowRequestPermissionRationale()

            // Track permission denied event
            com.nongtri.app.analytics.Events.logLocationPermissionDenied(
                denialCount = denialCount,
                canRequestAgain = shouldShowRationale
            )

            println("Permission denied: shouldShowRationale=$shouldShowRationale")

            // If shouldShowRationale=false, user has exhausted permission requests
            // Show "Open Settings" button
            if (!shouldShowRationale) {
                println("User exhausted permission requests - showing Settings button")
                _locationState.update {
                    it.copy(
                        shouldShowSettings = true,
                        permissionRequested = true,
                        error = strings.permissionLocationDeniedSettings
                    )
                }
            } else {
                // User can still request permission again
                _locationState.update {
                    it.copy(
                        permissionRequested = true,
                        error = strings.permissionLocationRationale
                    )
                }
            }
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    actual fun openLocationSettings() {
        // ROUND 7: Track location settings opened
        com.nongtri.app.analytics.Events.logLocationPermissionSettingsOpened()

        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            _locationState.update {
                it.copy(error = "${strings.errorCouldNotOpenSettings}: ${e.message}")
            }
        }
    }

    actual fun shareCurrentLocation() {
        if (!hasLocationPermission()) {
            _locationState.update { it.copy(error = strings.errorLocationPermissionNotGranted) }
            return
        }

        viewModelScope.launch {
            _locationState.update { it.copy(isLoading = true) }

            try {
                // ROUND 6: Track GPS location requested
                com.nongtri.app.analytics.Events.logLocationGpsRequested()

                // Get current GPS location
                val location = getCurrentGPSLocation()

                if (location != null) {
                    // ROUND 4: Track GPS location obtained
                    com.nongtri.app.analytics.Events.logLocationGpsObtained(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )

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
                                gpsLocation = savedLocation,
                                currentLocation = savedLocation,  // Deprecated field for backward compatibility
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
                    // ROUND 4: Track GPS location failed
                    com.nongtri.app.analytics.Events.logLocationGpsFailed(
                        errorType = "location_null",
                        errorMessage = "GPS returned null location"
                    )

                    _locationState.update {
                        it.copy(
                            isLoading = false,
                            error = strings.errorCouldNotGetCurrentLocation
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

    @SuppressLint("MissingPermission")
    // Permission is checked by caller (shareCurrentLocation checks hasLocationPermission)
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
                // Generate a meaningful location name: use locality (city) or admin area (region)
                // Don't use featureName as it can be just a street number like "9"
                val meaningfulName = address.locality ?: address.adminArea ?: address.countryName

                AddressInfo(
                    locationName = meaningfulName,
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
