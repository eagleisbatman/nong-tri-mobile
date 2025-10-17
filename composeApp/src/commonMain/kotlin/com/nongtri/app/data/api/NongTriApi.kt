package com.nongtri.app.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NongTriApi(
    private val baseUrl: String = "https://nong-tri.up.railway.app"
) {
    private val client = HttpClient {
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
    }

    suspend fun sendMessage(userId: String, message: String, userName: String? = null): Result<ChatResponse> {
        return try {
            val response: ChatResponse = client.post("$baseUrl/api/chat") {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(userId, message, userName))
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(userId: String, limit: Int = 20): Result<HistoryResponse> {
        return try {
            val response: HistoryResponse = client.get("$baseUrl/api/chat/history/$userId") {
                parameter("limit", limit)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(userId: String): Result<Boolean> {
        return try {
            client.delete("$baseUrl/api/chat/history/$userId")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}
