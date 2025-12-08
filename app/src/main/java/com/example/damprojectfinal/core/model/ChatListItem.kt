package com.example.damprojectfinal.core.model

data class ChatListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val updatedTime: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null,
    val initials: String = ""
)
