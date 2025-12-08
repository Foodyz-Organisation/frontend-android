package com.example.damprojectfinal.core.api

import android.os.Build

/**
 * Centralized BASE_URL configuration for the entire app.
 * 
 * To switch between emulator and real device:
 * 1. For emulator: Use "http://10.0.2.2:3000"
 * 2. For real device: Replace with your computer's local IP (e.g., "http://192.168.1.15:3000")
 * 
 * You can also set USE_AUTO_DETECTION = true to automatically detect emulator vs real device
 */
object BaseUrlProvider {
    
    // ============================================
    // ⚙️ CONFIGURATION - Change these values
    // ============================================
    
    /**
     * Set to true to auto-detect emulator vs real device
     * Set to false to use MANUAL_BASE_URL
     * 
     * 💡 TIP: If testing on real device fails, try:
     *   1. Set USE_AUTO_DETECTION = false
     *   2. Set MANUAL_BASE_URL = "http://10.0.2.2:3000" (for emulator)
     *   3. Or set MANUAL_BASE_URL = "http://172.16.0.109:3000" (for real device with correct IP)
     */
    private const val USE_AUTO_DETECTION = true
    
    /**
     * Manual BASE_URL (used when USE_AUTO_DETECTION = false)
     * Change this to your computer's IP when testing on real device
     * Example: "http://192.168.1.15:3000"
     */
    private const val MANUAL_BASE_URL = "http://10.0.2.2:3000"
    
    /**
     * Emulator BASE_URL (localhost from emulator's perspective)
     */
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:3000"
    
    /**
     * Real device BASE_URL (your computer's local IP)
     * ⚠️ IMPORTANT: Replace with your actual computer IP address
     * 
     * 📋 How to find your computer's IP:
     *   - Windows: Open CMD → type "ipconfig" → look for "IPv4 Address" (e.g., 192.168.1.15)
     *   - Mac: Open Terminal → type "ifconfig" → look for "inet" under en0 or en1 (e.g., 192.168.1.15)
     *   - Linux: Open Terminal → type "ip addr" or "hostname -I" → look for your local IP
     * 
     * ✅ Make sure:
     *   1. Your backend server is running on port 3000
     *   2. Your phone and computer are on the same WiFi network
     *   3. Your computer's firewall allows connections on port 3000
     * 
     * Example: "http://172.20.10.2:3000" (replace with your actual IP)
     */
    //private const val REAL_DEVICE_BASE_URL = "http://172.20.10.2:3000"

    private const val REAL_DEVICE_BASE_URL = "http://10.255.50.186:3000"


    // ============================================
    
    /**
     * Detects if running on emulator
     */
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
     * Automatically switches between emulator and real device
     */
    val BASE_URL: String by lazy {
        val url = when {
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
            append("  Auto-detection: $USE_AUTO_DETECTION\n")
            if (USE_AUTO_DETECTION) {
                append("  Detected device: ${if (isEmulator()) "Emulator" else "Real Device"}\n")
            }
            append("  Current BASE_URL: $BASE_URL\n")
        }
    }
}
