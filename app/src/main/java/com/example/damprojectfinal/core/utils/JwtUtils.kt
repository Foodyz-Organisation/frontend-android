package com.example.damprojectfinal.core.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

/**
 * JWT Token utilities for validation
 */
object JwtUtils {
    private const val TAG = "JwtUtils"

    /**
     * Check if a JWT token is expired
     * Returns true if expired, false if still valid
     */
    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true

        try {
            // JWT format: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w(TAG, "Invalid JWT format")
                return true
            }

            // Decode payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val json = JSONObject(decodedString)

            // Get expiration time (exp claim is in seconds)
            val exp = json.optLong("exp", 0)
            if (exp == 0L) {
                Log.w(TAG, "No exp claim in token")
                return true
            }

            // Current time in seconds
            val currentTime = System.currentTimeMillis() / 1000

            val isExpired = currentTime >= exp
            
            if (isExpired) {
                Log.w(TAG, "Token expired at: $exp, current: $currentTime")
            } else {
                Log.d(TAG, "Token valid until: $exp")
            }

            return isExpired

        } catch (e: Exception) {
            Log.e(TAG, "Error checking token expiration", e)
            return true // Assume expired if we can't parse it
        }
    }
}
