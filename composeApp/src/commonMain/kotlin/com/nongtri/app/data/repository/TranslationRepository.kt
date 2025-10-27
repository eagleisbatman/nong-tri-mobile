package com.nongtri.app.data.repository

import com.nongtri.app.data.api.NongTriApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Repository for managing translations from Weblate API
 *
 * Flow:
 * 1. Check server version vs cached version
 * 2. If version changed, fetch new translations
 * 3. If successful, cache locally with version number (in-memory for now)
 * 4. If API fails, fallback to cached translations
 * 5. If no cache exists, return empty map (hardcoded strings will be used as final fallback)
 */
class TranslationRepository(
    private val api: NongTriApi
) {
    // In-memory cache for translations
    private val translationCache = mutableMapOf<String, Map<String, String>>()
    private val versionCache = mutableMapOf<String, Int>()

    /**
     * Get translations for a specific language
     * @param languageCode Language code ("en", "vi")
     * @return Map of translation keys to values
     */
    suspend fun getTranslations(languageCode: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            // Fetch from API
            val result = api.getTranslations(languageCode)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                translationCache[languageCode] = response.translations
                println("✅ Fetched ${response.count} translations for $languageCode from API")
                response.translations
            } else {
                println("⚠️ Failed to fetch translations from API: ${result.exceptionOrNull()?.message}")
                // Fallback to cached translations
                translationCache[languageCode] ?: emptyMap()
            }
        } catch (e: Exception) {
            println("⚠️ Failed to fetch translations from API: ${e.message}")
            // Fallback to cached translations
            translationCache[languageCode] ?: emptyMap()
        }
    }

    /**
     * Get translations with version checking
     * Only fetches new translations if server version has changed
     */
    suspend fun getTranslationsWithVersionCheck(languageCode: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            // Check server version
            val versionResult = api.getTranslationVersion(languageCode)

            if (versionResult.isSuccess) {
                val serverVersion = versionResult.getOrNull()!!
                val cachedVersion = versionCache[languageCode] ?: 0

                if (serverVersion > cachedVersion) {
                    println("📥 Translation update available: v$cachedVersion → v$serverVersion")

                    // Fetch new translations
                    val translationsResult = api.getTranslations(languageCode)

                    if (translationsResult.isSuccess) {
                        val response = translationsResult.getOrNull()!!
                        translationCache[languageCode] = response.translations
                        versionCache[languageCode] = serverVersion

                        println("✅ Updated to v$serverVersion with ${response.count} translations")
                        return@withContext response.translations
                    }
                } else {
                    println("✅ Translations up-to-date (v$serverVersion)")
                }

                // Return cached translations if available
                return@withContext translationCache[languageCode] ?: emptyMap()
            } else {
                println("⚠️ Version check failed, using cached translations")
                return@withContext translationCache[languageCode] ?: emptyMap()
            }
        } catch (e: Exception) {
            println("❌ Translation version check error: ${e.message}")
            return@withContext translationCache[languageCode] ?: emptyMap()
        }
    }

    /**
     * Refresh translations cache (force fetch from API)
     */
    suspend fun refreshTranslations(languageCode: String): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val result = api.getTranslations(languageCode)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                translationCache[languageCode] = response.translations
                println("✅ Refreshed ${response.count} translations for $languageCode")
                Result.success(response.translations)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            println("❌ Failed to refresh translations: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Clear cached translations for a language
     */
    fun clearCache(languageCode: String) {
        translationCache.remove(languageCode)
        versionCache.remove(languageCode)
        println("🗑️ Cleared cache for $languageCode")
    }

    /**
     * Clear all cached translations
     */
    fun clearAllCaches() {
        translationCache.clear()
        versionCache.clear()
        println("🗑️ Cleared all translation caches")
    }
}
