package com.example.damprojectfinal.user.feature_notifications.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.NotificationApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.notifications.NotificationResponse
import com.example.damprojectfinal.core.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val isProfessional: Boolean = false
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _markAsReadSuccess = MutableStateFlow(false)
    val markAsReadSuccess: StateFlow<Boolean> = _markAsReadSuccess.asStateFlow()
    
    private val _markAllAsReadSuccess = MutableStateFlow(false)
    val markAllAsReadSuccess: StateFlow<Boolean> = _markAllAsReadSuccess.asStateFlow()
    
    /**
     * Load notifications for user or professional
     */
    fun loadNotifications(userId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val notificationsList = if (isProfessional) {
                    repository.getProfessionalNotifications(userId)
                } else {
                    repository.getUserNotifications(userId)
                }
                
                _notifications.value = notificationsList
                _unreadCount.value = notificationsList.count { !it.isRead }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error loading notifications: ${e.message}")
                _errorMessage.value = e.message ?: "Failed to load notifications"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh notifications
     */
    fun refreshNotifications(userId: String) {
        loadNotifications(userId)
    }
    
    /**
     * Mark a notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val success = repository.markAsRead(notificationId)
                if (success) {
                    // Update local state
                    _notifications.value = _notifications.value.map { notification ->
                        if (notification._id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _unreadCount.value = _notifications.value.count { !it.isRead }
                    _markAsReadSuccess.value = true
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error marking notification as read: ${e.message}")
                _errorMessage.value = e.message ?: "Failed to mark notification as read"
            }
        }
    }
    
    /**
     * Mark all notifications as read
     */
    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            try {
                val success = if (isProfessional) {
                    repository.markAllAsReadForProfessional(userId)
                } else {
                    repository.markAllAsReadForUser(userId)
                }
                
                if (success) {
                    // Update local state
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                    _unreadCount.value = 0
                    _markAllAsReadSuccess.value = true
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error marking all as read: ${e.message}")
                _errorMessage.value = e.message ?: "Failed to mark all notifications as read"
            }
        }
    }
    
    /**
     * Reset success states
     */
    fun resetMarkAsReadSuccess() {
        _markAsReadSuccess.value = false
    }
    
    fun resetMarkAllAsReadSuccess() {
        _markAllAsReadSuccess.value = false
    }
    
    /**
     * Get notification icon based on type
     */
    fun getNotificationIcon(type: String): String {
        return when (type) {
            "event_created" -> "📅"
            "post_created", "post_liked", "post_commented" -> "📸"
            "deal_created" -> "🎁"
            "reclamation_created", "reclamation_updated", "reclamation_responded" -> "📋"
            "message_received", "conversation_started" -> "💬"
            "order_created", "order_confirmed", "order_delivered" -> "🛒"
            else -> "🔔"
        }
    }
    
    /**
     * Get notification color based on type
     */
    fun getNotificationColor(type: String): androidx.compose.ui.graphics.Color {
        return when (type) {
            "event_created" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            "post_created", "post_liked", "post_commented" -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Pink
            "deal_created" -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            "reclamation_created", "reclamation_updated", "reclamation_responded" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            "message_received", "conversation_started" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "order_created", "order_confirmed", "order_delivered" -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Yellow
            else -> androidx.compose.ui.graphics.Color(0xFF9CA3AF) // Gray
        }
    }
    
    companion object {
        fun Factory(context: Context, isProfessional: Boolean = false): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val tokenManager = TokenManager(context)
                    val apiService = NotificationApiService(tokenManager)
                    val repository = NotificationRepository(apiService, tokenManager)
                    return NotificationViewModel(repository, isProfessional) as T
                }
            }
        }
    }
}

