package com.nongtri.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
    }

    fun initialize(context: Context) {
        this.context = context
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        loadCurrentLocation()
        loadSavedLocations()
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
            // Request permission
            _locationState.update { it.copy(permissionRequested = true) }
            permissionLauncher?.invoke(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
