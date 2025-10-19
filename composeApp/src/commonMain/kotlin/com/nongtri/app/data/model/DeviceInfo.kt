package com.nongtri.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    @SerialName("device_id") val deviceId: String,
    @SerialName("uuid") val uuid: String,
    @SerialName("device_type") val deviceType: String,
    @SerialName("device_os") val deviceOs: String,
    @SerialName("device_os_version") val deviceOsVersion: String,
    @SerialName("device_manufacturer") val deviceManufacturer: String?,
    @SerialName("device_model") val deviceModel: String?,
    @SerialName("device_brand") val deviceBrand: String?,
    @SerialName("screen_width") val screenWidth: Int?,
    @SerialName("screen_height") val screenHeight: Int?,
    @SerialName("screen_density") val screenDensity: Float?,
    @SerialName("client_source") val clientSource: String,
    @SerialName("client_version") val clientVersion: String,
    @SerialName("client_build_number") val clientBuildNumber: String,
    @SerialName("timezone_offset") val timezoneOffset: Int?,
    @SerialName("device_language") val deviceLanguage: String?
)
