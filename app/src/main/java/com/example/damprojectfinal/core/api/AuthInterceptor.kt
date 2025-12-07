package com.example.damprojectfinal.core.api

import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val TAG = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var requestBuilder = originalRequest.newBuilder()

        val userId = tokenManager.getUserId()
        val userType = tokenManager.getUserType() // This now correctly calls the blocking method

        if (userId != null) {
            requestBuilder.header("x-user-id", userId)
            Log.d(TAG, "Adding x-user-id header: $userId")
        } else {
            Log.w(TAG, "No userId found in TokenManager. Proceeding without x-user-id header for some requests.")
        }

        // --- NEW: Add x-owner-type header if available ---
        if (userType != null) {
            // Convert role format: "USER" -> "UserAccount", "PROFESSIONAL" -> "ProfessionalAccount"
            val ownerType = when (userType.uppercase()) {
                "USER" -> "UserAccount"
                "PROFESSIONAL" -> "ProfessionalAccount"
                else -> userType // Fallback to original value if unknown
            }
            requestBuilder.header("x-owner-type", ownerType)
            Log.d(TAG, "Adding x-owner-type header: $ownerType (original: $userType)")
        } else {
            Log.w(TAG, "No userType found in TokenManager. Proceeding without x-owner-type header for some requests.")
        }
        // --- END NEW ---

        val newRequest = requestBuilder.build()
        Log.d(TAG, "Intercepted request to URL: ${newRequest.url} with headers: ${newRequest.headers}")

        return chain.proceed(newRequest)
    }
}
