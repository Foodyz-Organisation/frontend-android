package com.example.damprojectfinal.user.feature_chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.R
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.user.feature_chat.viewmodel.ChatViewModel

// Data class to represent a message
data class Message(
    val id: Int,
    val text: String?,
    val isOutgoing: Boolean,
    val timestamp: String? = "",
    val hasBadWords: Boolean = false,
    val isSpam: Boolean = false,
    val spamConfidence: Double = 0.0,
    val wasModerated: Boolean = false
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

    LaunchedEffect(conversationId, accessToken) {
        if (conversationId != null && accessToken != null) {
            vm.loadMessages(accessToken!!, conversationId)
            vm.startMessagesAutoRefresh(accessToken!!, conversationId)
            
            // Initialize Socket for WebRTC and Real-time messages
            vm.initSocket(
                baseUrl = "http://10.0.2.2:3000", 
                authToken = accessToken!!,
                conversationId = conversationId
            )
            
            // Initialize WebRTC context
            vm.initWebRtc(context)
        }
    }
    DisposableEffect(conversationId) {
        onDispose { 
            vm.stopMessagesAutoRefresh()
            vm.disconnectSocket()
        }
    }

    val messages: List<Message> = remember(httpMessages, currentUserId) {
        httpMessages.mapIndexed { index, dto ->
            val hasBadWords = dto.hasBadWords ?: false
            val isSpam = dto.isSpam ?: false
            val spamConfidence = dto.spamConfidence ?: 0.0
            // Utiliser moderatedContent si disponible et différent de content
            val displayText = if (hasBadWords && !dto.moderatedContent.isNullOrEmpty()) {
                dto.moderatedContent
            } else {
                dto.content
            }
            
            Message(
                id = index,
                text = displayText,
                isOutgoing = currentUserId != null && dto.senderId == currentUserId,
                timestamp = dto.createdAt,
                hasBadWords = hasBadWords,
                isSpam = isSpam,
                spamConfidence = spamConfidence,
                wasModerated = hasBadWords && !dto.moderatedContent.isNullOrEmpty()
            )
        }
    }
    
    // State for auto-scrolling
    val listState = rememberLazyListState()
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatDetailAppBar(
                chatName = chatName,
                onBack = { navController.popBackStack() },
                onCall = {
                    if (conversationId != null) {
                        vm.startCall(conversationId, isVideo = false)
                    }
                },
                onVideoCall = {
                    if (conversationId != null) {
                        vm.startCall(conversationId, isVideo = true)
                    }
                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
        ) {

            // Message d'erreur réseau
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontSize = 12.sp
                )
            }

            // HEADER MODERNE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFf5c42e),
                                Color(0xFFffd966)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                                )
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            chatName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            if (isConnected || messages.isNotEmpty()) "● En ligne" else "Hors ligne",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Liste des messages
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isLoading) {
                    Text(
                        text = "No messages yet. Say hello!",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(messages) { message ->
                            if (message.isOutgoing) {
                                OutgoingMessage(message)
                            } else {
                                IncomingMessage(message)
                            }
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
    
    // Incoming Call Dialog
    val incomingCall by vm.incomingCall.collectAsState()
    if (incomingCall) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("Incoming Call") },
            text = { Text("$chatName is calling you...") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { vm.acceptCall(isVideo = false) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Accept Audio")
                }
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Button(
                    onClick = { vm.acceptCall(isVideo = true) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Accept Video")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(
                    onClick = { vm.declineCall() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Decline")
                }
            }
        )
    }
    
    // Active Call UI Overlay
    val isInCall by vm.isInCall.collectAsState()
    val isVideoCall by vm.isVideoCall.collectAsState()
    val isMicMuted by vm.isMicMuted.collectAsState()
    val isSpeakerOn by vm.isSpeakerOn.collectAsState()
    // context already defined above
    
    if (isInCall) {
        if (isVideoCall) {
             VideoCallScreen(vm = vm, chatName = chatName)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "In Audio Call with $chatName",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Button
                        IconButton(
                            onClick = { vm.toggleMic() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isMicMuted) Color.White else Color.DarkGray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMicMuted) Color.Black else Color.White
                            )
                        }
                        
                        // End Call Button
                        IconButton(
                            onClick = { vm.endCall() },
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Speaker Button
                        IconButton(
                            onClick = { vm.toggleSpeaker(context) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (isSpeakerOn) Color.White else Color.DarkGray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCallScreen(vm: ChatViewModel, chatName: String) {
    val isMicMuted by vm.isMicMuted.collectAsState()
    val isVideoMuted by vm.isVideoMuted.collectAsState()
    val isSpeakerOn by vm.isSpeakerOn.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Remote Video (Full Screen)
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                org.webrtc.SurfaceViewRenderer(ctx).apply {
                    init(org.webrtc.EglBase.create().eglBaseContext, null)
                    setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    setEnableHardwareScaler(true)
                    vm.attachRemoteVideo(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Local Video (PiP)
        if (!isVideoMuted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(100.dp, 150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        org.webrtc.SurfaceViewRenderer(ctx).apply {
                            init(org.webrtc.EglBase.create().eglBaseContext, null)
                            setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setEnableHardwareScaler(true)
                            setZOrderMediaOverlay(true)
                            vm.attachLocalVideo(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Mic
            IconButton(
                onClick = { vm.toggleMic() },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isMicMuted) Color.White else Color.DarkGray.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (isMicMuted) Color.Black else Color.White
                )
            }
            
            // Toggle Video
            IconButton(
                onClick = { vm.toggleVideo() },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isVideoMuted) Color.White else Color.DarkGray.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isVideoMuted) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = "Video",
                    tint = if (isVideoMuted) Color.Black else Color.White
                )
            }
            
            // End Call
            IconButton(
                onClick = { vm.endCall() },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "End Call",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailAppBar(chatName: String, onBack: () -> Unit, onCall: () -> Unit, onVideoCall: () -> Unit) {
    val titleText = chatName.ifBlank { "Conversation" }
    val textColor = Color(0xFF1F2A37)

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        },
        actions = {
            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Audio Call",
                    tint = textColor
                )
            }
            IconButton(onClick = onVideoCall) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = "Video Call",
                    tint = textColor
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = textColor,
            navigationIconContentColor = textColor
        )
    )
}

@Composable
fun IncomingMessage(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier.align(Alignment.Start)
        ) {
            if (message.text != null) {
                Column {
                    Text(
                        text = message.text,
                        color = Color(0xFF1F2937),
                        fontSize = 15.sp,
                        modifier = Modifier
                            .background(
                                Color.White,
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    
                    // Indicateurs de modération
                    Row(
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge message modéré (bad words)
                        if (message.wasModerated) {
                            Text(
                                text = "🛑 Message modéré",
                                fontSize = 11.sp,
                                color = Color(0xFFf5c42e),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Badge spam
                        if (message.isSpam) {
                            val spamPercentage = (message.spamConfidence * 100).toInt()
                            Text(
                                text = "⚠️ Spam ($spamPercentage%)",
                                fontSize = 11.sp,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutgoingMessage(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (message.text != null) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFf5c42e),
                                    Color(0xFFffd966)
                                )
                            ),
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 4.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                
                // Indicateurs de modération
                Row(
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    // Badge message modéré (bad words)
                    if (message.wasModerated) {
                        Text(
                            text = "🛑 Contenu modéré",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Badge spam
                    if (message.isSpam) {
                        val spamPercentage = (message.spamConfidence * 100).toInt()
                        Text(
                            text = "⚠️ Spam ($spamPercentage%)",
                            fontSize = 11.sp,
                            color = Color(0xFF374151),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { 
                Text(
                    "Écrire un message...",
                    color = Color.Gray.copy(alpha = 0.6f)
                ) 
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(26.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFf5c42e),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB)
            )
        )

        Spacer(modifier = Modifier.width(10.dp))

        FilledIconButton(
            onClick = onSend,
            modifier = Modifier.size(52.dp),
            enabled = !isSending,
            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFf5c42e),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE5E7EB)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Envoyer",
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatDetailScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        ChatDetailScreen(
            chatName = "Delivery Driver - Ahmed",
            navController = navController,
            conversationId = "preview-conversation-id",
            currentUserId = "preview-user-id",
            vm = viewModel()
        )
    }
}




