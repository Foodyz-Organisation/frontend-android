package com.example.damprojectfinal.core.api

import android.os.Build
import com.example.damprojectfinal.BuildConfig

object BaseUrlProvider {

    // ⚙️ CONFIGURATION FLAG: Set to true to use Render URL even in debug mode
    // Set to false to use localhost URLs in development
    private const val FORCE_PRODUCTION_URL_IN_DEBUG = true
    
    // Production URL (Render)
    private const val PRODUCTION_BASE_URL = "https://foodyz-backend.onrender.com"
    
    // Development URLs
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:3000"
    private const val REAL_DEVICE_BASE_URL = "http://192.168.1.147:3000"

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("Emulator") ||
                Build.BRAND.contains("google_sdk") ||
                Build.MODEL.contains("sdk") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
    }

    /**
     * Main BASE_URL - use this everywhere in your app
     * Automatically switches between production (Render) and development (localhost)
     * - Production: Uses Render URL (https://foodyz-backend.onrender.com)
     * - Development: Uses localhost URL based on device type (emulator vs real device)
     * 
     * Note: If FORCE_PRODUCTION_URL_IN_DEBUG is true, Render URL will be used even in debug mode
     */
    val BASE_URL: String by lazy {
        val url = if (BuildConfig.DEBUG && !FORCE_PRODUCTION_URL_IN_DEBUG) {
            // Development mode: use localhost URLs (only if not forcing production)
            val isEmu = isEmulator()
            android.util.Log.d("BaseUrlProvider", "🔍 Development mode detected")
            android.util.Log.d("BaseUrlProvider", "🔍 Auto-detection: isEmulator = $isEmu")
            if (isEmu) {
                android.util.Log.i("BaseUrlProvider", "📱 Using EMULATOR URL: $EMULATOR_BASE_URL")
                EMULATOR_BASE_URL
            } else {
                android.util.Log.i("BaseUrlProvider", "📲 Using REAL DEVICE URL: $REAL_DEVICE_BASE_URL")
                android.util.Log.w("BaseUrlProvider", "⚠️ Make sure your computer IP is correct: $REAL_DEVICE_BASE_URL")
                REAL_DEVICE_BASE_URL
            }
        } else {
            // Production mode OR forced production in debug: use Render URL
            if (BuildConfig.DEBUG && FORCE_PRODUCTION_URL_IN_DEBUG) {
                android.util.Log.i("BaseUrlProvider", "🚀 Debug mode with FORCE_PRODUCTION: Using RENDER URL: $PRODUCTION_BASE_URL")
            } else {
                android.util.Log.i("BaseUrlProvider", "🚀 Production mode: Using RENDER URL: $PRODUCTION_BASE_URL")
            }
            PRODUCTION_BASE_URL
        }
        android.util.Log.i("BaseUrlProvider", "✅ Final BASE_URL selected: $url")
        url
    }
    
    /**
     * BASE_URL with trailing slash (for Retrofit)
     */
    val BASE_URL_WITH_SLASH: String by lazy {
        if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
    }
    
    /**
     * Get full image URL from a path (handles both Supabase URLs and legacy relative paths)
     * 
     * After Supabase migration:
     * - New uploads return full Supabase URLs (e.g., https://xxx.supabase.co/storage/v1/object/public/...)
     * - These URLs are returned directly without modification
     * - Legacy relative paths (e.g., /uploads/image.jpg) are still supported for backward compatibility
     * 
     * @param path Image path from API response (can be full Supabase URL or relative path)
     * @return Full URL ready to use in image loading libraries, or null if path is null/empty
     */
    fun getFullImageUrl(path: String?): String? {
        if (path == null || path.isEmpty()) return null
        // If it's already a full URL (Supabase or any http/https URL), use it directly
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            // Legacy support: construct full URL from relative path
            // This handles old data that might still have relative paths
            "$BASE_URL_WITH_SLASH${path.removePrefix("/")}"
        }
    }
    
    /**
     * Get current configuration info (for debugging)
     */
    fun getConfigInfo(): String {
        return buildString {
            append("BaseUrlProvider Configuration:\n")
            append("  Environment: ${if (BuildConfig.DEBUG) "Development" else "Production"}\n")
            if (BuildConfig.DEBUG) {
                append("  Detected device: ${if (isEmulator()) "Emulator" else "Real Device"}\n")
            }
            append("  Current BASE_URL: $BASE_URL\n")
        }
    }
}
