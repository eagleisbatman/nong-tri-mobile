package com.nongtri.app.util

import android.util.Base64
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.Buffer

/**
 * Coil Fetcher for handling base64 data URLs
 * Supports format: data:image/jpeg;base64,/9j/4AAQ...
 */
class Base64ImageFetcher(
    private val data: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        // Extract base64 data after "base64," prefix
        val base64Data = data.substringAfter("base64,")

        // Decode base64 to bytes
        val bytes = Base64.decode(base64Data, Base64.DEFAULT)

        // Create Okio Buffer and write bytes
        val buffer = Buffer()
        buffer.write(bytes)

        // Return as SourceFetchResult
        return SourceFetchResult(
            source = ImageSource(
                source = buffer,
                fileSystem = options.fileSystem
            ),
            mimeType = extractMimeType(data),
            dataSource = DataSource.MEMORY
        )
    }

    private fun extractMimeType(dataUrl: String): String? {
        // Extract MIME type from data URL (e.g., "data:image/jpeg;base64,..." -> "image/jpeg")
        return if (dataUrl.startsWith("data:")) {
            dataUrl.substringAfter("data:")
                .substringBefore(";")
                .takeIf { it.isNotEmpty() }
        } else {
            null
        }
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(
            data: String,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            // Only handle strings that start with "data:image/"
            return if (data.startsWith("data:image/") && data.contains("base64,")) {
                Base64ImageFetcher(data, options)
            } else {
                null
            }
        }
    }
}
