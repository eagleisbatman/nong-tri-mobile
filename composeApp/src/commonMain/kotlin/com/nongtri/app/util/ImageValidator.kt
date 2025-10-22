package com.nongtri.app.util

import com.nongtri.app.l10n.Strings

/**
 * Result of image validation
 */
sealed class ImageValidationResult {
    data object Valid : ImageValidationResult()
    data class Invalid(val reason: String) : ImageValidationResult()
}

/**
 * Validates images before upload for plant diagnosis
 */
object ImageValidator {
    // Maximum file size: 5MB
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024

    // Maximum dimensions: 4096x4096
    private const val MAX_DIMENSION = 4096

    // Supported formats
    private val SUPPORTED_FORMATS = setOf("jpg", "jpeg", "png", "webp")

    /**
     * Validate image file size
     */
    fun validateFileSize(sizeBytes: Long, strings: Strings): ImageValidationResult {
        return if (sizeBytes > MAX_FILE_SIZE_BYTES) {
            ImageValidationResult.Invalid(
                strings.formatImageTooLargeWithSize(
                    formatSize(sizeBytes),
                    formatSize(MAX_FILE_SIZE_BYTES.toLong())
                )
            )
        } else if (sizeBytes == 0L) {
            ImageValidationResult.Invalid(strings.errorImageFileEmpty)
        } else {
            ImageValidationResult.Valid
        }
    }

    /**
     * Validate image dimensions
     */
    fun validateDimensions(width: Int, height: Int, strings: Strings): ImageValidationResult {
        return when {
            width == 0 || height == 0 -> {
                ImageValidationResult.Invalid(strings.errorInvalidImageDimensions)
            }
            width > MAX_DIMENSION || height > MAX_DIMENSION -> {
                ImageValidationResult.Invalid(
                    strings.formatImageDimensionsTooLarge(width, height, MAX_DIMENSION)
                )
            }
            else -> ImageValidationResult.Valid
        }
    }

    /**
     * Validate image format by file extension or MIME type
     */
    fun validateFormat(filename: String?, mimeType: String?, strings: Strings): ImageValidationResult {
        // Check MIME type first if available
        if (mimeType != null) {
            val validMimeTypes = setOf("image/jpeg", "image/png", "image/webp")
            if (mimeType.lowercase() !in validMimeTypes) {
                return ImageValidationResult.Invalid(
                    strings.formatUnsupportedImageFormatMimeType(mimeType)
                )
            }
            return ImageValidationResult.Valid
        }

        // Fall back to file extension
        if (filename != null) {
            val extension = filename.substringAfterLast('.', "").lowercase()
            if (extension !in SUPPORTED_FORMATS) {
                return ImageValidationResult.Invalid(
                    strings.formatUnsupportedImageFormatExtension(extension)
                )
            }
            return ImageValidationResult.Valid
        }

        return ImageValidationResult.Invalid(strings.errorCannotDetermineImageFormat)
    }

    /**
     * Perform complete validation
     */
    fun validate(
        sizeBytes: Long,
        width: Int,
        height: Int,
        strings: Strings,
        filename: String? = null,
        mimeType: String? = null
    ): ImageValidationResult {
        // Validate size
        val sizeResult = validateFileSize(sizeBytes, strings)
        if (sizeResult is ImageValidationResult.Invalid) {
            return sizeResult
        }

        // Validate dimensions
        val dimensionsResult = validateDimensions(width, height, strings)
        if (dimensionsResult is ImageValidationResult.Invalid) {
            return dimensionsResult
        }

        // Validate format
        val formatResult = validateFormat(filename, mimeType, strings)
        if (formatResult is ImageValidationResult.Invalid) {
            return formatResult
        }

        return ImageValidationResult.Valid
    }

    /**
     * Format file size for display
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    /**
     * Check if image needs compression
     * Returns suggested quality (0-100) or null if no compression needed
     */
    fun getSuggestedCompressionQuality(sizeBytes: Long): Int? {
        val targetSize = 2 * 1024 * 1024 // 2MB target
        return when {
            sizeBytes <= targetSize -> null  // No compression needed
            sizeBytes > MAX_FILE_SIZE_BYTES -> 50  // Aggressive compression
            else -> {
                // Calculate quality based on size
                val ratio = targetSize.toFloat() / sizeBytes
                (ratio * 100).toInt().coerceIn(50, 90)
            }
        }
    }
}
