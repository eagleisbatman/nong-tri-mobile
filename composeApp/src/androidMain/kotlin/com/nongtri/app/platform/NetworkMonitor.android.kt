package com.nongtri.app.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Network Monitor Service
 * BATCH 3: Monitors network connectivity and tracks reconnection events for analytics
 */
class NetworkMonitor(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(checkInitialConnection())
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var disconnectedAt: Long? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        startMonitoring()
    }

    /**
     * Check initial network connection state
     */
    private fun checkInitialConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Start monitoring network connectivity changes
     */
    private fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network became available (reconnected)
                val wasDisconnected = !_isConnected.value

                if (wasDisconnected && disconnectedAt != null) {
                    // BATCH 3: Track network reconnection with disconnection duration
                    val disconnectionDurationMs = System.currentTimeMillis() - disconnectedAt!!

                    com.nongtri.app.analytics.Events.logNetworkReconnected(
                        disconnectionDurationMs = disconnectionDurationMs
                    )

                    println("[NetworkMonitor] ✓ Network reconnected after ${disconnectionDurationMs}ms")
                    disconnectedAt = null
                }

                _isConnected.value = true
            }

            override fun onLost(network: Network) {
                // Network lost (disconnected)
                if (_isConnected.value) {
                    disconnectedAt = System.currentTimeMillis()
                    println("[NetworkMonitor] ⚠ Network disconnected")
                }

                _isConnected.value = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Network capabilities changed (e.g., WiFi -> Mobile data)
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                if (hasInternet != _isConnected.value) {
                    if (hasInternet) {
                        onAvailable(network)
                    } else {
                        onLost(network)
                    }
                }
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            println("[NetworkMonitor] Started monitoring network connectivity")
        } catch (e: Exception) {
            println("[NetworkMonitor] ❌ Failed to register network callback: ${e.message}")
        }
    }

    /**
     * Stop monitoring network connectivity (cleanup)
     */
    fun stopMonitoring() {
        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
                println("[NetworkMonitor] Stopped monitoring network connectivity")
            }
        } catch (e: Exception) {
            println("[NetworkMonitor] ❌ Failed to unregister network callback: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor(context.applicationContext).also { instance = it }
            }
        }
    }
}
