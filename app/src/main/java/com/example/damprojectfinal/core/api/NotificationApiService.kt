package com.example.damprojectfinal.core.api

import com.example.damprojectfinal.core.dto.notifications.MarkAsReadResponse
import com.example.damprojectfinal.core.dto.notifications.NotificationResponse
import com.example.damprojectfinal.core.dto.notifications.NotificationsListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class NotificationApiService(private val tokenManager: TokenManager) {
    
    private val BASE_URL = BaseUrlProvider.BASE_URL
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
    }
    
    /**
     * Get all notifications for a user
     * GET /notifications/user/:userId
     */
    suspend fun getUserNotifications(userId: String, token: String): List<NotificationResponse> {
        val url = "$BASE_URL/notifications/user/$userId"
        val response = client.get(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        // Try to parse as NotificationsListResponse first, fallback to direct list
        return try {
            val listResponse: NotificationsListResponse = response.body()
            listResponse.notifications
        } catch (e: Exception) {
            // If parsing as NotificationsListResponse fails, try direct list
            response.body<List<NotificationResponse>>()
        }
    }
    
    /**
     * Get all notifications for a professional
     * GET /notifications/professional/:professionalId
     */
    suspend fun getProfessionalNotifications(professionalId: String, token: String): List<NotificationResponse> {
        val url = "$BASE_URL/notifications/professional/$professionalId"
        val response = client.get(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        // Try to parse as NotificationsListResponse first, fallback to direct list
        return try {
            val listResponse: NotificationsListResponse = response.body()
            listResponse.notifications
        } catch (e: Exception) {
            // If parsing as NotificationsListResponse fails, try direct list
            response.body<List<NotificationResponse>>()
        }
    }
    
    /**
     * Get unread notifications for a user
     * GET /notifications/unread?userId=:userId
     */
    suspend fun getUnreadUserNotifications(userId: String, token: String): List<NotificationResponse> {
        val url = "$BASE_URL/notifications/unread?userId=$userId"
        val response = client.get(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        // Try to parse as NotificationsListResponse first, fallback to direct list
        return try {
            val listResponse: NotificationsListResponse = response.body()
            listResponse.notifications
        } catch (e: Exception) {
            // If parsing as NotificationsListResponse fails, try direct list
            response.body<List<NotificationResponse>>()
        }
    }
    
    /**
     * Get unread notifications for a professional
     * GET /notifications/unread?professionalId=:professionalId
     */
    suspend fun getUnreadProfessionalNotifications(professionalId: String, token: String): List<NotificationResponse> {
        val url = "$BASE_URL/notifications/unread?professionalId=$professionalId"
        val response = client.get(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        // Try to parse as NotificationsListResponse first, fallback to direct list
        return try {
            val listResponse: NotificationsListResponse = response.body()
            listResponse.notifications
        } catch (e: Exception) {
            // If parsing as NotificationsListResponse fails, try direct list
            response.body<List<NotificationResponse>>()
        }
    }
    
    /**
     * Mark a notification as read
     * PATCH /notifications/:notificationId/read
     */
    suspend fun markAsRead(notificationId: String, token: String): MarkAsReadResponse {
        val url = "$BASE_URL/notifications/$notificationId/read"
        val response = client.patch(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        return response.body()
    }
    
    /**
     * Mark all notifications as read for a user
     * PATCH /notifications/read-all?userId=:userId
     */
    suspend fun markAllAsReadForUser(userId: String, token: String): MarkAsReadResponse {
        val url = "$BASE_URL/notifications/read-all?userId=$userId"
        val response = client.patch(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        return response.body()
    }
    
    /**
     * Mark all notifications as read for a professional
     * PATCH /notifications/read-all?professionalId=:professionalId
     */
    suspend fun markAllAsReadForProfessional(professionalId: String, token: String): MarkAsReadResponse {
        val url = "$BASE_URL/notifications/read-all?professionalId=$professionalId"
        val response = client.patch(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        return response.body()
    }
}

