package com.nongtri.app.data.provider

import com.nongtri.app.data.model.DeviceInfo

/**
 * Provides device-specific information like device ID, UUID, OS details, etc.
 * This is an expect/actual pattern where each platform (Android, iOS) provides its own implementation.
 */
expect class DeviceInfoProvider {
    /**
     * Get comprehensive device information
     * @return DeviceInfo containing device ID, UUID, OS details, screen info, etc.
     */
    fun getDeviceInfo(): DeviceInfo

    /**
     * Get unique device ID (Android ID / iOS IDFV)
     * This persists across app installs (on Android) or per-app (on iOS)
     */
    fun getDeviceId(): String

    /**
     * Get per-install UUID
     * This is unique per app installation and regenerates on reinstall
     */
    fun getUuid(): String

    /**
     * Check if this is first app launch (for UUID generation)
     */
    fun isFirstLaunch(): Boolean
}
