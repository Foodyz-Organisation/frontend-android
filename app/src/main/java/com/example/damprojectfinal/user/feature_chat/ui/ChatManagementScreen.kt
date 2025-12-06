package com.example.damprojectfinal.user.feature_chat.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.PeerDto
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.model.ChatListItem
import com.example.damprojectfinal.user.feature_chat.ui.components.ChatItemNew
import com.example.damprojectfinal.user.feature_chat.viewmodel.ChatViewModel

private const val TAG = "ChatManagementScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatManagementScreen(
    navController: NavController
    ) {
    Log.d(TAG, "ChatManagementScreen composing...")

    val vm: ChatViewModel = viewModel()
    Log.d(TAG, "ViewModel created successfully")

    var searchQuery by remember { mutableStateOf("") }
    var isPeerSheetVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId = remember { tokenManager.getUserId() }
    val accessToken by tokenManager.getAccessToken().collectAsState(initial = null)

    LaunchedEffect(accessToken) {
        accessToken?.let { token ->
            vm.loadPeers(token, currentUserId = currentUserId)
            vm.loadConversations(token, currentUserId)
            vm.startAutoRefresh(token, currentUserId)
        }
    }
    DisposableEffect(Unit) {
        onDispose { vm.stopAutoRefresh() }
    }

    val chatList by vm.chatItems.collectAsState(initial = emptyList())
    val peers by vm.peers.collectAsState(initial = emptyList())
    val isLoading by vm.isLoading.collectAsState(initial = false)
    val error by vm.errorMessage.collectAsState(initial = null)
    val isStarting by vm.isStartingConversation.collectAsState(initial = false)
    val startConversationError by vm.startConversationError.collectAsState(initial = null)

    val filteredChats = remember(searchQuery, chatList) {
        if (searchQuery.isBlank()) {
            chatList
        } else {
            chatList.filter { chat ->
                chat.title.contains(searchQuery, ignoreCase = true) ||
                        chat.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onNewChat = { isPeerSheetVisible = true })

            SearchField(searchQuery = searchQuery, onQueryChange = { searchQuery = it })

            when {
                isLoading -> LoadingState()
                error != null -> StatusCard(
                    text = "Error: $error",
                    tint = Color.Red,
                    icon = "⚠️"
                )
                filteredChats.isEmpty() -> StatusCard(
                    text = if (searchQuery.isNotBlank())
                        "No conversations found"
                    else
                        "No conversations yet. Start one from the + button.",
                    tint = Color(0xFFF59E0B),
                    icon = "\uD83D\uDCAC"
                )
                else -> ConversationList(
                    chats = filteredChats,
                    currentUserId = currentUserId ?: "unknown",
                    navController = navController
                )
            }

            NewChatFab { isPeerSheetVisible = true }
        }

        if (isPeerSheetVisible) {
            PeerBottomSheet(
                peers = peers,
                onDismiss = { isPeerSheetVisible = false },
                isStarting = isStarting,
                errorMessage = startConversationError,
                onPeerSelected = { peer ->
                    accessToken?.let { token ->
                        vm.startConversationWithPeer(
                            peer = peer,
                            authToken = token,
                            currentUserId = currentUserId
                        ) { conversation ->
                            if (conversation != null) {
                                val title = vm.displayTitleFor(conversation, currentUserId)
                                val resolvedUser = currentUserId ?: "unknown"
                                navController.navigate("chatDetail/${conversation.id}/$title/$resolvedUser")
                            }
                        }
                    }
                }
            )
        }

        if (isStarting) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                StatusCard(
                    text = "Starting chat…",
                    tint = Color(0xFFF59E0B),
                    icon = "\u23F3"
                )
            }
        }
    }
}

@Composable
private fun TopBar(onNewChat: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Foodyz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2A37)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New chat",
                        tint = Color(0xFF1F2A37),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { /* TODO: Open search modal */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Global search",
                        tint = Color(0xFF1F2A37),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { /* TODO: Open notifications */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1F2A37),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFFB87300)
            )
        },
        placeholder = { Text("Search conversations...") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFB87300).copy(alpha = 0.2f),
            focusedBorderColor = Color(0xFFF59E0B),
            focusedTextColor = Color(0xFFB87300),
            cursorColor = Color(0xFFB87300),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ConversationList(
    chats: List<ChatListItem>,
    currentUserId: String,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chats) { chat ->
            ChatItemNew(
                chat = chat,
                onClick = {
                    navController.navigate(
                        "chatDetail/${chat.id}/${chat.title}/$currentUserId"
                    )
                }
            )
        }
    }
}

@Composable
private fun NewChatFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF59E0B), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start new chat",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeerBottomSheet(
    peers: List<PeerDto>,
    onDismiss: () -> Unit,
    isStarting: Boolean,
    errorMessage: String?,
    onPeerSelected: (PeerDto) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Start a chat",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (peers.isEmpty()) {
                Text(
                    text = "You don't have anyone to chat with yet. Invite someone or refresh.",
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                peers.forEach { peer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(peer.name, fontWeight = FontWeight.SemiBold)
                            Text(peer.email, color = Color.Gray, fontSize = 13.sp)
                        }
                        Button(
                            onClick = { if (!isStarting) onPeerSelected(peer) },
                            enabled = !isStarting
                        ) {
                            Text("Chat")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(text: String, tint: Color, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatManagementScreenPreview() {
    val navController = rememberNavController()
    ChatManagementScreen(navController = navController)
}
