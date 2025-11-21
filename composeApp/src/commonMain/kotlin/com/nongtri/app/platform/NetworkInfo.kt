package com.nongtri.app.platform

/**
 * Platform-specific network info helper.
 */
expect object NetworkInfo {
    fun getNetworkType(): String // e.g., "wifi", "cellular", "none", "unknown"
}
