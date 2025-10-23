package com.nongtri.app.platform

import androidx.compose.runtime.Composable

/**
 * Result of image selection
 */
data class ImagePickerResult(
    val uri: String,           // Platform-specific URI
    val base64Data: String?,    // Base64-encoded image data (optional, for upload)
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val mimeType: String?,
    val source: String = "unknown",  // "camera" or "gallery" (for analytics)
    val error: String? = null  // Error message if processing failed
)

/**
 * Platform-specific image picker
 * Provides camera capture and gallery selection
 */
expect class ImagePicker {
    /**
     * Launch camera to capture a photo
     * Callback receives result or null if cancelled/error
     */
    fun launchCamera(onResult: (ImagePickerResult?) -> Unit)

    /**
     * Launch gallery/photo picker to select an existing image
     * Callback receives result or null if cancelled/error
     */
    fun launchGallery(onResult: (ImagePickerResult?) -> Unit)
}

/**
 * Remember and create ImagePicker instance
 */
@Composable
expect fun rememberImagePicker(): ImagePicker
