package com.example.damprojectfinal.ui.theme.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.damprojectfinal.viewmodel.chatVm.ChatViewModel

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
    val accessToken = remember { TokenManager(context).getAccessToken() }

    val httpMessages by vm.messages.collectAsState()
    val isConnected by vm.isSocketConnected.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val isSending by vm.isSendingMessage.collectAsState(initial = false)
    val isLoading by vm.isLoading.collectAsState(initial = false)

    LaunchedEffect(conversationId, accessToken) {
        conversationId?.let {
            vm.loadMessages(accessToken, it)
            vm.startMessagesAutoRefresh(accessToken, it)
        }
    }
    androidx.compose.runtime.DisposableEffect(conversationId) {
        onDispose { vm.stopMessagesAutoRefresh() }
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

    Scaffold(
        topBar = {
            ChatDetailAppBar(
                chatName = chatName,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
                MessageInputBar(
                    text = currentMessage,
                    onTextChange = { currentMessage = it },
                    onSend = {
                        if (currentMessage.isNotBlank()) {
                            if (conversationId != null) {
                                vm.sendMessageHttp(accessToken, conversationId, currentMessage)
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
                .background(Color(0xFFF5F5F7))
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

            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF59E0B),
                                Color(0xFFF59E0B)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) Color(0xFF4CAF50) else Color.Gray
                                )
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            chatName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            if (isConnected || messages.isNotEmpty()) "Online" else "Offline",
                            color = Color(0xFFE8F5E9),
                            fontSize = 14.sp
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
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages) { message ->
                            if (message.isOutgoing) {
                                OutgoingMessage(message.text)
                            } else {
                                IncomingMessage(message.text)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailAppBar(chatName: String, onBack: () -> Unit) {
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = textColor,
            navigationIconContentColor = textColor
        )
    )
}

@Composable
fun IncomingMessage(text: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (text != null) {
            Text(
                text = text,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun OutgoingMessage(text: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (text != null) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color(0xFFF59E0B), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Type a message...") },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        Spacer(modifier = Modifier.width(8.dp))

        FilledIconButton(
            onClick = onSend,
            modifier = Modifier.size(48.dp),
            enabled = !isSending


        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
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
            navController = navController
        )
    }
}
