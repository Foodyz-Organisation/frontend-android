package com.example.damprojectfinal.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Image compression utility for restaurant permit uploads
 * Ensures images are under 800 KB for OCR API processing
 */
object ImageCompressor {
    
    private const val TAG = "ImageCompressor"
    private const val MAX_DIMENSION = 1920 // Good for OCR text recognition
    private const val TARGET_SIZE_KB = 800 // Stay under 1 MB API limit
    private const val MIN_QUALITY = 20 // Don't go below 20% quality
    
    /**
     * Compress image from URI to base64 string
     * 
     * @param context Application context
     * @param uri Image URI from gallery/camera
     * @return Base64 string with data URI prefix
     * @throws Exception if compression fails
     */
    fun compressImageToBase64(context: Context, uri: Uri): String {
        Log.d(TAG, "🔄 Starting image compression for: $uri")
        
        try {
            // Step 1: Load bitmap from URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Failed to open input stream")
            
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                throw IOException("Failed to decode bitmap from URI")
            }
            
            // Log original size
            val originalSizeKB = estimateBitmapSizeKB(originalBitmap, 100)
            Log.d(TAG, "📊 Original image: ${originalBitmap.width}x${originalBitmap.height}, ~$originalSizeKB KB")
            
            // Step 2: Fix orientation (if needed)
            val rotatedBitmap = fixOrientation(context, uri, originalBitmap)
            
            // Step 3: Resize if dimensions exceed max
            val resizedBitmap = resizeIfNeeded(rotatedBitmap)
            
            // Step 4: Compress with quality adjustment
            val compressedBase64 = compressToTargetSize(resizedBitmap)
            
            // Step 5: Cleanup
            if (originalBitmap != rotatedBitmap) originalBitmap.recycle()
            if (rotatedBitmap != resizedBitmap) rotatedBitmap.recycle()
            resizedBitmap.recycle()
            
            // Calculate final size
            val finalSizeKB = (compressedBase64.length * 3 / 4) / 1024
            Log.d(TAG, "✅ Compression complete! Final size: $finalSizeKB KB")
            
            return "data:image/jpeg;base64,$compressedBase64"
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Compression failed: ${e.message}", e)
            throw Exception("Failed to compress image: ${e.message}")
        }
    }
    
    /**
     * Fix image orientation based on EXIF data
     */
    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Could not fix orientation: ${e.message}")
            return bitmap
        }
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Resize bitmap if it exceeds max dimensions
     */
    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // Check if resize is needed
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            Log.d(TAG, "📐 No resize needed: ${width}x${height}")
            return bitmap
        }
        
        // Calculate new dimensions maintaining aspect ratio
        val ratio = if (width > height) {
            MAX_DIMENSION.toFloat() / width
        } else {
            MAX_DIMENSION.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        Log.d(TAG, "📐 Resizing from ${width}x${height} to ${newWidth}x${newHeight}")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Compress bitmap to meet target size
     */
    private fun compressToTargetSize(bitmap: Bitmap): String {
        var quality = 85 // Start with good quality
        var outputStream: ByteArrayOutputStream
        
        do {
            outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val sizeKB = outputStream.size() / 1024
            Log.d(TAG, "🗜️ Trying quality $quality%: $sizeKB KB")
            
            if (outputStream.size() <= TARGET_SIZE_KB * 1024) {
                // Success! Size is acceptable
                break
            }
            
            quality -= 10 // Reduce quality by 10%
            
        } while (quality >= MIN_QUALITY)
        
        // Convert to base64
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Estimate bitmap size in KB
     */
    private fun estimateBitmapSizeKB(bitmap: Bitmap, quality: Int): Int {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.size() / 1024
    }
}

