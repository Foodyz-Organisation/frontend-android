package com.example.damprojectfinal.user.feature_notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.TokenManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.UserRepository

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, ORDER, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId = remember { tokenManager.getUserIdBlocking() ?: "unknown" }
    
    // State for profile picture URL
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    
    // Fetch user profile picture
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && currentUserId != "unknown") {
            try {
                val token = tokenManager.getAccessTokenAsync()
                if (!token.isNullOrEmpty()) {
                    val userApiService = UserApiService(tokenManager)
                    val userRepository = UserRepository(userApiService)
                    val user = userRepository.getUserById(currentUserId, token)
                    profilePictureUrl = user.profilePictureUrl
                }
            } catch (e: Exception) {
                Log.e("NotificationsScreen", "Error fetching profile picture: ${e.message}")
            }
        }
    }

    // Mock notifications data - replace with ViewModel later
    val notifications = remember {
        listOf(
            NotificationItem(
                id = "1",
                title = "New Follower",
                message = "Alex Smith started following you",
                time = "2h ago",
                isRead = false,
                type = NotificationType.FOLLOW
            ),
            NotificationItem(
                id = "2",
                title = "Post Liked",
                message = "Your post got 10 likes!",
                time = "5h ago",
                isRead = false,
                type = NotificationType.LIKE
            ),
            NotificationItem(
                id = "3",
                title = "Order Confirmed",
                message = "Order #1001 has been confirmed",
                time = "1d ago",
                isRead = true,
                type = NotificationType.ORDER
            ),
            NotificationItem(
                id = "4",
                title = "New Comment",
                message = "Sarah commented on your post",
                time = "2d ago",
                isRead = true,
                type = NotificationType.COMMENT
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navController = navController,
                currentRoute = UserRoutes.NOTIFICATIONS_SCREEN,
                openDrawer = {
                    navController.navigate("user_menu")
                },
                onSearchClick = { /* TODO: Implement search */ },
                onProfileClick = { userId ->
                    navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/$userId")
                },
                onReelsClick = {
                    navController.navigate(UserRoutes.REELS_SCREEN)
                },
                currentUserId = currentUserId,
                onLogoutClick = { /* TODO: Implement logout */ },
                profilePictureUrl = profilePictureUrl
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2A37)
                )
                TextButton(onClick = { /* Mark all as read */ }) {
                    Text(
                        text = "Mark all as read",
                        color = Color(0xFFF59E0B),
                        fontSize = 14.sp
                    )
                }
            }

            // Notifications List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF9C4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.LIKE -> Color(0xFFFFEBEE)
                            NotificationType.COMMENT -> Color(0xFFE3F2FD)
                            NotificationType.FOLLOW -> Color(0xFFE8F5E9)
                            NotificationType.ORDER -> Color(0xFFFFF3E0)
                            NotificationType.SYSTEM -> Color(0xFFF3E5F5)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.LIKE -> Color(0xFFE91E63)
                        NotificationType.COMMENT -> Color(0xFF2196F3)
                        NotificationType.FOLLOW -> Color(0xFF4CAF50)
                        NotificationType.ORDER -> Color(0xFFFF9800)
                        NotificationType.SYSTEM -> Color(0xFF9C27B0)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = Color(0xFF1F2A37)
                )
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107))
                )
            }
        }
    }
}

