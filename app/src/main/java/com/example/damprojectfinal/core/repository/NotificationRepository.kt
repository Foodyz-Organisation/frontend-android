package com.example.damprojectfinal.core.repository

import com.example.damprojectfinal.core.api.NotificationApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.notifications.NotificationResponse

class NotificationRepository(
    private val apiService: NotificationApiService,
    private val tokenManager: TokenManager
) {
    
    suspend fun getUserNotifications(userId: String): List<NotificationResponse> {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        return apiService.getUserNotifications(userId, token)
    }
    
    suspend fun getProfessionalNotifications(professionalId: String): List<NotificationResponse> {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        return apiService.getProfessionalNotifications(professionalId, token)
    }
    
    suspend fun getUnreadUserNotifications(userId: String): List<NotificationResponse> {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        return apiService.getUnreadUserNotifications(userId, token)
    }
    
    suspend fun getUnreadProfessionalNotifications(professionalId: String): List<NotificationResponse> {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        return apiService.getUnreadProfessionalNotifications(professionalId, token)
    }
    
    suspend fun markAsRead(notificationId: String): Boolean {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        val response = apiService.markAsRead(notificationId, token)
        return response.success
    }
    
    suspend fun markAllAsReadForUser(userId: String): Boolean {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        val response = apiService.markAllAsReadForUser(userId, token)
        return response.success
    }
    
    suspend fun markAllAsReadForProfessional(professionalId: String): Boolean {
        val token = tokenManager.getAccessTokenAsync()
            ?: throw IllegalStateException("No access token available")
        val response = apiService.markAllAsReadForProfessional(professionalId, token)
        return response.success
    }
}

