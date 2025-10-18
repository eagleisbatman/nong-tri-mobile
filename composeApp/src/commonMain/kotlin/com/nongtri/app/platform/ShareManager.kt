package com.nongtri.app.platform

/**
 * Platform-specific sharing functionality
 */
expect class ShareManager {
    /**
     * Share text content using the platform's native share sheet
     */
    fun shareText(text: String, title: String = "Share")

    /**
     * Share an image using the platform's native share sheet
     * @param imageBytes The image data as byte array
     * @param fileName The name for the shared file
     */
    fun shareImage(imageBytes: ByteArray, fileName: String = "screenshot.png")
}
