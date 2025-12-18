package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CommentResponse
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@Composable
fun CommentBottomSheetContent(
    postId: String,
    onDismiss: () -> Unit,
    postsViewModel: PostsViewModel = viewModel()
) {
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingComments by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Animation state - starts false, becomes true to trigger slide-up animation
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation when screen appears
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Fetch post and comments
    LaunchedEffect(postId) {
        isLoading = true
        errorMessage = null
        try {
            post = RetrofitClient.postsApiService.getPostById(postId)
            comments = RetrofitClient.postsApiService.getComments(postId)
        } catch (e: Exception) {
            errorMessage = "Failed to load: ${e.localizedMessage ?: e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Header with title and close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comments",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }

        Divider()

        // Comments list
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Post preview at top
                post?.let { currentPost ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = BaseUrlProvider.getFullImageUrl(currentPost.mediaUrls.firstOrNull()),
                                    contentDescription = "Post",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentPost.caption,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(
                                            text = "${currentPost.likeCount} likes",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "${currentPost.commentCount} comments",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Comments list
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet. Be the first to comment!",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(comments) { comment ->
                        TikTokStyleCommentItem(
                            comment = comment,
                            onLikeClick = { /* TODO: Implement like */ },
                            onReplyClick = { /* TODO: Implement reply */ }
                        )
                    }
                }
            }
        }

        Divider()

        // Comment input at bottom
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            postsViewModel.createComment(postId, newCommentText)
                                            newCommentText = ""
                                            // Refresh comments
                                            comments = RetrofitClient.postsApiService.getComments(postId)
                                            snackbarHostState.showSnackbar(
                                                message = "Comment added",
                                                duration = SnackbarDuration.Short
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "Failed to add comment: ${e.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = newCommentText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = if (newCommentText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                )
            }
        }
    }

    // Snackbar host
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
fun CommentScreen(
    navController: NavController,
    postId: String,
    postsViewModel: PostsViewModel = viewModel()
) {
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingComments by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Animation state - starts false, becomes true to trigger slide-up animation
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation when screen appears
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Fetch post and comments
    LaunchedEffect(postId) {
        isLoading = true
        errorMessage = null
        try {
            post = RetrofitClient.postsApiService.getPostById(postId)
            comments = RetrofitClient.postsApiService.getComments(postId)
        } catch (e: Exception) {
            errorMessage = "Failed to load: ${e.localizedMessage ?: e.message}"
        } finally {
            isLoading = false
        }
    }

    // Bottom sheet wrapper that slides up from bottom
    Box(modifier = Modifier.fillMaxSize()) {
        // Overlay background with fade animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = { 
                        isVisible = false
                        scope.launch {
                            kotlinx.coroutines.delay(300) // Wait for animation
                            navController.popBackStack() 
                        }
                    })
            )
        }
        
        // Bottom sheet content that slides up smoothly
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = tween(400, easing = EaseOutCubic),
                initialOffsetY = { fullHeight -> fullHeight }
            ) + fadeIn(animationSpec = tween(400)),
            exit = slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { fullHeight -> fullHeight }
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with comment count and close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${comments.size} comments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        IconButton(
                            onClick = { 
                                isVisible = false
                                scope.launch {
                                    kotlinx.coroutines.delay(300) // Wait for animation
                                    navController.popBackStack() 
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close, 
                                contentDescription = "Close",
                                tint = Color(0xFF1F2937)
                            )
                        }
                    }

                    // Comments list
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Comments list
                            if (comments.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No comments yet. Be the first to comment!",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                items(comments) { comment ->
                                    TikTokStyleCommentItem(
                                        comment = comment,
                                        onLikeClick = { /* TODO: Implement like */ },
                                        onReplyClick = { /* TODO: Implement reply */ }
                                    )
                                }
                            }
                        }
                    }

                    // Comment input at bottom - TikTok style
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Profile picture
                            AsyncImage(
                                model = null, // TODO: Get current user profile picture
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            // Input field
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Add comment...", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color(0xFFF3F4F6),
                                    unfocusedContainerColor = Color(0xFFF3F4F6)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                trailingIcon = {
                                    if (newCommentText.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        postsViewModel.createComment(postId, newCommentText)
                                                        newCommentText = ""
                                                        comments = RetrofitClient.postsApiService.getComments(postId)
                                                        snackbarHostState.showSnackbar(
                                                            message = "Comment added",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    } catch (e: Exception) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Failed to add comment: ${e.message}",
                                                            duration = SnackbarDuration.Long
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Send,
                                                contentDescription = "Send",
                                                tint = Color(0xFF3B82F6),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            )
                            
                            // Action icons
                            IconButton(
                                onClick = { /* TODO: Tag user */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("@", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            }
                            
                            IconButton(
                                onClick = { /* TODO: Send gift */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CardGiftcard,
                                    contentDescription = "Gift",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { /* TODO: Emoji picker */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("😊", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Snackbar host
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
    }
}

// Helper function to format timestamp
fun formatTimeAgo(createdAt: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(createdAt)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        when {
            seconds < 60 -> "${seconds}s ago"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val outputFormat = SimpleDateFormat("MMM d", Locale.US)
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Just now"
    }
}

@Composable
fun TikTokStyleCommentItem(
    comment: CommentResponse,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(0) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Profile picture on left
        AsyncImage(
            model = BaseUrlProvider.getFullImageUrl(comment.authorAvatar),
            contentDescription = "Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Comment content (middle)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Username row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = comment.authorUsername ?: comment.authorName ?: "Anonymous",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937)
                )
                // Optional: Add verified badge or creator badge here
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Comment text
            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = Color(0xFF1F2937),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Timestamp and Reply
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Text(
                    text = "Reply",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.clickable { onReplyClick() }
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Like button on right
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    likeCount = if (isLiked) likeCount + 1 else maxOf(0, likeCount - 1)
                    onLikeClick()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(22.dp)
                )
            }
            if (likeCount > 0) {
                Text(
                    text = formatCount(likeCount),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

// Helper to format large numbers (e.g., 52200 -> "52.2K")
fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}









