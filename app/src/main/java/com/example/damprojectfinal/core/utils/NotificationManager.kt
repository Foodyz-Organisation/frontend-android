package com.example.damprojectfinal.core.utils

import android.content.Context
import android.util.Log
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.dto.professional.UpdateProfessionalRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manages Firebase Cloud Messaging (FCM) tokens and syncing with backend
 */
class NotificationManager(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private val TAG = "NotificationManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // User API service (for updating user profile)
    private val userApiService by lazy { 
        com.example.damprojectfinal.core.api.UserApiService(tokenManager) 
    }
    
    // Professional API service (for updating pro profile)
    private val professionalApiService by lazy { 
        com.example.damprojectfinal.core.api.ProfessionalApiService(
            client = com.example.damprojectfinal.core.`object`.KtorClient.client,
            tokenManager = tokenManager
        )
    }

    /**
     * initializes FCM and syncs token with backend if user is logged in
     */
    fun initialize() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "🔥 Firebase Token: $token")
            
            // Sync with backend
            syncTokenWithBackend(token)
        }
        
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = context.getString(com.example.damprojectfinal.R.string.default_notification_channel_id)
            val channelName = "Foodyz Notifications"
            val channelDescription = "Notifications for orders, messages, and updates"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Notification Channel created: $channelId")
        }
    }

    /**
     * Syncs the given FCM token with the backend for the current user
     */
    fun syncTokenWithBackend(fcmToken: String) {
        scope.launch {
            try {
                // Check if user is logged in
                val accessToken = tokenManager.getAccessTokenAsync()
                if (accessToken.isNullOrEmpty()) {
                    Log.d(TAG, "User not logged in, skipping FCM token sync")
                    return@launch
                }

                val userId = tokenManager.getUserIdFlow().first()
                val role = tokenManager.getUserRole().first()
                
                if (userId.isNullOrEmpty()) {
                     Log.w(TAG, "No user ID found, skipping sync")
                     return@launch
                }
                
                Log.i(TAG, "Syncing FCM token for $role ($userId)")

                if (role == "professional") {
                    // Update Professional
                    try {
                        professionalApiService.update(
                            id = userId,
                            request = UpdateProfessionalRequest(fcmToken = fcmToken)
                        )
                        Log.i(TAG, "✅ FCM Token updated for Professional")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to update Professional FCM token: ${e.message}")
                    }
                } else {
                    // Update User (User or Client)
                    try {
                        userApiService.updateProfile(
                            request = UpdateUserRequest(fcmToken = fcmToken),
                            token = accessToken,
                            userId = userId
                        )
                        Log.i(TAG, "✅ FCM Token updated for User")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to update User FCM token: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error syncing FCM token", e)
            }
        }
    }
}
