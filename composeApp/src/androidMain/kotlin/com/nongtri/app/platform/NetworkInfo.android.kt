package com.nongtri.app.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.nongtri.app.App

actual object NetworkInfo {
    actual fun getNetworkType(): String {
        return try {
            val cm = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return "none"
            val caps = cm.getNetworkCapabilities(network) ?: return "unknown"
            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "unknown"
            }
        } catch (e: Exception) {
            "unknown"
        }
    }
}
