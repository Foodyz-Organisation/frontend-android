package com.example.damprojectfinal.model

data class ChatMessage(
    val conversationId: String,
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int,
    val profileImage: Int,
    val online: Boolean
)
