package com.example.damprojectfinal.core.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ===== DTOs =====

data class ConversationDto(
    @SerializedName("_id") val id: String? = null,
    val kind: String? = null,
    val participants: List<String> = emptyList(),
    val title: String? = null,
    val meta: Any? = null, // Changed to Any to handle different formats
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastMessage: MessageDto? = null,
    val unreadCount: Int = 0
)

data class MessageDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("conversation") val conversationId: String? = null,
    @SerializedName("sender") val senderId: String? = null,
    val content: String? = null,
    val type: String = "text",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val meta: Any? = null // Changed from Map<String, String> to Any to handle different formats
)

data class CreateConversationDto(
    val kind: String,
    val participants: List<String>,
    val title: String? = null,
    val meta: Map<String, String>? = null
)

data class SendMessageDto(
    val content: String,
    val type: String = "text",
    val meta: Map<String, String>? = null
)

data class PeerDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val kind: String? = null  // "user" or "professional"
)

// ===== API Service Interface =====

interface ChatApiService {
    // Conversations (aligned with iOS usage)
    @GET("chat/conversations")
    suspend fun getConversations(
        @Header("Authorization") bearerToken: String
    ): List<ConversationDto>

    @POST("chat/conversations")
    suspend fun createConversation(
        @Header("Authorization") bearerToken: String,
        @Body dto: CreateConversationDto
    ): ConversationDto

    @GET("chat/conversations/{id}")
    suspend fun getConversation(
        @Header("Authorization") bearerToken: String,
        @Path("id") conversationId: String
    ): ConversationDto

    // Messages
    @GET("chat/conversations/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") bearerToken: String,
        @Path("id") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): List<MessageDto>

    @POST("chat/conversations/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") bearerToken: String,
        @Path("id") conversationId: String,
        @Body dto: SendMessageDto
    ): MessageDto

    // Peers
    @GET("chat/peers")
    suspend fun getPeers(
        @Header("Authorization") bearerToken: String
    ): List<PeerDto>

    // Delete conversation
    @DELETE("chat/conversations/{id}")
    suspend fun deleteConversation(
        @Header("Authorization") bearerToken: String,
        @Path("id") conversationId: String
    ): retrofit2.Response<Unit>
}
