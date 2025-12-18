package com.example.damprojectfinal.user.feature_chat.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.PeerDto
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.model.ChatListItem
import com.example.damprojectfinal.user.feature_chat.ui.components.ChatItemNew
import com.example.damprojectfinal.user.feature_chat.viewmodel.ChatViewModel
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.professional.common._component.CustomProTopBarWithIcons
import com.example.damprojectfinal.professional.common._component.ProfessionalBottomNavigationBar
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.UserRepository

private const val TAG = "ChatManagementScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatManagementScreen(
    navController: NavController
) {
    // User-side chat management (uses user TopAppBar and navigation)
    ChatManagementInternal(
        navController = navController,
        isPro = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProChatManagementScreen(
    navController: NavController
) {
    // Professional-side chat management (uses pro TopBar and navigation)
    ChatManagementInternal(
        navController = navController,
        isPro = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatManagementInternal(
    navController: NavController,
    isPro: Boolean
) {
    Log.d(TAG, "ChatManagementScreen composing...")

    val vm: ChatViewModel = viewModel()
    Log.d(TAG, "ViewModel created successfully")

    var searchQuery by remember { mutableStateOf("") }
    var isPeerSheetVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val currentUserId = remember { tokenManager.getUserId() }
    val accessToken by tokenManager.getAccessTokenFlow().collectAsState(initial = null)
    
    // State for profile picture URL
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    
    // Fetch user profile picture
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrEmpty()) {
            try {
                val token = tokenManager.getAccessTokenAsync()
                if (!token.isNullOrEmpty()) {
                    val userApiService = UserApiService(tokenManager)
                    val userRepository = UserRepository(userApiService)
                    val user = userRepository.getUserById(currentUserId, token)
                    profilePictureUrl = user.profilePictureUrl
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching profile picture: ${e.message}")
            }
        }
    }
    
    // Snackbar for success messages
    val snackbarHostState = remember { SnackbarHostState() }

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
    val deleteSuccess by vm.deleteSuccess.collectAsState(initial = false)

    // Filter conversations for display
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

    // Filter peers for user/professional search
    val filteredPeers = remember(searchQuery, peers) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            peers.filter { peer ->
                peer.name.contains(searchQuery, ignoreCase = true) ||
                        peer.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Current route (for highlighting bottom nav items)
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    // Show success snackbar when conversation is deleted
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            snackbarHostState.showSnackbar(
                message = "Your conversation has been deleted",
                duration = SnackbarDuration.Short
            )
            // Reset the success state
            vm.resetDeleteSuccess()
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
                        actionColor = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        },
        topBar = {
            if (!isPro) {
                // 🔹 User-side TopAppBar
                TopAppBar(
                    navController = navController,
                    currentRoute = "chatList",
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
                    currentUserId = currentUserId ?: "unknown",
                    onLogoutClick = { /* TODO: Implement logout */ },
                    profilePictureUrl = profilePictureUrl
                )
            } else {
                // 🔹 Pro-side TopBar using Foodyz Pro design
                val proId = currentUserId ?: "unknown"
                val hostController = navController as? NavHostController

                if (hostController != null) {
                    CustomProTopBarWithIcons(
                        professionalId = proId,
                        navController = hostController,
                        onLogout = {
                            // TODO: Wire this to pro logout flow if needed
                        },
                        onMenuClick = {
                            hostController.navigate("professional_menu/$proId")
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (isPro) {
                val hostController = navController as? NavHostController
                val proId = currentUserId ?: "unknown"
                if (hostController != null) {
                    ProfessionalBottomNavigationBar(
                        navController = hostController,
                        currentRoute = currentRoute,
                        professionalId = proId
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchField(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                // Show search results for users/professionals when typing
                if (searchQuery.isNotBlank() && filteredPeers.isNotEmpty()) {
                    SearchResultsDropdown(
                        peers = filteredPeers,
                        onPeerSelected = { peer ->
                            searchQuery = "" // Clear search
                            accessToken?.let { token ->
                                vm.startConversationWithPeer(
                                    peer = peer,
                                    authToken = token,
                                    currentUserId = currentUserId
                                ) { conversation ->
                                    if (conversation != null && !conversation.id.isNullOrBlank()) {
                                        val title = vm.displayTitleFor(conversation, currentUserId)
                                        val resolvedUser = currentUserId ?: "unknown"
                                        navController.navigate("chatDetail/${conversation.id}/$title/$resolvedUser")
                                    }
                                }
                            }
                        }
                    )
                }

                // Show conversations when not searching or when search has no peer results
                when {
                    isLoading -> LoadingState()
                    error != null -> StatusCard(
                        text = "Error: $error",
                        tint = Color.Red,
                        icon = "⚠️"
                    )
                    filteredChats.isEmpty() && (searchQuery.isBlank() || filteredPeers.isEmpty()) -> StatusCard(
                        text = if (searchQuery.isNotBlank() && filteredPeers.isEmpty())
                            "No users or conversations found"
                        else
                            "No conversations yet.",
                        tint = Color(0xFFF59E0B),
                        icon = "\uD83D\uDCAC"
                    )
                    filteredChats.isNotEmpty() -> ConversationList(
                        chats = filteredChats,
                        currentUserId = currentUserId ?: "unknown",
                        navController = navController,
                        onDeleteConversation = { conversationId ->
                            accessToken?.let { token ->
                                vm.deleteConversation(token, conversationId, currentUserId)
                            }
                        }
                    )
                }
            }

            if (isPeerSheetVisible) {
                PeerBottomSheet(
                    peers = peers,
                    onDismiss = { isPeerSheetVisible = false },
                    isStarting = isStarting,
                    errorMessage = startConversationError,
                    onPeerSelected = { peer: PeerDto ->
                        accessToken?.let { token ->
                            vm.startConversationWithPeer(
                                peer = peer,
                                authToken = token,
                                currentUserId = currentUserId
                            ) { conversation ->
                                if (conversation != null && !conversation.id.isNullOrBlank()) {
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
        placeholder = { Text("Search users or conversations...") },
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
private fun SearchResultsDropdown(
    peers: List<PeerDto>,
    onPeerSelected: (PeerDto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Start a conversation with:",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(peers) { peer ->
                PeerSearchResultItem(
                    peer = peer,
                    onClick = { onPeerSelected(peer) }
                )
            }
        }
    }
}

@Composable
private fun PeerSearchResultItem(
    peer: PeerDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = peer.name.take(2).uppercase(),
                color = Color(0xFFB87300),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Name and email
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peer.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF1F2A37)
            )
            Text(
                text = peer.email,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        // Chat icon
        Icon(
            imageVector = Icons.Filled.Chat,
            contentDescription = "Start chat",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp)
        )
    }
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
    navController: NavController,
    onDeleteConversation: (String) -> Unit
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
                },
                onDelete = {
                    onDeleteConversation(chat.id)
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
