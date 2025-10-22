package com.nongtri.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Image validation and compression utility for plant diagnosis
 * Validates image format, size, and dimensions
 * Compresses images to target size while maintaining quality
 */
object ImageValidator {
    const val MAX_SIZE_MB = 5
    const val TARGET_SIZE_MB = 2  // Compress if larger
    const val MIN_DIMENSION = 200  // Minimum width or height in pixels
    const val MAX_DIMENSION = 4096

    /**
     * Validate image from URI
     * Checks format, dimensions, and file size
     * Returns ValidationResult with success/error/needs compression
     */
    fun validateImage(uri: Uri, context: Context): ValidationResult {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ValidationResult.Error("Failed to open image file")

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                return ValidationResult.Error("Invalid image file. Please select a valid image.")
            }

            // Check dimensions
            if (bitmap.width < MIN_DIMENSION || bitmap.height < MIN_DIMENSION) {
                bitmap.recycle()
                return ValidationResult.Error(
                    "Image too small (${bitmap.width}x${bitmap.height}px). " +
                    "Minimum size is ${MIN_DIMENSION}x${MIN_DIMENSION}px. " +
                    "Please use a higher resolution image for accurate diagnosis."
                )
            }

            if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
                // Image will be resized during compression
                return ValidationResult.Warning(
                    bitmap = bitmap,
                    message = "Image will be resized to fit quality requirements"
                )
            }

            // Check file size
            val fileSize = getFileSize(uri, context)
            val sizeMB = fileSize / (1024.0 * 1024.0)

            println("[ImageValidator] Image size: ${bitmap.width}x${bitmap.height}px, ${String.format("%.2f", sizeMB)}MB")

            if (sizeMB > MAX_SIZE_MB) {
                bitmap.recycle()
                return ValidationResult.Error(
                    "Image too large (${String.format("%.1f", sizeMB)}MB). " +
                    "Maximum size is ${MAX_SIZE_MB}MB. " +
                    "Please select a smaller image."
                )
            }

            if (sizeMB > TARGET_SIZE_MB) {
                return ValidationResult.NeedsCompression(
                    bitmap = bitmap,
                    currentSizeMB = sizeMB
                )
            }

            return ValidationResult.Valid(bitmap)

        } catch (e: Exception) {
            println("[ImageValidator] Error validating image: ${e.message}")
            return ValidationResult.Error("Failed to read image: ${e.message}")
        }
    }

    /**
     * Compress image to target size while maintaining quality
     * Uses iterative quality reduction from 90% to 50%
     * Returns compressed JPEG as ByteArray
     */
    fun compressImage(
        bitmap: Bitmap,
        targetSizeMB: Int = TARGET_SIZE_MB
    ): ByteArray {
        println("[ImageValidator] Compressing image (${bitmap.width}x${bitmap.height}px) to target ${targetSizeMB}MB")

        var quality = 90
        var output: ByteArray
        val targetSizeBytes = targetSizeMB * 1024 * 1024

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            output = stream.toByteArray()
            stream.close()

            val sizeMB = output.size / (1024.0 * 1024.0)
            println("[ImageValidator] Quality $quality% → ${String.format("%.2f", sizeMB)}MB")

            if (output.size <= targetSizeBytes || quality <= 50) {
                break
            }

            quality -= 10
        } while (true)

        val finalSizeMB = output.size / (1024.0 * 1024.0)
        println("[ImageValidator] ✓ Compression complete: ${String.format("%.2f", finalSizeMB)}MB at $quality% quality")

        return output
    }

    /**
     * Resize bitmap if dimensions exceed maximum
     * Maintains aspect ratio
     */
    fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= MAX_DIMENSION && bitmap.height <= MAX_DIMENSION) {
            return bitmap
        }

        val ratio = minOf(
            MAX_DIMENSION.toFloat() / bitmap.width,
            MAX_DIMENSION.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        println("[ImageValidator] Resizing ${bitmap.width}x${bitmap.height} → ${newWidth}x${newHeight}")

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Get file size from URI
     */
    private fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            // Try using ContentResolver cursor
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: run {
                // Fallback: read actual file if it's a file URI
                uri.path?.let { path ->
                    File(path).length()
                } ?: 0L
            }
        } catch (e: Exception) {
            println("[ImageValidator] Error getting file size: ${e.message}")
            0L
        }
    }

    /**
     * Convert ByteArray to Base64 data URL for API
     */
    fun toBase64DataUrl(imageBytes: ByteArray): String {
        val base64 = android.util.Base64.encodeToString(
            imageBytes,
            android.util.Base64.NO_WRAP
        )
        return "data:image/jpeg;base64,$base64"
    }
}

/**
 * Validation result with different states
 */
sealed class ValidationResult {
    /**
     * Image is valid and ready to use
     */
    data class Valid(val bitmap: Bitmap) : ValidationResult()

    /**
     * Image needs compression before upload
     */
    data class NeedsCompression(
        val bitmap: Bitmap,
        val currentSizeMB: Double
    ) : ValidationResult()

    /**
     * Image has minor issues but can be used
     */
    data class Warning(
        val bitmap: Bitmap,
        val message: String
    ) : ValidationResult()

    /**
     * Image is invalid or has critical issues
     */
    data class Error(val message: String) : ValidationResult()
}
