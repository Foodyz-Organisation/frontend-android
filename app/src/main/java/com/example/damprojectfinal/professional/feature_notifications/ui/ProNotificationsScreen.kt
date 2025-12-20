package com.example.damprojectfinal.professional.feature_notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.notifications.NotificationResponse
import com.example.damprojectfinal.user.feature_notifications.viewmodel.NotificationViewModel
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.damprojectfinal.professional.common._component.ProfessionalBottomNavigationBar
import com.example.damprojectfinal.professional.common._component.CustomProTopBarWithIcons
import com.example.damprojectfinal.core.retro.RetrofitClient
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProNotificationsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentProfessionalId = remember { tokenManager.getUserIdBlocking() ?: "unknown" }

    // Initialize ViewModel for professional
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(context, isProfessional = true)
    )
    
    // Collect state
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val markAllAsReadSuccess by viewModel.markAllAsReadSuccess.collectAsState()
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Profile picture state
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    
    // Get current route for bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    // Load professional profile picture for top app bar
    LaunchedEffect(currentProfessionalId) {
        if (currentProfessionalId.isNotEmpty() && currentProfessionalId != "unknown") {
            try {
                val prof = RetrofitClient.professionalApiService.getProfessionalAccount(currentProfessionalId)
                if (!prof.profilePictureUrl.isNullOrEmpty()) {
                    profilePictureUrl = prof.profilePictureUrl
                }
            } catch (e: Exception) {
                Log.e("ProNotificationsScreen", "Error fetching profile picture: ${e.message}")
            }
        }
    }
    
    // Load notifications on first launch
    LaunchedEffect(currentProfessionalId) {
        if (currentProfessionalId.isNotEmpty() && currentProfessionalId != "unknown") {
            viewModel.loadNotifications(currentProfessionalId)
        }
    }
    
    // Show success message when all notifications are marked as read
    LaunchedEffect(markAllAsReadSuccess) {
        if (markAllAsReadSuccess) {
            snackbarHostState.showSnackbar(
                message = "All notifications marked as read",
                duration = SnackbarDuration.Short
            )
            viewModel.resetMarkAllAsReadSuccess()
        }
    }
    
    // Handle notification click and navigate
    val onNotificationClick: (NotificationResponse) -> Unit = { notification ->
        // Mark as read first
        viewModel.markAsRead(notification._id)
        
        // Navigate based on notification type
        when (notification.type) {
            "event_created" -> {
                notification.eventId?._id?.let { eventId ->
                    navController.navigate("event_detail/$eventId")
                }
            }
            "post_created", "post_liked", "post_commented" -> {
                notification.postId?._id?.let { postId ->
                    navController.navigate("post_details_screen/$postId")
                }
            }
            "deal_created" -> {
                notification.dealId?._id?.let { dealId ->
                    navController.navigate("deals/$dealId")
                }
            }
            "reclamation_created", "reclamation_updated", "reclamation_responded" -> {
                notification.reclamationId?._id?.let { reclamationId ->
                    navController.navigate("restaurant_reclamation_detail/$reclamationId")
                }
            }
            "message_received", "conversation_started" -> {
                notification.conversationId?._id?.let { conversationId ->
                    val title = notification.metadata?.senderName ?: "Chat"
                    navController.navigate("chatDetail/$conversationId/$title/$currentProfessionalId")
                }
            }
            "order_created", "order_confirmed", "order_delivered" -> {
                notification.orderId?._id?.let { orderId ->
                    navController.navigate("pro_order_details/$orderId")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        },
        topBar = {
            CustomProTopBarWithIcons(
                professionalId = currentProfessionalId,
                navController = navController,
                profilePictureUrl = profilePictureUrl,
                onLogout = { /* TODO: implement logout */ },
                onMenuClick = {
                    navController.navigate("professional_menu/$currentProfessionalId")
                }
            )
        },
        bottomBar = {
            ProfessionalBottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                professionalId = currentProfessionalId
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
                Column {
                    Text(
                        text = "Notifications",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2A37)
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount unread",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                TextButton(
                    onClick = { 
                        if (currentProfessionalId.isNotEmpty() && currentProfessionalId != "unknown") {
                            viewModel.markAllAsRead(currentProfessionalId)
                        }
                    },
                    enabled = unreadCount > 0 && !isLoading
                ) {
                    Text(
                        text = "Mark all as read",
                        color = Color(0xFFF59E0B),
                        fontSize = 14.sp
                    )
                }
            }

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF59E0B))
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error loading notifications",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            Text(
                                text = errorMessage ?: "Unknown error",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { 
                                    if (currentProfessionalId.isNotEmpty() && currentProfessionalId != "unknown") {
                                        viewModel.refreshNotifications(currentProfessionalId)
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "No notifications yet",
                                fontSize = 18.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "You'll see notifications here when you have updates",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
                else -> {
                    // Notifications List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            ProNotificationCard(
                                notification = notification,
                                viewModel = viewModel,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "event_created" -> Icons.Filled.Event
        "post_created", "post_liked", "post_commented" -> Icons.Filled.PhotoCamera
        "deal_created" -> Icons.Filled.LocalOffer
        "reclamation_created", "reclamation_updated", "reclamation_responded" -> Icons.Filled.Assignment
        "message_received", "conversation_started" -> Icons.Filled.Message
        "order_created", "order_confirmed", "order_delivered" -> Icons.Filled.ShoppingCart
        else -> Icons.Filled.Notifications
    }
}

@Composable
fun ProNotificationCard(
    notification: NotificationResponse,
    viewModel: NotificationViewModel,
    onClick: () -> Unit
) {
    val iconColor = viewModel.getNotificationColor(notification.type)
    val icon = getNotificationIcon(notification.type)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
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
                    color = Color(0xFF6B7280),
                    maxLines = 2
                )
                Text(
                    text = formatNotificationTime(notification.createdAt),
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

fun formatNotificationTime(createdAt: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(createdAt)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> {
                val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        }
    } catch (e: Exception) {
        "Recently"
    }
}

