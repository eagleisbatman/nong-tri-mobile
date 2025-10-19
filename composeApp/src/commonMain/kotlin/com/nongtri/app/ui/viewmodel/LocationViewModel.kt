package com.nongtri.app.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.ui.components.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationState(
    val currentLocation: UserLocation? = null,
    val savedLocations: List<UserLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionRequested: Boolean = false
)

expect class LocationViewModel() : ViewModel {
    val locationState: StateFlow<LocationState>

    fun loadCurrentLocation()
    fun loadSavedLocations()
    fun requestLocationPermission()
    fun shareCurrentLocation()
    fun setPrimaryLocation(locationId: Int)
    fun deleteLocation(locationId: Int)
}

@Composable
expect fun rememberLocationViewModel(): LocationViewModel
