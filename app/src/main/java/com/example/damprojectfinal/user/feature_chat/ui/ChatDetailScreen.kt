package com.example.damprojectfinal.user.feature_chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.user.feature_chat.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Data class to represent a message
data class Message(
    val id: Int,
    val text: String?,
    val isOutgoing: Boolean,
    val timestamp: String? = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    navController: NavController,
    conversationId: String? = null,
    currentUserId: String? = null,
    vm: ChatViewModel = viewModel()
) {

    val context = LocalContext.current
    var currentMessage by remember { mutableStateOf("") }
    val tokenManager = remember { TokenManager(context) }
    val accessToken by tokenManager.getAccessTokenFlow().collectAsState(initial = null)

    val httpMessages by vm.messages.collectAsState()
    val isConnected by vm.isSocketConnected.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val isSending by vm.isSendingMessage.collectAsState(initial = false)
    val isLoading by vm.isLoading.collectAsState(initial = false)

    // Get profile picture URL - use state to update when peers are loaded
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    
    // Observe peers state to update profile picture when enriched
    val peers by vm.peers.collectAsState()

    LaunchedEffect(conversationId, accessToken) {
        if (conversationId != null && accessToken != null) {
            // Load peers first to get profile pictures
            vm.loadPeers(accessToken, force = true, currentUserId = currentUserId)
            // Wait for peer enrichment to complete
            kotlinx.coroutines.delay(500)
            // Now get the profile picture URL
            profilePictureUrl = vm.getProfilePictureUrl(conversationId, currentUserId)
            
            vm.loadMessages(accessToken!!, conversationId, showLoading = true, currentUserId = currentUserId)
            vm.startMessagesAutoRefresh(accessToken!!, conversationId, currentUserId = currentUserId)
            vm.markConversationAsRead(conversationId, currentUserId)
            
            // Validate and Connect Socket
            val baseUrl = com.example.damprojectfinal.core.api.BaseUrlProvider.BASE_URL
                .replace("http://", "")
                .replace("https://", "")
                .substringBefore("/") // Clean up base URL for socket if needed
            
            // Using the full base URL string for now as logic might vary
            val fullBaseUrl = com.example.damprojectfinal.core.api.BaseUrlProvider.BASE_URL
            vm.initSocket(fullBaseUrl, accessToken!!, conversationId)
        }
    }
    
    // Update profile picture when peers change
    LaunchedEffect(peers, conversationId, currentUserId) {
        if (conversationId != null && peers.isNotEmpty()) {
            profilePictureUrl = vm.getProfilePictureUrl(conversationId, currentUserId)
        }
    }
    DisposableEffect(conversationId) {
        onDispose { 
            vm.stopMessagesAutoRefresh() 
            // vm.disconnectSocket() ? The ViewModel handles disconnect onCleared, but we might want to leave conversation here.
        }
    }

    val messages: List<Message> = remember(httpMessages, currentUserId) {
        httpMessages.mapIndexed { index, dto ->
            Message(
                id = index,
                text = dto.content,
                isOutgoing = currentUserId != null && dto.senderId == currentUserId,
                timestamp = dto.createdAt
            )
        }
    }

    // Auto-scroll to bottom when new messages arrive
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // Scroll to the last item (bottom of the list)
            delay(100) // Small delay to ensure layout is ready
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatHeader(
                chatName = chatName,
                profilePictureUrl = profilePictureUrl,
                isOnline = isConnected || messages.isNotEmpty(),
                onBack = { navController.popBackStack() },
                onCallClick = { /* TODO: Implement call */ },
                onVideoCallClick = { /* TODO: Implement video call */ },
                onMoreClick = { /* TODO: Implement more options */ }
            )
        },
        bottomBar = {
            MessageInputBar(
                text = currentMessage,
                onTextChange = { currentMessage = it },
                onSend = {
                    if (currentMessage.isNotBlank()) {
                        if (conversationId != null && accessToken != null) {
                            vm.sendMessageHttp(accessToken!!, conversationId, currentMessage)
                        }
                        currentMessage = ""
                    }
                },
                isSending = isSending
            )
        }
    ) { padding ->
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp
        val isSmallScreen = screenWidth < 360
        val isTablet = screenWidth >= 600
        val isLargeTablet = screenWidth >= 840

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding) // Apply Scaffold padding to respect topBar and bottomBar
        ) {
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (isSmallScreen) 8.dp else 16.dp,
                            vertical = if (isSmallScreen) 6.dp else 8.dp
                        ),
                    fontSize = if (isSmallScreen) 11.sp else 12.sp
                )
            }

            // Messages list - starts from top
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isLoading) {
                    Text(
                        text = "No messages yet. Say hello!",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = if (isSmallScreen) 14.sp else if (isTablet) 18.sp else 16.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = if (isSmallScreen) 12.dp else if (isTablet) 24.dp else 16.dp,
                            vertical = if (isSmallScreen) 6.dp else if (isTablet) 12.dp else 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 3.dp else 4.dp),
                        reverseLayout = false
                    ) {
                        items(messages) { message ->
                            if (message.isOutgoing) {
                                OutgoingMessage(
                                    text = message.text,
                                    timestamp = message.timestamp,
                                    isSmallScreen = isSmallScreen,
                                    isTablet = isTablet
                                )
                            } else {
                                IncomingMessage(
                                    text = message.text,
                                    timestamp = message.timestamp,
                                    isSmallScreen = isSmallScreen,
                                    isTablet = isTablet
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFFC107)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatHeader(
    chatName: String,
    profilePictureUrl: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth >= 600
    val isLargeTablet = screenWidth >= 840

    // Responsive sizes
    val profileSize = when {
        isSmallScreen -> 40.dp
        isTablet && !isLargeTablet -> 56.dp
        isLargeTablet -> 64.dp
        else -> 48.dp
    }

    val iconSize = when {
        isSmallScreen -> 20.dp
        isTablet && !isLargeTablet -> 28.dp
        isLargeTablet -> 32.dp
        else -> 24.dp
    }

    val onlineIndicatorSize = when {
        isSmallScreen -> 10.dp
        isTablet -> 14.dp
        else -> 12.dp
    }

    val horizontalPadding = when {
        isSmallScreen -> 6.dp
        isTablet -> 16.dp
        else -> 8.dp
    }

    val verticalPadding = when {
        isSmallScreen -> 6.dp
        isTablet -> 12.dp
        else -> 8.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.width(if (isSmallScreen) 6.dp else 8.dp))

        // Profile picture with online indicator
        Box(modifier = Modifier.size(profileSize)) {
            Box(
                modifier = Modifier
                    .size(profileSize)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB))
            ) {
                if (!profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = BaseUrlProvider.getFullImageUrl(profilePictureUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chatName.take(1).uppercase(),
                            fontSize = if (isSmallScreen) 18.sp else if (isTablet) 24.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
            // Online indicator
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(onlineIndicatorSize)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))

        // Name and online status
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chatName,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSmallScreen) 16.sp else if (isTablet) 22.sp else 18.sp,
                color = Color(0xFF1F2937)
            )
            Text(
                text = if (isOnline) "Online" else "Offline",
                fontSize = if (isSmallScreen) 12.sp else if (isTablet) 16.sp else 14.sp,
                color = Color(0xFF6B7280)
            )
        }

        // Action buttons
        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Call",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = onVideoCallClick) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = "Video Call",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun IncomingMessage(
    text: String?,
    timestamp: String?,
    isSmallScreen: Boolean = false,
    isTablet: Boolean = false
) {
    val maxWidth = if (isTablet) 0.65f else 0.75f
    val fontSize = if (isSmallScreen) 14.sp else if (isTablet) 18.sp else 16.sp
    val timestampSize = if (isSmallScreen) 11.sp else if (isTablet) 14.sp else 12.sp
    val padding = if (isSmallScreen) 12.dp else if (isTablet) 20.dp else 16.dp
    val verticalPadding = if (isSmallScreen) 8.dp else if (isTablet) 14.dp else 10.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isSmallScreen) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxWidth)
                    .background(Color.White, RoundedCornerShape(if (isTablet) 20.dp else 18.dp))
                    .padding(horizontal = padding, vertical = verticalPadding)
            ) {
                if (text != null) {
                    Text(
                        text = text,
                        color = Color(0xFF1F2937),
                        fontSize = fontSize
                    )
                }
            }
        }
        // Timestamp
        if (timestamp != null) {
            Text(
                text = formatMessageTime(timestamp),
                fontSize = timestampSize,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(
                    start = if (isSmallScreen) 10.dp else 12.dp,
                    top = if (isSmallScreen) 1.dp else 2.dp
                )
            )
        }
    }
}

@Composable
fun OutgoingMessage(
    text: String?,
    timestamp: String?,
    isSmallScreen: Boolean = false,
    isTablet: Boolean = false
) {
    val maxWidth = if (isTablet) 0.65f else 0.75f
    val fontSize = if (isSmallScreen) 14.sp else if (isTablet) 18.sp else 16.sp
    val timestampSize = if (isSmallScreen) 11.sp else if (isTablet) 14.sp else 12.sp
    val padding = if (isSmallScreen) 12.dp else if (isTablet) 20.dp else 16.dp
    val verticalPadding = if (isSmallScreen) 8.dp else if (isTablet) 14.dp else 10.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isSmallScreen) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(maxWidth)
                    .background(Color(0xFFFFC107), RoundedCornerShape(if (isTablet) 20.dp else 18.dp))
                    .padding(horizontal = padding, vertical = verticalPadding)
            ) {
                if (text != null) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = fontSize
                    )
                }
            }
        }
        // Timestamp
        if (timestamp != null) {
            Text(
                text = formatMessageTime(timestamp),
                fontSize = timestampSize,
                color = Color(0xFF9CA3AF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = if (isSmallScreen) 10.dp else 12.dp,
                        top = if (isSmallScreen) 1.dp else 2.dp
                    ),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth >= 600
    val isLargeTablet = screenWidth >= 840

    // Responsive sizes
    val iconButtonSize = when {
        isSmallScreen -> 36.dp
        isTablet && !isLargeTablet -> 48.dp
        isLargeTablet -> 56.dp
        else -> 40.dp
    }

    val iconSize = when {
        isSmallScreen -> 20.dp
        isTablet && !isLargeTablet -> 28.dp
        isLargeTablet -> 32.dp
        else -> 24.dp
    }

    val sendButtonSize = when {
        isSmallScreen -> 44.dp
        isTablet && !isLargeTablet -> 56.dp
        isLargeTablet -> 64.dp
        else -> 48.dp
    }

    val horizontalPadding = when {
        isSmallScreen -> 6.dp
        isTablet -> 16.dp
        else -> 8.dp
    }

    val verticalPadding = when {
        isSmallScreen -> 6.dp
        isTablet -> 12.dp
        else -> 8.dp
    }

    val inputMinHeight = when {
        isSmallScreen -> 36.dp
        isTablet -> 52.dp
        else -> 40.dp
    }

    val inputMaxHeight = when {
        isSmallScreen -> 100.dp
        isTablet -> 140.dp
        else -> 120.dp
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji button
        IconButton(
            onClick = { /* TODO: Open emoji picker */ },
            modifier = Modifier.size(iconButtonSize)
        ) {
            Text(
                text = "😊",
                fontSize = if (isSmallScreen) 20.sp else if (isTablet) 28.sp else 24.sp
            )
        }

        // Gallery button
        IconButton(
            onClick = { /* TODO: Open gallery */ },
            modifier = Modifier.size(iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = "Gallery",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(iconSize)
            )
        }

        // Attachment button
        IconButton(
            onClick = { /* TODO: Open attachments */ },
            modifier = Modifier.size(iconButtonSize)
        ) {
            Icon(
                imageVector = Icons.Filled.AttachFile,
                contentDescription = "Attach",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(iconSize)
            )
        }

        // Text input
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    "Type a message...",
                    color = Color(0xFF9CA3AF),
                    fontSize = if (isSmallScreen) 14.sp else if (isTablet) 18.sp else 16.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = inputMinHeight, max = inputMaxHeight),
            shape = RoundedCornerShape(if (isTablet) 28.dp else 24.dp),
            singleLine = false,
            maxLines = 4,
            textStyle = TextStyle(
                fontSize = if (isSmallScreen) 14.sp else if (isTablet) 18.sp else 16.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFFFFC107),
                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFFFFC107)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        Spacer(modifier = Modifier.width(if (isSmallScreen) 6.dp else 8.dp))

        // Send button
        IconButton(
            onClick = onSend,
            modifier = Modifier.size(sendButtonSize),
            enabled = !isSending && text.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank()) Color(0xFFFFC107) else Color(0xFFD1D5DB),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

fun formatMessageTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    return try {
        // Parse ISO 8601 format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp.substringBefore(".").substringBefore("Z"))
            ?: return timestamp

        val now = Date()
        val diff = now.time - date.time
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes m ago"
            hours < 24 -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                timeFormat.format(date)
            }
            days < 7 -> {
                val dayFormat = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
                dayFormat.format(date)
            }
            else -> {
                val fullFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                fullFormat.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}
