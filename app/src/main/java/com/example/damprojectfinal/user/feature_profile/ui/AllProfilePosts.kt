package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
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
    navController: NavController,
    userId: String,
    postsViewModel: PostsViewModel = viewModel()
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dialog State
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var postToModifyId by remember { mutableStateOf<String?>(null) }
    var postMsgToEdit by remember { mutableStateOf<String?>(null) } // Store caption? No just ID is enough for now.

    // Fetch posts when screen loads
    LaunchedEffect(userId) {
        isLoading = true
        errorMessage = null
        try {
            posts = RetrofitClient.postsApiService.getPostsByOwnerId(userId)
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
                        "My Posts", 
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
        containerColor = Color(0xFFF9FAFB) // Light gray background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No posts yet", 
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Media (Image or Video)
                 if (post.mediaType == "reel" || (post.mediaUrls.firstOrNull()?.endsWith(".mp4") == true)) {
                    val videoUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull()) ?: ""
                    FeedVideoPlayer(
                        videoUrl = videoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f) // Slightly taller than wide for food
                            .background(Color.Black)
                    )
                } else {
                    val rawUrl = post.mediaUrls.firstOrNull()
                    val imageUrlToLoad = BaseUrlProvider.getFullImageUrl(rawUrl)

                    AsyncImage(
                        model = imageUrlToLoad,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .background(Color(0xFFE5E7EB))
                    )
                }

                // --- Overlays ---

                // Price Badge (Top Right)
                if (post.price != null && post.price > 0) {
                    Surface(
                        color = Color(0xFFFFC107), // Yellow
                        contentColor = Color(0xFF1F2937),
                        shape = RoundedCornerShape(bottomStart = 12.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "${post.price} TND",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Prep Time Badge (Bottom Right)
                if (post.preparationTime != null && post.preparationTime > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(topStart = 12.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${post.preparationTime} min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Food Type Badge (Bottom Left)
                 if (!post.foodType.isNullOrBlank()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        contentColor = Color(0xFF1F2937),
                        shape = RoundedCornerShape(topEnd = 12.dp),
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = post.foodType,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Menu Button (Top Left - Overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(50))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

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
                                Icon(Icons.Outlined.Edit, contentDescription = null)
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

            // Footer Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                 // Caption
                if (post.caption.isNotBlank()) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1F2937),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                     Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Like Button
                        val isLiked = post.isLiked ?: false
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(24.dp) // Slightly smaller
                                .clickable { onLikeClick(post._id) }
                        )
                        if (post.likeCount > 0) {
                            Text(
                                text = "${post.likeCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Comment Button
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { onCommentClick(post._id) }
                        )
                        if (post.commentCount > 0) {
                            Text(
                                text = "${post.commentCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }

                    // Date (Moved to footer)
                    Text(
                        text = post.createdAt.take(10), // Simplistic date formatting
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
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

