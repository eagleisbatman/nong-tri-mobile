package com.nongtri.app.data.api

import com.nongtri.app.AppConfig
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Singleton ApiClient for HTTP requests
 * Provides a shared HttpClient instance with proper configuration
 */
class ApiClient private constructor() {
    val baseUrl: String = AppConfig.API_URL

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    // Redact sensitive data (base64 images/audio) in logs
                    val redacted = if (message.contains("base64") || message.contains("data:image") || message.contains("data:audio")) {
                        message.replace(Regex("(data:(image|audio)/[^;]+;base64,)[A-Za-z0-9+/=]{20,}"), "$1[REDACTED]")
                            .replace(Regex("\"imageData\":\"[^\"]+\""), "\"imageData\":\"[REDACTED]\"")
                            .replace(Regex("\"audio\":\"[^\"]+\""), "\"audio\":\"[REDACTED]\"")
                    } else {
                        message
                    }
                    // Only log in debug builds (release builds skip logging)
                    // For production, set level to NONE or remove this logger entirely
                    if (AppConfig.VERSION_NAME.contains("debug", ignoreCase = true) || 
                        System.getProperty("debug") == "true") {
                        println("[ApiClient] $redacted")
                    }
                }
            }
            // Set to NONE in release builds to disable all logging
            level = LogLevel.NONE  // Change to LogLevel.INFO for debugging
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000  // 60 seconds for AI responses
            connectTimeoutMillis = 15000  // 15 seconds to establish connection
            socketTimeoutMillis = 60000   // 60 seconds for socket read/write
        }
        // Set default base URL for all requests
        defaultRequest {
            url(baseUrl)
        }
    }

    companion object {
        @Volatile
        private var instance: ApiClient? = null

        fun getInstance(): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient().also { instance = it }
            }
        }
    }

    fun close() {
        client.close()
    }
}
