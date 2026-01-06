package com.example.damprojectfinal.user.common._component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.ChatApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.retro.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchableUser(
    val id: String,
    val name: String,
    val profilePictureUrl: String?,
    val email: String?,
    val kind: String // "user" or "professional"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostDialog(
    postId: String,
    onDismiss: () -> Unit,
    onShareSuccess: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchableUser>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    var shareError by remember { mutableStateOf<String?>(null) }
    var shareSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    // Fetch post details
    var postData by remember { mutableStateOf<com.example.damprojectfinal.core.dto.posts.PostResponse?>(null) }
    var isLoadingPost by remember { mutableStateOf(true) }
    
    LaunchedEffect(postId) {
        try {
            val postsApi = RetrofitClient.postsApiService
            postData = postsApi.getPostById(postId)
        } catch (e: Exception) {
            android.util.Log.e("SharePostDialog", "Error loading post: ${e.message}")
            shareError = "Failed to load post details"
        } finally {
            isLoadingPost = false
        }
    }

    // Debounced search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        
        isSearching = true
        delay(500) // Debounce
        
        try {
            val token = tokenManager.getAccessTokenAsync()
            if (!token.isNullOrEmpty()) {
                val chatApi = RetrofitClient.chatApiService
                val peers = chatApi.getPeers("Bearer $token")
                
                // Filter peers by search query
                searchResults = peers
                    .filter { peer ->
                        peer.name.contains(searchQuery, ignoreCase = true) ||
                        peer.email.contains(searchQuery, ignoreCase = true)
                    }
                    .map { peer ->
                        // Clean name to remove email addresses in parentheses (e.g., "ikbel (ikbel@gmail.com)" -> "ikbel")
                        val cleanName = peer.name.split(" (").first().trim()
                        SearchableUser(
                            id = peer.id,
                            name = cleanName,
                            profilePictureUrl = peer.avatarUrl,
                            email = peer.email,
                            kind = peer.kind ?: "user"
                        )
                    }
            }
        } catch (e: Exception) {
            android.util.Log.e("SharePostDialog", "Search error: ${e.message}")
            searchResults = emptyList()
        } finally {
            isSearching = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Share Post",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Post Preview
                if (isLoadingPost) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
                    }
                } else if (postData != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Post thumbnail
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE5E7EB))
                            ) {
                                val imageUrl = if (postData!!.mediaType == "reel" && postData!!.thumbnailUrl != null) {
                                    postData!!.thumbnailUrl
                                } else {
                                    postData!!.mediaUrls.firstOrNull()
                                }
                                
                                if (!imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = BaseUrlProvider.getFullImageUrl(imageUrl),
                                        contentDescription = "Post preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            
                            // Post caption preview
                            Text(
                                text = postData!!.caption.takeIf { it.isNotEmpty() } ?: "Post",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1F2937)
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search users or kitchens...",
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF6B7280)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF6B7280)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedIndicatorColor = Color(0xFFE5E7EB),
                        focusedIndicatorColor = Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Success/Error Messages
                shareSuccessMessage?.let { message ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                shareError?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search Results
                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFFC107))
                        }
                    }
                    searchQuery.isBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Search for users or kitchens to share with",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                    }
                    searchResults.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { user ->
                                UserResultItem(
                                    user = user,
                                    isSharing = isSharing,
                                    onClick = {
                                        scope.launch {
                                            isSharing = true
                                            shareError = null
                                            shareSuccessMessage = null
                                            
                                            try {
                                                val token = tokenManager.getAccessTokenAsync()
                                                if (!token.isNullOrEmpty()) {
                                                    val postsApi = RetrofitClient.postsApiService
                                                    
                                                    // Include post info in message as workaround until backend is fixed
                                                    val messageText = if (postData != null) {
                                                        "Shared a post with you "
                                                    } else {
                                                        "Shared a post with you"
                                                    }
                                                    
                                                    val response = postsApi.sharePost(
                                                        postId = postId,
                                                        sharePostRequest = com.example.damprojectfinal.core.api.posts.PostsApiService.SharePostRequest(
                                                            recipientId = user.id,
                                                            message = messageText
                                                        )
                                                    )
                                                    
                                                    if (response.success) {
                                                        shareSuccessMessage = "Post shared with ${user.name}!"
                                                        delay(1500)
                                                        onShareSuccess()
                                                        onDismiss()
                                                    } else {
                                                        val errorMsg = when (val msg = response.message) {
                                                            is String -> msg
                                                            else -> "Failed to share post"
                                                        }
                                                        shareError = errorMsg
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("SharePostDialog", "Share error: ${e.message}")
                                                shareError = "Failed to share post: ${e.message}"
                                            } finally {
                                                isSharing = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserResultItem(
    user: SearchableUser,
    isSharing: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSharing, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9FAFB),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                ) {
                    if (!user.profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = BaseUrlProvider.getFullImageUrl(user.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                // User Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Badge for user type
                    Surface(
                        color = if (user.kind == "professional") Color(0xFFFFC107).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (user.kind == "professional") "Kitchen" else "User",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (user.kind == "professional") Color(0xFFF59E0B) else Color(0xFF3B82F6),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Send Button
            IconButton(
                onClick = onClick,
                enabled = !isSharing
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Share",
                    tint = Color(0xFFFFC107)
                )
            }
        }
    }
}

