package com.nongtri.app.util

import android.util.Base64
import android.util.Log
import coil3.map.Mapper
import coil3.request.Options

/**
 * Coil Mapper that converts base64 data URLs to ByteArray
 * This runs BEFORE Fetchers, so we can intercept base64 strings
 */
class Base64DataMapper : Mapper<String, ByteArray> {
    override fun map(data: String, options: Options): ByteArray? {
        // Only handle base64 data URLs
        if (!data.startsWith("data:image/") || !data.contains("base64,")) {
            return null // Let other mappers/fetchers handle it
        }

        Log.i("Base64DataMapper", "✅ Converting base64 data URL to ByteArray (length: ${data.length})")

        return try {
            // Extract base64 data after "base64," prefix
            val base64Data = data.substringAfter("base64,")
            
            // Decode base64 to bytes
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            Log.i("Base64DataMapper", "✅ Decoded ${bytes.size} bytes")
            
            bytes
        } catch (e: Exception) {
            Log.e("Base64DataMapper", "❌ Error decoding base64: ${e.message}", e)
            null
        }
    }
}
