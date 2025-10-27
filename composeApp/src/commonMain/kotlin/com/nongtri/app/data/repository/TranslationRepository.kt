package com.nongtri.app.data.repository

import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.model.TranslationResponse
import com.nongtri.app.data.preferences.UserPreferences
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TranslationVersion(
    val language: String,
    val version: Int,
    val updated_at: String?
)

/**
 * Repository for managing translations from Weblate API
 *
 * Flow:
 * 1. Check server version vs cached version
 * 2. If version changed, fetch new translations
 * 3. If successful, cache locally with version number
 * 4. If API fails, fallback to cached translations
 * 5. If no cache exists, return empty map (hardcoded strings will be used as final fallback)
 */
class TranslationRepository(
    private val api: NongTriApi,
    private val userPreferences: UserPreferences
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Get translations for a specific language
     * @param languageCode Language code ("en", "vi")
     * @return Map of translation keys to values
     */
    suspend fun getTranslations(languageCode: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            // Fetch from API
            val response = api.httpClient.get("/api/translations/$languageCode").body<TranslationResponse>()

            // Cache the translations locally
            cacheTranslations(languageCode, response.translations)

            println("✅ Fetched ${response.count} translations for $languageCode from API")
            response.translations

        } catch (e: Exception) {
            println("⚠️ Failed to fetch translations from API: ${e.message}")

            // Fallback to cached translations
            val cached = getCachedTranslations(languageCode)
            if (cached.isNotEmpty()) {
                println("✅ Using ${cached.size} cached translations for $languageCode")
                return@withContext cached
            }

            println("❌ No cached translations available for $languageCode")
            emptyMap()
        }
    }

    /**
     * Refresh translations cache (force fetch from API)
     */
    suspend fun refreshTranslations(languageCode: String): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val response = api.httpClient.get("/api/translations/$languageCode").body<TranslationResponse>()
            cacheTranslations(languageCode, response.translations)
            println("✅ Refreshed ${response.count} translations for $languageCode")
            Result.success(response.translations)
        } catch (e: Exception) {
            println("❌ Failed to refresh translations: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Clear cached translations for a language
     */
    suspend fun clearCache(languageCode: String) = withContext(Dispatchers.IO) {
        userPreferences.setString("translations_$languageCode", "")
        println("🗑️ Cleared cache for $languageCode")
    }

    /**
     * Clear all cached translations
     */
    suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        userPreferences.setString("translations_en", "")
        userPreferences.setString("translations_vi", "")
        println("🗑️ Cleared all translation caches")
    }

    /**
     * Save translations to local cache
     */
    private suspend fun cacheTranslations(languageCode: String, translations: Map<String, String>) {
        try {
            val jsonString = json.encodeToString(translations)
            userPreferences.setString("translations_$languageCode", jsonString)
            println("💾 Cached ${translations.size} translations for $languageCode")
        } catch (e: Exception) {
            println("⚠️ Failed to cache translations: ${e.message}")
        }
    }

    /**
     * Get cached translations from local storage
     */
    private suspend fun getCachedTranslations(languageCode: String): Map<String, String> {
        return try {
            val jsonString = userPreferences.getString("translations_$languageCode", "")
            if (jsonString.isEmpty()) {
                emptyMap()
            } else {
                json.decodeFromString<Map<String, String>>(jsonString)
            }
        } catch (e: Exception) {
            println("⚠️ Failed to load cached translations: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Check if translations are cached locally
     */
    suspend fun hasCachedTranslations(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        val jsonString = userPreferences.getString("translations_$languageCode", "")
        jsonString.isNotEmpty()
    }

    /**
     * Check if translations need updating (by comparing version numbers)
     * @return true if translations should be refetched, false otherwise
     */
    suspend fun needsUpdate(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get server version
            val serverVersion = getServerVersion(languageCode)

            // Get cached version
            val cachedVersion = getCachedVersion(languageCode)

            println("🔍 Version check: $languageCode - Server: v${serverVersion}, Cached: v${cachedVersion}")

            // Need update if server version is higher
            serverVersion > cachedVersion
        } catch (e: Exception) {
            println("⚠️ Version check failed: ${e.message}")
            // If version check fails, don't force update (use cache)
            false
        }
    }

    /**
     * Get current translation version from server
     */
    private suspend fun getServerVersion(languageCode: String): Int {
        return try {
            val response = api.httpClient.get("/api/translations/version/$languageCode")
                .body<TranslationVersion>()
            response.version
        } catch (e: Exception) {
            println("⚠️ Failed to get server version: ${e.message}")
            0
        }
    }

    /**
     * Get cached translation version
     */
    private suspend fun getCachedVersion(languageCode: String): Int {
        return userPreferences.getInt("translations_version_$languageCode", 0)
    }

    /**
     * Save translation version to cache
     */
    private suspend fun cacheVersion(languageCode: String, version: Int) {
        userPreferences.setInt("translations_version_$languageCode", version)
        println("💾 Cached version for $languageCode: v$version")
    }

    /**
     * Get translations with version checking
     * This is the smart method that checks versions before fetching
     */
    suspend fun getTranslationsWithVersionCheck(languageCode: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to update
            if (needsUpdate(languageCode)) {
                println("🔄 Translation update available for $languageCode, fetching...")
                val response = api.httpClient.get("/api/translations/$languageCode").body<TranslationResponse>()

                // Cache translations and version
                cacheTranslations(languageCode, response.translations)
                cacheVersion(languageCode, getServerVersion(languageCode))

                println("✅ Updated to latest translations for $languageCode")
                return@withContext response.translations
            } else {
                // Use cached translations
                val cached = getCachedTranslations(languageCode)
                if (cached.isNotEmpty()) {
                    println("✅ Using ${cached.size} cached translations for $languageCode (up to date)")
                    return@withContext cached
                } else {
                    // No cache, fetch from API
                    println("🌐 No cache found, fetching $languageCode translations...")
                    val response = api.httpClient.get("/api/translations/$languageCode").body<TranslationResponse>()
                    cacheTranslations(languageCode, response.translations)
                    cacheVersion(languageCode, getServerVersion(languageCode))
                    return@withContext response.translations
                }
            }
        } catch (e: Exception) {
            println("❌ Failed to get translations: ${e.message}")

            // Fallback to cached translations
            val cached = getCachedTranslations(languageCode)
            if (cached.isNotEmpty()) {
                println("✅ Using ${cached.size} cached translations (offline)")
                return@withContext cached
            }

            println("❌ No cached translations available for $languageCode")
            emptyMap()
        }
    }
}
