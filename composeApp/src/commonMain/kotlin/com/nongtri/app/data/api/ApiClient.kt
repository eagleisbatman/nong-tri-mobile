package com.nongtri.app.data.api

import com.nongtri.app.BuildConfig
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Singleton ApiClient for HTTP requests
 * Provides a shared HttpClient instance with proper configuration
 */
class ApiClient private constructor() {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000  // 60 seconds for AI responses
            connectTimeoutMillis = 15000  // 15 seconds to establish connection
            socketTimeoutMillis = 60000   // 60 seconds for socket read/write
        }
    }

    val baseUrl: String = BuildConfig.API_URL

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
