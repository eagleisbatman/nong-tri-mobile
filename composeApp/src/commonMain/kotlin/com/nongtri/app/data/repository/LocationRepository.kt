package com.nongtri.app.data.repository

import com.nongtri.app.data.api.ApiClient
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.ui.components.UserLocation
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LocationRepository private constructor() {
    private val apiClient by lazy { ApiClient.getInstance() }
    private val userPreferences by lazy { UserPreferences.getInstance() }

    companion object {
        @Volatile
        private var instance: LocationRepository? = null

        fun getInstance(): LocationRepository {
            return instance ?: synchronized(this) {
                instance ?: LocationRepository().also { instance = it }
            }
        }
    }

    /**
     * Initialize location on app startup
     * Creates user if doesn't exist, detects and saves IP-based location
     */
    suspend fun initializeLocation(): Result<UserLocation?> {
        return try {
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val response = apiClient.client.post("/api/location/init") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "userId" to deviceInfo.device_id,
                    "deviceInfo" to mapOf(
                        "uuid" to deviceInfo.uuid,
                        "device_type" to deviceInfo.device_type,
                        "device_os" to deviceInfo.device_os,
                        "device_os_version" to deviceInfo.device_os_version,
                        "device_manufacturer" to deviceInfo.device_manufacturer,
                        "device_model" to deviceInfo.device_model,
                        "device_brand" to deviceInfo.device_brand,
                        "screen_width" to deviceInfo.screen_width,
                        "screen_height" to deviceInfo.screen_height,
                        "screen_density" to deviceInfo.screen_density,
                        "client_source" to deviceInfo.client_source,
                        "client_version" to deviceInfo.client_version,
                        "client_build_number" to deviceInfo.client_build_number,
                        "timezone_offset" to deviceInfo.timezone_offset,
                        "device_language" to deviceInfo.device_language
                    )
                ))
            }

            if (response.status.isSuccess()) {
                val body = response.body<LocationResponse>()
                if (body.success && body.location != null) {
                    Result.success(body.location.toUserLocation())
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("Failed to initialize location: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error initializing location: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get user's current best location (GPS > IP)
     */
    suspend fun getCurrentLocation(): Result<UserLocation?> {
        return try {
            val userId = userPreferences.getDeviceId()
            val response = apiClient.client.get("/api/location/current") {
                parameter("userId", userId)
            }

            if (response.status.isSuccess()) {
                val body = response.body<LocationResponse>()
                if (body.success && body.location != null) {
                    Result.success(body.location.toUserLocation())
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("Failed to get location: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error getting current location: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get all saved GPS locations
     */
    suspend fun getSavedLocations(): Result<List<UserLocation>> {
        return try {
            val userId = userPreferences.getDeviceId()
            val response = apiClient.client.get("/api/location/saved") {
                parameter("userId", userId)
            }

            if (response.status.isSuccess()) {
                val body = response.body<SavedLocationsResponse>()
                if (body.success) {
                    Result.success(body.locations.map { it.toUserLocation() })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Failed to get saved locations: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error getting saved locations: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Share GPS location
     */
    suspend fun shareLocation(
        latitude: Double,
        longitude: Double,
        locationName: String?,
        address: String?,
        city: String?,
        region: String?,
        country: String?,
        isPrimary: Boolean = true
    ): Result<UserLocation> {
        return try {
            val userId = userPreferences.getDeviceId()
            val response = apiClient.client.post("/api/location/share") {
                contentType(ContentType.Application.Json)
                setBody(ShareLocationRequest(
                    userId = userId,
                    latitude = latitude,
                    longitude = longitude,
                    location_name = locationName,
                    address = address,
                    city = city,
                    region = region,
                    country = country,
                    is_primary = isPrimary
                ))
            }

            if (response.status.isSuccess()) {
                val body = response.body<ShareLocationResponse>()
                if (body.success && body.location != null) {
                    Result.success(body.location.toUserLocation())
                } else {
                    Result.failure(Exception("Failed to save location"))
                }
            } else {
                Result.failure(Exception("Failed to share location: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error sharing location: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Set a location as primary
     */
    suspend fun setPrimaryLocation(locationId: Int): Result<Unit> {
        return try {
            val userId = userPreferences.getDeviceId()
            val response = apiClient.client.put("/api/location/$locationId/primary") {
                parameter("userId", userId)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to set primary location: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error setting primary location: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Delete a saved location
     */
    suspend fun deleteLocation(locationId: Int): Result<Unit> {
        return try {
            val userId = userPreferences.getDeviceId()
            val response = apiClient.client.delete("/api/location/$locationId") {
                parameter("userId", userId)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete location: ${response.status}"))
            }
        } catch (e: Exception) {
            println("Error deleting location: ${e.message}")
            Result.failure(e)
        }
    }
}

// API Models
@Serializable
data class ShareLocationRequest(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val location_name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val postal_code: String? = null,
    val is_primary: Boolean = true
)

@Serializable
data class LocationDTO(
    val id: Int? = null,
    val source: String,
    val priority: Int,
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val location_name: String? = null,
    val address: String? = null,
    val is_primary: Boolean = false,
    val shared_at: String? = null
)

@Serializable
data class LocationResponse(
    val success: Boolean,
    val location: LocationDTO? = null
)

@Serializable
data class SavedLocationsResponse(
    val success: Boolean,
    val locations: List<LocationDTO> = emptyList(),
    val count: Int = 0
)

@Serializable
data class ShareLocationResponse(
    val success: Boolean,
    val message: String? = null,
    val location: LocationDTO? = null
)

// Extension function to convert DTO to domain model
private fun LocationDTO.toUserLocation(): UserLocation {
    return UserLocation(
        id = this.id ?: 0,
        locationName = this.location_name,
        city = this.city,
        region = this.region,
        country = this.country,
        latitude = this.latitude,
        longitude = this.longitude,
        isPrimary = this.is_primary,
        source = this.source,
        sharedAt = this.shared_at
    )
}
