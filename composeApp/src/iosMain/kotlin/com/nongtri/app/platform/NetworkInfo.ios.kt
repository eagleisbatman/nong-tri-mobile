package com.nongtri.app.platform

actual object NetworkInfo {
    actual fun getNetworkType(): String {
        // Simplified for iOS: returning unknown avoids blocking analytics; can be extended later.
        return "unknown"
    }
}
