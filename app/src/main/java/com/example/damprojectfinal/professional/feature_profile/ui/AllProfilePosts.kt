package com.example.damprojectfinal.professional.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.damprojectfinal.UserRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProfilePosts(
    navController: NavHostController,
    professionalId: String,
    postsViewModel: PostsViewModel = viewModel()
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dialog State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var postToModifyId by remember { mutableStateOf<String?>(null) }

    // Fetch posts when screen loads
    LaunchedEffect(professionalId) {
        isLoading = true
        errorMessage = null
        try {
            posts = RetrofitClient.postsApiService.getPostsByOwnerId(professionalId)
        } catch (e: Exception) {
            errorMessage = "Failed to load posts: ${e.localizedMessage ?: e.message}"
        } finally {
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "All Posts",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No posts available.", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onLikeClick = { postId ->
                            // Use isLiked if available, otherwise fallback to likeCount > 0 for backward compatibility
                            val currentlyLiked = post.isLiked ?: (post.likeCount > 0)
                            if (currentlyLiked) {
                                postsViewModel.decrementLikeCount(postId)
                            } else {
                                postsViewModel.incrementLikeCount(postId)
                            }
                        },
                        onCommentClick = { postId ->
                            navController.navigate("${UserRoutes.POST_DETAILS_SCREEN}/$postId")
                        },
                        onEditRequest = { postId ->
                            postToModifyId = postId
                            showEditDialog = true
                        },
                        onDeleteRequest = { postId ->
                            postToModifyId = postId
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                 Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFEF4444))
            },
            title = {
                Text(
                    text = "Delete Post",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this post?",
                    color = Color(0xFF4B5563),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        postToModifyId?.let { postId ->
                            postsViewModel.deletePost(postId)
                            posts = posts.filter { it._id != postId }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // App Yellow
                        contentColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Confirmation Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            icon = {
                 Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color(0xFFFFC107))
            },
            title = {
                Text(
                    text = "Edit Post",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Text(
                    "Do you want to edit this post?",
                    color = Color(0xFF4B5563),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        postToModifyId?.let { postId ->
                            val post = posts.find { it._id == postId }
                            val encodedCaption = URLEncoder.encode(
                                post?.caption ?: "",
                                StandardCharsets.UTF_8.toString()
                            )
                            navController.navigate(
                                UserRoutes.EDIT_POST_SCREEN
                                    .replace("{postId}", postId)
                                    .replace("{initialCaption}", encodedCaption)
                            )
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // App Yellow
                        contentColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Yes, Edit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PostItem(
    post: PostResponse,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onEditRequest: (String) -> Unit,
    onDeleteRequest: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), 
        shape = androidx.compose.ui.graphics.RectangleShape 
    ) {
        Column {
            // Header: Date + Three Dots Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Post Date
                Text(
                    text = post.createdAt.take(10), 
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )
                
                // Three Dots Menu
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier
                            .clickable { showMenu = true }
                            .padding(4.dp)
                    )
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                showMenu = false
                                onEditRequest(post._id)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color(0xFF1F2937)) // Dark Icon
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                            onClick = { 
                                showMenu = false
                                onDeleteRequest(post._id)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        )
                    }
                }
            }

            // Post Media
            // Decide whether to show VideoPlayer or Image
            if (post.mediaType == "reel" || (post.mediaUrls.firstOrNull()?.endsWith(".mp4") == true)) {
                // Video Player
                val videoUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull()) ?: ""
                FeedVideoPlayer(
                    videoUrl = videoUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Keep square for feed consistency
                        .background(Color.Black)
                )
            } else {
                // Static Image
                val rawUrl = post.mediaUrls.firstOrNull()
                val imageUrlToLoad = BaseUrlProvider.getFullImageUrl(rawUrl)

                AsyncImage(
                    model = imageUrlToLoad,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) 
                        .background(Color(0xFFE5E7EB))
                )
            }

            // Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Actions: Like & Comment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like
                    val isLiked = post.isLiked ?: false
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF1F2937),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onLikeClick(post._id) }
                    )
                    
                    // Comment
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline, 
                        contentDescription = "Comment",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onCommentClick(post._id) }
                    )
                }

            }
            
            // Likes Count
            if (post.likeCount > 0) {
                Text(
                    text = "${post.likeCount} likes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Caption
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    color = Color(0xFF1F2937),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    lineHeight = 20.sp
                )
            }
            
            // View Comments Link
            if (post.commentCount > 0) {
                 Text(
                    text = "View all ${post.commentCount} comments",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onCommentClick(post._id) }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun FeedVideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            volume = 0f // Start muted
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop
            playWhenReady = true // Auto-play
        }
    }
    
    // Manage Lifecycle
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    // Render PlayerView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Hide controls for feed auto-play
            }
        },
        modifier = modifier
    )
}

