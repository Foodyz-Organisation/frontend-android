package com.example.damprojectfinal.user.feature_chat.ui

import android.Manifest
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.ui.text.style.TextOverflow
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

data class Message(
    val id: Int,
    val text: String?,
    val isOutgoing: Boolean,
    val timestamp: String? = "",
    val sharedPostId: String? = null,
    val sharedPostCaption: String? = null,
    val sharedPostImage: String? = null
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

    val incomingCall by vm.incomingCall.collectAsState()
    val isInCall by vm.isInCall.collectAsState()
    val isVideoCall by vm.isVideoCall.collectAsState()
    val isMicMuted by vm.isMicMuted.collectAsState()
    val isVideoMuted by vm.isVideoMuted.collectAsState()
    val isSpeakerOn by vm.isSpeakerOn.collectAsState()
    
    val remoteVideoTrackStream by vm.remoteVideoTrackStream.collectAsState()
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    val peers by vm.peers.collectAsState()
    var pendingCallType by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionDialogMessage by remember { mutableStateOf("") }

    var hasAudioPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasAudioPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                android.util.Log.d("ChatDetailScreen", "ON_RESUME: Audio=$hasAudioPermission, Camera=$hasCameraPermission")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        android.util.Log.d("ChatDetailScreen", "Permission result: $permissions")
        
        if (permissions.all { it.value }) {
            android.util.Log.d("ChatDetailScreen", "All permissions granted, pendingCallType=$pendingCallType")
            when (pendingCallType) {
                "voice" -> {
                    if (conversationId != null) {
                        android.util.Log.d("ChatDetailScreen", "Starting voice call")
                        vm.startCall(conversationId, isVideo = false)
                    }
                }
                "video" -> {
                    if (conversationId != null) {
                        android.util.Log.d("ChatDetailScreen", "Starting video call")
                        vm.startCall(conversationId, isVideo = true)
                    }
                }
                "accept" -> {
                    android.util.Log.d("ChatDetailScreen", "Accepting call")
                    vm.acceptCall(isVideo = true)
                }
            }
            pendingCallType = null
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.joinToString()
            android.util.Log.e("ChatDetailScreen", "Permissions denied: $deniedPermissions")
            
            Toast.makeText(context, "Permission Denied. Please enable in Settings.", Toast.LENGTH_LONG).show()
            
             try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", context.packageName, null)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("ChatDetailScreen", "Failed to open settings", e)
            }
            pendingCallType = null
        }
    }

    LaunchedEffect(conversationId, accessToken) {
        if (conversationId != null && accessToken != null) {
            vm.initWebRtc(context)
            
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

    // State to hold fetched post details for messages without metadata
    var postsCache by remember { mutableStateOf<Map<String, com.example.damprojectfinal.core.dto.posts.PostResponse>>(emptyMap()) }
    
    val messages: List<Message> = remember(httpMessages, currentUserId, postsCache) {
        httpMessages.mapIndexed { index, dto ->
            // Check if message has shared post metadata
            var sharedPostId: String? = null
            var sharedPostCaption: String? = null
            var sharedPostImage: String? = null
            var isSharedPost = false
            
            // Safely extract meta data
            try {
                android.util.Log.d("ChatDetailScreen", "Processing message ${dto.id}: type=${dto.type}, content=${dto.content}, meta=${dto.meta}")
                
                when (val meta = dto.meta) {
                    is Map<*, *> -> {
                        android.util.Log.d("ChatDetailScreen", "Meta is Map with keys: ${meta.keys}")
                        sharedPostId = meta["sharedPostId"] as? String
                        sharedPostCaption = meta["sharedPostCaption"] as? String
                        sharedPostImage = meta["sharedPostImage"] as? String
                        android.util.Log.d("ChatDetailScreen", "Extracted: postId=$sharedPostId, caption=$sharedPostCaption, image=$sharedPostImage")
                    }
                    is String -> {
                        // Try to parse as JSON string
                        android.util.Log.d("ChatDetailScreen", "Meta is String: $meta")
                        try {
                            val jsonObject = org.json.JSONObject(meta)
                            sharedPostId = jsonObject.optString("sharedPostId").takeIf { it.isNotEmpty() }
                            sharedPostCaption = jsonObject.optString("sharedPostCaption").takeIf { it.isNotEmpty() }
                            sharedPostImage = jsonObject.optString("sharedPostImage").takeIf { it.isNotEmpty() }
                        } catch (e: Exception) {
                            android.util.Log.e("ChatDetailScreen", "Failed to parse meta as JSON: ${e.message}")
                        }
                    }
                    else -> {
                        android.util.Log.w("ChatDetailScreen", "Meta is ${meta?.javaClass?.simpleName ?: "null"}")
                    }
                }
                
                // AGGRESSIVE DETECTION: Check if this is a shared post message
                val isSharedPostByContent = dto.content?.contains("Shared a post", ignoreCase = true) == true
                val isSharedPostByType = dto.type == "shared_post" || dto.type == "post"
                
                if (isSharedPostByContent || isSharedPostByType) {
                    isSharedPost = true
                    android.util.Log.d("ChatDetailScreen", "Detected shared post message!")
                    
                    // If no metadata, try to extract from content or use cached data
                    if (sharedPostId == null) {
                        // Try to extract post ID from content patterns
                        dto.content?.let { content ->
                            val postIdPattern = "postId:([a-zA-Z0-9]+)".toRegex()
                            val match = postIdPattern.find(content)
                            if (match != null) {
                                sharedPostId = match.groupValues[1]
                                android.util.Log.d("ChatDetailScreen", "Extracted postId from content: $sharedPostId")
                            }
                        }
                        
                        // Check if we have cached post data for this message
                        dto.id?.let { messageId ->
                            postsCache[messageId]?.let { cachedPost ->
                                sharedPostId = cachedPost._id
                                sharedPostCaption = cachedPost.caption
                                sharedPostImage = if (cachedPost.mediaType == "reel" && cachedPost.thumbnailUrl != null) {
                                    cachedPost.thumbnailUrl
                                } else {
                                    cachedPost.mediaUrls.firstOrNull()
                                }
                                android.util.Log.d("ChatDetailScreen", "Using cached post data for message $messageId")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatDetailScreen", "Error parsing meta: ${e.message}", e)
            }
            
            Message(
                id = index,
                text = if (isSharedPost) null else dto.content, // Hide text for shared posts
                isOutgoing = currentUserId != null && dto.senderId == currentUserId,
                timestamp = dto.createdAt,
                sharedPostId = sharedPostId,
                sharedPostCaption = sharedPostCaption,
                sharedPostImage = sharedPostImage
            )
        }
    }
    
    // Fetch post details for shared post messages that don't have metadata
    LaunchedEffect(httpMessages) {
        val messagesToFetch = httpMessages.mapNotNull { dto ->
            val isSharedPost = dto.content?.contains("Shared a post", ignoreCase = true) == true || 
                               dto.type == "shared_post" || 
                               dto.type == "post"
            val hasNoMetadata = (dto.meta as? Map<*, *>)?.get("sharedPostId") == null
            
            if (isSharedPost && hasNoMetadata && dto.id != null) {
                // Try to extract post ID from content
                dto.content?.let { content ->
                    val postIdPattern = "postId:([a-zA-Z0-9]+)".toRegex()
                    val match = postIdPattern.find(content)
                    if (match != null) {
                        val extractedPostId = match.groupValues[1]
                        Pair(dto.id!!, extractedPostId)
                    } else null
                } ?: null
            } else null
        }
        
        if (messagesToFetch.isNotEmpty()) {
            android.util.Log.d("ChatDetailScreen", "Found ${messagesToFetch.size} shared post messages to fetch")
            
            // Fetch post details for each message
            messagesToFetch.forEach { (messageId, postId) ->
                try {
                    android.util.Log.d("ChatDetailScreen", "Fetching post $postId for message $messageId")
                    val postsApi = com.example.damprojectfinal.core.retro.RetrofitClient.postsApiService
                    val post = postsApi.getPostById(postId)
                    
                    // Cache the post data
                    postsCache = postsCache + (messageId to post)
                    android.util.Log.d("ChatDetailScreen", "Successfully fetched and cached post $postId")
                } catch (e: Exception) {
                    android.util.Log.e("ChatDetailScreen", "Failed to fetch post $postId: ${e.message}")
                }
            }
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

    // Permission rationale dialog
    if (showPermissionDialog) {
        PermissionRationaleDialog(
            message = permissionDialogMessage,
            onAllow = {
                showPermissionDialog = false
                // Request actual system permissions
                when (pendingCallType) {
                    "voice" -> {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.RECORD_AUDIO
                        ))
                    }
                    "video" -> {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        ))
                    }
                    "accept" -> {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        ))
                    }
                }
            },
            onDontAllow = {
                showPermissionDialog = false
                pendingCallType = null
                Toast.makeText(context, "Permissions are required to make calls", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Incoming call dialog
    if (incomingCall) {
        IncomingCallDialog(
            callerName = chatName,
            onAccept = {
                // Check permissions first
                val hasAudio = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (hasAudio && hasCamera) {
                    android.util.Log.d("ChatDetailScreen", "Permissions already granted, accepting call")
                    vm.acceptCall(isVideo = true)
                } else {
                    pendingCallType = "accept"
                    permissionDialogMessage = "Allow Foodyz to access your camera and microphone to accept this video call?"
                    showPermissionDialog = true
                }
            },
            onDecline = { vm.declineCall() }
        )
    }

    // Active call screen
    if (isInCall) {
        ActiveCallScreen(
            callerName = chatName,
            isVideoCall = isVideoCall,
            isMicMuted = isMicMuted,
            isVideoMuted = isVideoMuted,
            isSpeakerOn = isSpeakerOn,
            onToggleMic = { vm.toggleMic() },
            onToggleVideo = { vm.toggleVideo() },
            onSwitchCamera = { vm.switchCamera() },
            onToggleSpeaker = { vm.toggleSpeaker(context) },
            onEndCall = { vm.endCall() },
            onAttachLocalVideo = { renderer -> vm.attachLocalVideo(renderer) },

            onAttachRemoteVideo = { renderer -> vm.attachRemoteVideo(renderer) },
            remoteVideoTrackStream = remoteVideoTrackStream
        )
        return // Don't show chat UI during call
    }

    Scaffold(
        topBar = {
            ChatHeader(
                chatName = chatName,
                profilePictureUrl = profilePictureUrl,
                isOnline = isConnected || messages.isNotEmpty(),
                onBack = { navController.popBackStack() },
                onCallClick = {
                    if (conversationId != null) {
                        // Use the state we checked in ON_RESUME as a backup, but always check fresh
                        val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, 
                            Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        android.util.Log.d("ChatDetailScreen", "onCallClick: isGranted=$isGranted")

                        if (isGranted) {
                            vm.startCall(conversationId, isVideo = false)
                        } else {
                            pendingCallType = "voice"
                            permissionDialogMessage = "Allow Foodyz to access your microphone to make voice calls?"
                            showPermissionDialog = true
                        }
                    }
                },
                onVideoCallClick = {
                    if (conversationId != null) {
                        val hasAudio = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, 
                            Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        val hasCamera = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, 
                            Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        android.util.Log.d("ChatDetailScreen", "onVideoCallClick: Audio=$hasAudio, Camera=$hasCamera")

                        if (hasAudio && hasCamera) {
                            vm.startCall(conversationId, isVideo = true)
                        } else {
                            pendingCallType = "video"
                            permissionDialogMessage = "Allow Foodyz to access your camera and microphone to make video calls?"
                            showPermissionDialog = true
                        }
                    }
                },
                onMoreClick = { /* TODO: Implement more options */ }
            )
        },
        bottomBar = {
            var showEmojiPicker by remember { mutableStateOf(false) }
            
            Column {
                // Emoji Picker - positioned above the input bar
                if (showEmojiPicker) {
                    EmojiPicker(
                        onEmojiSelected = { emoji ->
                            currentMessage += emoji
                            showEmojiPicker = false
                        }
                    )
                }
                
                MessageInputBar(
                    text = currentMessage,
                    onTextChange = { currentMessage = it },
                    onSend = {
                        if (currentMessage.isNotBlank()) {
                            if (conversationId != null && accessToken != null) {
                                vm.sendMessageHttp(accessToken!!, conversationId, currentMessage)
                            }
                            currentMessage = ""
                            showEmojiPicker = false // Hide emoji picker after sending
                        }
                    },
                    isSending = isSending,
                    showEmojiPicker = showEmojiPicker,
                    onEmojiPickerToggle = { showEmojiPicker = !showEmojiPicker },
                    onEmojiSelected = { emoji ->
                        currentMessage += emoji
                        showEmojiPicker = false // Hide picker after selection
                    }
                )
            }
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
                            if (message.sharedPostId != null) {
                                // Shared post message with metadata - show image card
                                if (message.isOutgoing) {
                                    OutgoingSharedPostMessage(
                                        sharedPostId = message.sharedPostId!!,
                                        postCaption = message.sharedPostCaption,
                                        postImageUrl = message.sharedPostImage,
                                        timestamp = message.timestamp,
                                        isSmallScreen = isSmallScreen,
                                        isTablet = isTablet,
                                        onPostClick = {
                                            navController.navigate("${com.example.damprojectfinal.UserRoutes.POST_DETAILS_SCREEN}/${message.sharedPostId}")
                                        }
                                    )
                                } else {
                                    IncomingSharedPostMessage(
                                        sharedPostId = message.sharedPostId!!,
                                        postCaption = message.sharedPostCaption,
                                        postImageUrl = message.sharedPostImage,
                                        timestamp = message.timestamp,
                                        isSmallScreen = isSmallScreen,
                                        isTablet = isTablet,
                                        onPostClick = {
                                            navController.navigate("${com.example.damprojectfinal.UserRoutes.POST_DETAILS_SCREEN}/${message.sharedPostId}")
                                        }
                                    )
                                }
                            } else if (message.text != null) {
                                // Regular text message (excluding hidden shared post texts)
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
                            // If message has no text and no sharedPostId, it's filtered out (hidden)
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
    showEmojiPicker: Boolean = false,
    onEmojiPickerToggle: () -> Unit = {},
    onEmojiSelected: (String) -> Unit = {},
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
            onClick = onEmojiPickerToggle,
            modifier = Modifier.size(iconButtonSize)
        ) {
            Text(
                text = if (showEmojiPicker) "⌨️" else "😊", // Show keyboard icon when picker is open
                fontSize = if (isSmallScreen) 20.sp else if (isTablet) 28.sp else 24.sp
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

// ------------------------------------------------------
// 😊 Emoji Picker Component (Messenger-style)
// ------------------------------------------------------
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth >= 600
    
    // Common emojis organized by category (similar to Messenger)
    val emojis = listOf(
        // Smileys & People
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😜", "😝", "😎", "🤩", "🥳", "😏", "😒", "😞",
        "😔", "😟", "😕", "🙁", "😣", "😖", "😫", "😩", "🥺", "😢",
        "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱",
        "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶",
        "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱",
        "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷",
        "🤒", "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡", "💩",
        "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃", "😺", "😸", "😹",
        "😻", "😼", "😽", "🙀", "😿", "😾",
        
        // Gestures & Body Parts
        "👋", "🤚", "🖐", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
        "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍",
        "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝",
        "🙏", "✍️", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃",
        "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅", "👄",
        
        // Hearts & Emotions
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
        "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
        "⛎", "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐",
        "♑", "♒", "♓", "🆔", "⚛️", "🉑", "☢️", "☣️",
        
        // Food & Drink
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈",
        "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦",
        "🥬", "🥒", "🌶️", "🌽", "🥕", "🥔", "🍠", "🥐", "🥯", "🍞",
        "🥖", "🥨", "🧀", "🥚", "🍳", "🥞", "🥓", "🥩", "🍗", "🍖",
        "🦴", "🌭", "🍔", "🍟", "🍕", "🥪", "🥙", "🌮", "🌯", "🥗",
        "🥘", "🥫", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🍤",
        "🍙", "🍚", "🍘", "🍥", "🥠", "🥮", "🍢", "🍡", "🍧", "🍨",
        "🍦", "🥧", "🍰", "🎂", "🍮", "🍭", "🍬", "🍫", "🍿", "🍩",
        "🍪", "🌰", "🥜", "🍯", "🥛", "🍼", "☕", "🍵", "🧃", "🥤",
        "🍶", "🍺", "🍻", "🥂", "🍷", "🥃", "🍸", "🍹", "🧉", "🍾",
        "🧊",
        
        // Activities & Sports
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
        "🏓", "🏸", "🥅", "🏒", "🏑", "🥍", "🏏", "🥊", "⛳", "🏹",
        "🎣", "🥋", "🥌", "🎽", "🎿", "⛷️", "🏂", "🛷", "🎮",
        "🕹️", "🎲", "♟️", "🎯", "🎳", "🎰", "🎴", "🃏", "🀄", "🎭",
        "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸",
        "🎻", "🎲", "🎯", "🎳", "🎮", "🎰"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 200.dp else if (isTablet) 300.dp else 250.dp),
        shape = RoundedCornerShape(0.dp), // No rounded corners at top since it's in bottomBar
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emoji",
                    fontSize = if (isSmallScreen) 16.sp else if (isTablet) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Emoji Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(emojis.size) { index ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onEmojiSelected(emojis[index]) }
                            .background(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emojis[index],
                            fontSize = if (isSmallScreen) 20.sp else if (isTablet) 28.sp else 24.sp
                        )
                    }
                }
            }
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

@Composable
fun IncomingSharedPostMessage(
    sharedPostId: String,
    postCaption: String?,
    postImageUrl: String?,
    timestamp: String?,
    isSmallScreen: Boolean = false,
    isTablet: Boolean = false,
    onPostClick: () -> Unit
) {
    val maxWidth = if (isTablet) 0.65f else 0.75f
    val timestampSize = if (isSmallScreen) 11.sp else if (isTablet) 14.sp else 12.sp
    val cardHeight = if (isSmallScreen) 280.dp else if (isTablet) 400.dp else 320.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isSmallScreen) 2.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(maxWidth)
                    .height(cardHeight)
                    .clickable(onClick = onPostClick),
                shape = RoundedCornerShape(if (isTablet) 20.dp else 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Post Image/Video thumbnail as background
                    if (!postImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = BaseUrlProvider.getFullImageUrl(postImageUrl),
                            contentDescription = "Shared Post",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Dark gradient overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 100f
                                    )
                                )
                        )
                    } else {
                        // Fallback gray background if no image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 48.sp
                            )
                        }
                    }
                    
                    // Content overlay at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(if (isSmallScreen) 12.dp else if (isTablet) 18.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Post",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isSmallScreen) 10.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        if (!postCaption.isNullOrBlank()) {
                            Text(
                                text = postCaption.take(120),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = if (isSmallScreen) 13.sp else if (isTablet) 16.sp else 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                        blurRadius = 2f
                                    )
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "👆",
                                fontSize = if (isSmallScreen) 12.sp else 14.sp
                            )
                            Text(
                                text = "Tap to view full post",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = if (isSmallScreen) 11.sp else if (isTablet) 13.sp else 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }
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
fun OutgoingSharedPostMessage(
    sharedPostId: String,
    postCaption: String?,
    postImageUrl: String?,
    timestamp: String?,
    isSmallScreen: Boolean = false,
    isTablet: Boolean = false,
    onPostClick: () -> Unit
) {
    val maxWidth = if (isTablet) 0.65f else 0.75f
    val timestampSize = if (isSmallScreen) 11.sp else if (isTablet) 14.sp else 12.sp
    val cardHeight = if (isSmallScreen) 280.dp else if (isTablet) 400.dp else 320.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isSmallScreen) 2.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(maxWidth)
                    .height(cardHeight)
                    .clickable(onClick = onPostClick),
                shape = RoundedCornerShape(if (isTablet) 20.dp else 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Post Image/Video thumbnail as background
                    if (!postImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = BaseUrlProvider.getFullImageUrl(postImageUrl),
                            contentDescription = "Shared Post",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Dark gradient overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 100f
                                    )
                                )
                        )
                    } else {
                        // Fallback gray background if no image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 48.sp
                            )
                        }
                    }
                    
                    // Content overlay at bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(if (isSmallScreen) 12.dp else if (isTablet) 18.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp)
                    ) {
                        // "Reel" or "Post" label
                        Surface(
                            color = Color(0xFFFFC107).copy(alpha = 0.95f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Post",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isSmallScreen) 10.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Caption
                        if (!postCaption.isNullOrBlank()) {
                            Text(
                                text = postCaption.take(120),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = if (isSmallScreen) 13.sp else if (isTablet) 16.sp else 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                        blurRadius = 2f
                                    )
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // "Tap to view" hint
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "👆",
                                fontSize = if (isSmallScreen) 12.sp else 14.sp
                            )
                            Text(
                                text = "Tap to view full post",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = if (isSmallScreen) 11.sp else if (isTablet) 13.sp else 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }
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
fun IncomingCallDialog(
    callerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        title = {
            Text(
                text = "Incoming Call",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$callerName is calling...",
                    fontSize = 16.sp,
                    color = Color(0xFF6B7280)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Accept",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accept")
            }
        },
        dismissButton = {
            Button(
                onClick = onDecline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Decline",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Decline")
            }
        }
    )
}

@Composable
fun ActiveCallScreen(
    callerName: String,
    isVideoCall: Boolean,
    isMicMuted: Boolean,
    isVideoMuted: Boolean,
    isSpeakerOn: Boolean,
    onToggleMic: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    onAttachLocalVideo: (org.webrtc.SurfaceViewRenderer) -> Unit,

    onAttachRemoteVideo: (org.webrtc.SurfaceViewRenderer) -> Unit,
    remoteVideoTrackStream: org.webrtc.MediaStream? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2937))
    ) {
        // Remote video (full screen)
        if (isVideoCall) {
            AndroidView(
                factory = { context ->
                    org.webrtc.SurfaceViewRenderer(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        onAttachRemoteVideo(this)
                    }
                },
                update = { renderer ->
                    // Re-attach when stream changes or view updates
                    if (remoteVideoTrackStream != null) {
                        onAttachRemoteVideo(renderer)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Local video (picture-in-picture)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp) // Add top padding to avoid status bar/cutout
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        org.webrtc.SurfaceViewRenderer(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setZOrderMediaOverlay(true) // Crucial: Draw on top of surface view
                            onAttachLocalVideo(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Voice call UI - show caller name
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF374151)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callerName.take(1).uppercase(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = callerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "In call...",
                    fontSize = 16.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        // Call controls at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mic toggle
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isMicMuted) Color(0xFFEF4444) else Color(0xFF374151),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isMicMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Toggle Mic",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Video toggle (only for video calls)
                if (isVideoCall) {
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isVideoMuted) Color(0xFFEF4444) else Color(0xFF374151),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isVideoMuted) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                            contentDescription = "Toggle Video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Camera switch
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF374151), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Speaker toggle (voice calls only)
                if (!isVideoCall) {
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isSpeakerOn) Color(0xFFFFC107) else Color(0xFF374151),
                                CircleShape
                            )
                    ) {
                        Text(
                            text = "🔊",
                            fontSize = 24.sp
                        )
                    }
                }

                // End call
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
