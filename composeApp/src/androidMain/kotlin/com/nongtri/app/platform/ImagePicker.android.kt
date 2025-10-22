package com.nongtri.app.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

actual class ImagePicker(private val context: Context) {
    private lateinit var activity: ComponentActivity
    
    // Store callbacks for camera and gallery results
    companion object {
        var cameraResultCallback: ((ImagePickerResult?) -> Unit)? = null
        var galleryResultCallback: ((ImagePickerResult?) -> Unit)? = null
        var cameraLauncher: ((Uri) -> Unit)? = null
        var galleryLauncher: (() -> Unit)? = null
        
        // Temporary URI for camera capture
        var pendingCameraUri: Uri? = null
    }
    
    fun initialize(activity: ComponentActivity) {
        this.activity = activity
    }
    
    /**
     * Launch camera to capture a photo
     */
    actual fun launchCamera(onResult: (ImagePickerResult?) -> Unit) {
        try {
            // Create a temporary file for the captured image
            val imageFile = createTempImageFile()
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            
            // Store URI and callback
            pendingCameraUri = imageUri
            cameraResultCallback = onResult
            
            println("[ImagePicker] Launching camera with URI: $imageUri")
            
            // Launch camera via MainActivity's launcher
            cameraLauncher?.invoke(imageUri)
        } catch (e: Exception) {
            println("[ImagePicker] Error launching camera: ${e.message}")
            e.printStackTrace()
            onResult(null)
        }
    }
    
    /**
     * Launch gallery/photo picker to select an existing image
     */
    actual fun launchGallery(onResult: (ImagePickerResult?) -> Unit) {
        try {
            galleryResultCallback = onResult
            
            println("[ImagePicker] Launching gallery picker")
            
            // Launch gallery via MainActivity's launcher
            galleryLauncher?.invoke()
        } catch (e: Exception) {
            println("[ImagePicker] Error launching gallery: ${e.message}")
            e.printStackTrace()
            onResult(null)
        }
    }
    
    /**
     * Process camera capture result
     */
    fun onCameraResult(success: Boolean) {
        val callback = cameraResultCallback
        val imageUri = pendingCameraUri
        
        try {
            if (success && imageUri != null) {
                println("[ImagePicker] Camera capture successful, processing image...")
                val result = processImageUri(imageUri)
                callback?.invoke(result)
            } else {
                println("[ImagePicker] Camera capture cancelled or failed")
                callback?.invoke(null)
            }
        } catch (e: Exception) {
            println("[ImagePicker] Error processing camera result: ${e.message}")
            e.printStackTrace()
            callback?.invoke(null)
        } finally {
            // Clean up
            cameraResultCallback = null
            pendingCameraUri = null
        }
    }
    
    /**
     * Process gallery selection result
     */
    fun onGalleryResult(uri: Uri?) {
        val callback = galleryResultCallback
        
        try {
            if (uri != null) {
                println("[ImagePicker] Gallery image selected: $uri")
                val result = processImageUri(uri)
                callback?.invoke(result)
            } else {
                println("[ImagePicker] Gallery selection cancelled")
                callback?.invoke(null)
            }
        } catch (e: Exception) {
            println("[ImagePicker] Error processing gallery result: ${e.message}")
            e.printStackTrace()
            callback?.invoke(null)
        } finally {
            galleryResultCallback = null
        }
    }
    
    /**
     * Process image URI and create ImagePickerResult
     */
    private fun processImageUri(uri: Uri): ImagePickerResult? {
        try {
            // Load bitmap
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                println("[ImagePicker] Failed to decode bitmap from URI")
                return null
            }
            
            println("[ImagePicker] Original bitmap: ${bitmap.width}x${bitmap.height}")
            
            // Get original size
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            
            // Compress and convert to base64
            val (compressedBitmap, quality) = compressBitmapIfNeeded(bitmap)
            val base64Data = bitmapToBase64(compressedBitmap, quality)
            
            // Get file size estimate from base64 length
            val sizeBytes = (base64Data.length * 3L / 4) // Approximate size
            
            // Get MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            
            println("[ImagePicker] Processed image: ${compressedBitmap.width}x${compressedBitmap.height}, size: ${sizeBytes / 1024}KB, quality: $quality%")
            
            // Clean up bitmaps
            if (compressedBitmap != bitmap) {
                bitmap.recycle()
            }
            compressedBitmap.recycle()
            
            return ImagePickerResult(
                uri = uri.toString(),
                base64Data = base64Data,
                sizeBytes = sizeBytes,
                width = originalWidth,
                height = originalHeight,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            println("[ImagePicker] Error processing image: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Compress bitmap if it's too large (> 2MB target)
     * Returns (compressedBitmap, quality)
     */
    private fun compressBitmapIfNeeded(bitmap: Bitmap): Pair<Bitmap, Int> {
        // First, check if dimensions are too large
        val maxDimension = 2048
        var scaledBitmap = bitmap
        
        if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            
            println("[ImagePicker] Scaling down from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
        
        // Try different quality levels to find optimal compression
        val targetSize = 2 * 1024 * 1024 // 2MB target
        var quality = 90
        var outputStream: ByteArrayOutputStream
        
        do {
            outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val size = outputStream.size()
            
            println("[ImagePicker] Quality $quality%: ${size / 1024}KB")
            
            if (size <= targetSize || quality <= 50) {
                break
            }
            
            quality -= 10
        } while (true)
        
        return Pair(scaledBitmap, quality)
    }
    
    /**
     * Convert bitmap to base64 data URL
     */
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val bytes = outputStream.toByteArray()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }
    
    /**
     * Create a temporary file for camera capture
     */
    private fun createTempImageFile(): File {
        val timestamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timestamp}_"
        val storageDir = File(context.cacheDir, "shared_images")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
}

/**
 * Remember and create ImagePicker instance
 */
@Composable
actual fun rememberImagePicker(): ImagePicker {
    val context = LocalContext.current
    val imagePicker = remember { ImagePicker(context) }
    
    // Initialize with activity
    remember {
        val activity = context as? ComponentActivity
        if (activity != null) {
            imagePicker.initialize(activity)
        }
        imagePicker
    }
    
    return imagePicker
}
