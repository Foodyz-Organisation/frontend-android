package com.example.damprojectfinal.core.api

import android.os.Build
object BaseUrlProvider {

    // ============================================
    // PRODUCTION CONFIGURATION
    // ============================================
    // Set this to true to use production backend (Render)
    // Set this to false to use local development backend
    private const val USE_PRODUCTION = true
    
    // Your Render deployment URL (replace with your actual Render URL)
    // Example: "https://your-app-name.onrender.com"
    // Make sure to include the protocol (https://) and no trailing slash
    private const val PRODUCTION_BASE_URL = "https://backend-nest-08qw.onrender.com"
    
    // ============================================
    // LOCAL DEVELOPMENT CONFIGURATION
    // ============================================
    private const val USE_AUTO_DETECTION = true
    private const val MANUAL_BASE_URL = "http://10.0.2.2:3000"
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:3000"
    private const val REAL_DEVICE_BASE_URL = "http://192.168.1.103:3000"

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
     * Automatically switches between production (Render) and local development
     */
    val BASE_URL: String by lazy {
        val url = if (USE_PRODUCTION) {
            // Production mode: Use Render deployment
            android.util.Log.i("BaseUrlProvider", "🚀 Using PRODUCTION URL (Render): $PRODUCTION_BASE_URL")
            PRODUCTION_BASE_URL
        } else {
            // Development mode: Use local backend
            when {
                USE_AUTO_DETECTION -> {
                    val isEmu = isEmulator()
                    android.util.Log.d("BaseUrlProvider", "🔍 Auto-detection: isEmulator = $isEmu")
                    if (isEmu) {
                        android.util.Log.i("BaseUrlProvider", "📱 Using EMULATOR URL: $EMULATOR_BASE_URL")
                        EMULATOR_BASE_URL
                    } else {
                        android.util.Log.i("BaseUrlProvider", "📲 Using REAL DEVICE URL: $REAL_DEVICE_BASE_URL")
                        android.util.Log.w("BaseUrlProvider", "⚠️ Make sure your computer IP is correct: $REAL_DEVICE_BASE_URL")
                        REAL_DEVICE_BASE_URL
                    }
                }
                else -> {
                    android.util.Log.i("BaseUrlProvider", "🔧 Using MANUAL URL: $MANUAL_BASE_URL")
                    MANUAL_BASE_URL
                }
            }
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
     * Get full image URL from a relative path
     */
    fun getFullImageUrl(path: String?): String? {
        if (path == null || path.isEmpty()) return null
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            "$BASE_URL_WITH_SLASH${path.removePrefix("/")}"
        }
    }
    
    /**
     * Get current configuration info (for debugging)
     */
    fun getConfigInfo(): String {
        return buildString {
            append("BaseUrlProvider Configuration:\n")
            append("  Production mode: $USE_PRODUCTION\n")
            if (USE_PRODUCTION) {
                append("  Production URL: $PRODUCTION_BASE_URL\n")
            } else {
                append("  Auto-detection: $USE_AUTO_DETECTION\n")
                if (USE_AUTO_DETECTION) {
                    append("  Detected device: ${if (isEmulator()) "Emulator" else "Real Device"}\n")
                }
            }
            append("  Current BASE_URL: $BASE_URL\n")
        }
    }
}
