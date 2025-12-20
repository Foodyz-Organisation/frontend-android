package com.example.damprojectfinal.core.dto.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val _id: String,
    val userId: String? = null,
    val professionalId: String? = null,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val updatedAt: String,
    
    // Entity references
    val eventId: EntityReference? = null,
    val postId: EntityReference? = null,
    val dealId: EntityReference? = null,
    val reclamationId: EntityReference? = null,
    val messageId: EntityReference? = null,
    val conversationId: EntityReference? = null,
    val orderId: EntityReference? = null,
    
    // Metadata
    val metadata: NotificationMetadata? = null
)

@Serializable
data class EntityReference(
    val _id: String,
    // Additional fields can be added based on entity type
    // For now, we'll use the _id for navigation
)

@Serializable
data class NotificationMetadata(
    val eventName: String? = null,
    val eventDate: String? = null,
    val postCaption: String? = null,
    val dealName: String? = null,
    val restaurantName: String? = null,
    val reclamationStatus: String? = null,
    val senderName: String? = null,
    val messagePreview: String? = null,
    val senderId: String? = null
)

@Serializable
data class NotificationsListResponse(
    val notifications: List<NotificationResponse>
)

@Serializable
data class MarkAsReadResponse(
    val success: Boolean = true,
    val message: String? = null
)

