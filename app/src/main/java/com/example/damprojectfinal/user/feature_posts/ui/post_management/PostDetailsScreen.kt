package com.example.damprojectfinal.user.feature_posts.ui.post_management


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CommentResponse
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import kotlinx.coroutines.launch // For rememberCoroutineScope
import androidx.compose.foundation.Canvas // For the Canvas composable
import androidx.compose.ui.draw.alpha
import androidx.media3.common.util.UnstableApi
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    postId: String,
    postsViewModel: PostsViewModel = viewModel()
) {
    var post by remember { mutableStateOf<PostResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // States for comments section
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var isLoadingComments by remember { mutableStateOf(false) }
    var commentsError by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }
    var isSubmittingComment by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // State for fullscreen image viewer
    var showFullscreenImage by remember { mutableStateOf(false) }
    var initialImageIndex by remember { mutableStateOf(0) }


    // --- Fetch Post Details ---
    LaunchedEffect(postId) {
        isLoading = true
        errorMessage = null
        try {
            post = postsViewModel.posts.value.find { it._id == postId }
            if (post == null) {
                post = RetrofitClient.postsApiService.getPostById(postId) // For now, assume it works without viewerId in API endpoint itself.
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load post details: ${e.localizedMessage ?: e.message}"
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage!!,
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
            }
        } finally {
            isLoading = false
        }
    }

    // --- Fetch Comments for the Post ---
    val fetchComments: () -> Unit = {
        post?._id?.let { id ->
            scope.launch {
                isLoadingComments = true
                commentsError = null
                try {
                    comments = RetrofitClient.postsApiService.getComments(id)
                } catch (e: Exception) {
                    commentsError = "Failed to load comments: ${e.localizedMessage ?: e.message}"
                } finally {
                    isLoadingComments = false
                }
            }
        }
    }

    // Trigger initial comment fetch once post is loaded
    LaunchedEffect(post) {
        if (post != null) {
            fetchComments()
        }
    }


    // Professional color scheme
    val AccentYellow = Color(0xFFFFC107)
    val DarkText = Color(0xFF1F2937)
    val MediumGray = Color(0xFF6B7280)
    val LightGray = Color(0xFFF3F4F6)
    val CardBackground = Color(0xFFFFFFFF)
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Post Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground,
                    titleContentColor = DarkText
                )
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (post == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Post not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                val currentPost = post!!
                
                // 1. Enhanced Profile Header Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Enhanced Avatar with border
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(AccentYellow.copy(alpha = 0.3f), Color.Transparent)
                                        )
                                    )
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(CardBackground)
                                ) {
                                    AsyncImage(
                                        model = BaseUrlProvider.getFullImageUrl(currentPost.ownerId?.profilePictureUrl),
                                        contentDescription = "Author Avatar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentPost.ownerId?.fullName ?: "Unknown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "@${currentPost.ownerId?.username ?: "user"}",
                                    fontSize = 14.sp,
                                    color = MediumGray
                                )
                            }
                            
                            // Enhanced Rating Badge
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentYellow
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "${currentPost.postRating ?: 4.8}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Enhanced Media Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        if (currentPost.mediaType == "reel" && currentPost.mediaUrls.firstOrNull() != null) {
                            val videoUrl = BaseUrlProvider.getFullImageUrl(currentPost.mediaUrls.firstOrNull())
                            val thumbnailUrl = BaseUrlProvider.getFullImageUrl(currentPost.thumbnailUrl)
                            PostVideoPlayer(
                                videoUrl = videoUrl ?: "",
                                thumbnailUrl = thumbnailUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                            )
                        } else {
                            val isCarousel = currentPost.mediaType == "carousel" && currentPost.mediaUrls.size > 1
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .clickable {
                                        initialImageIndex = 0
                                        showFullscreenImage = true
                                    }
                            ) {
                                val imageUrl = BaseUrlProvider.getFullImageUrl(currentPost.mediaUrls.firstOrNull())
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = currentPost.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                if (isCarousel) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.7f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Outlined.Collections,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${currentPost.mediaUrls.size}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Enhanced Post Details Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Caption
                            Text(
                                text = currentPost.caption,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = DarkText,
                                lineHeight = 32.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Price and Rating Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentPost.price != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${currentPost.price}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp,
                                            color = AccentYellow
                                        )
                                        Text(
                                            text = "TND",
                                            fontSize = 16.sp,
                                            color = MediumGray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "${currentPost.postRating ?: 4.9}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkText
                                    )
                                    Text(
                                        text = "• ${currentPost.reviewsCount ?: 5} reviews",
                                        fontSize = 14.sp,
                                        color = MediumGray
                                    )
                                }
                            }
                            
                            // Preparation Time
                            currentPost.preparationTime?.let { prepTime ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LightGray
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccessTime,
                                            contentDescription = "Preparation Time",
                                            tint = AccentYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "$prepTime minutes",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Description Card (Conditional)
                currentPost.description?.takeIf { it.isNotBlank() }?.let { descriptionText ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Description",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkText
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    descriptionText,
                                    fontSize = 15.sp,
                                    color = MediumGray,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }

                // 5. Ingredients Card (Conditional)
                currentPost.ingredients?.takeIf { it.isNotEmpty() }?.let { ingredientsList ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Ingredients",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkText
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ingredientsList.forEachIndexed { index, ingredient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AccentYellow)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            ingredient,
                                            fontSize = 15.sp,
                                            color = DarkText,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Enhanced Comments Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Comment,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Comments",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = DarkText
                                    )
                                    if (comments.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = AccentYellow.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${comments.size}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentYellow
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Comments List
                            if (isLoadingComments) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentYellow)
                                }
                            } else if (commentsError != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        commentsError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp
                                    )
                                    Button(
                                        onClick = fetchComments,
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            } else if (comments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Comment,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MediumGray.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            "No comments yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MediumGray
                                        )
                                        Text(
                                            "Be the first to comment!",
                                            fontSize = 14.sp,
                                            color = MediumGray.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                comments.forEach { comment ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EnhancedCommentItem(comment = comment)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))

                            // Enhanced Comment Input
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = LightGray
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newCommentText,
                                        onValueChange = { newCommentText = it },
                                        placeholder = { Text("Add a comment...", color = MediumGray) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentYellow,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = CardBackground,
                                            unfocusedContainerColor = CardBackground
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (newCommentText.isBlank() || isSubmittingComment) return@IconButton
                                            scope.launch {
                                                isSubmittingComment = true
                                                try {
                                                    val createdComment = postsViewModel.createCommentImmediate(
                                                        currentPost._id,
                                                        newCommentText.trim()
                                                    )
                                                    val enriched = createdComment.copy(
                                                        authorName = createdComment.authorName ?: "You"
                                                    )
                                                    comments = listOf(enriched) + comments
                                                    newCommentText = ""
                                                    fetchComments()
                                                    postsViewModel.fetchPosts()
                                                } catch (e: Exception) {
                                                    commentsError = "Failed to add comment: ${e.localizedMessage ?: e.message}"
                                                } finally {
                                                    isSubmittingComment = false
                                                }
                                            }
                                        },
                                        enabled = newCommentText.isNotBlank() && !isSubmittingComment,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                if (newCommentText.isNotBlank()) AccentYellow else MediumGray.copy(alpha = 0.3f),
                                                CircleShape
                                            )
                                    ) {
                                        if (isSubmittingComment) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Send,
                                                contentDescription = "Send",
                                                tint = if (newCommentText.isNotBlank()) Color.White else MediumGray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Fullscreen Image Viewer Dialog
        if (showFullscreenImage) {
            val currentPostForViewer = post // Local variable to avoid smart cast issue
            if (currentPostForViewer != null) {
                FullscreenImageViewer(
                    imageUrls = currentPostForViewer.mediaUrls.mapNotNull { BaseUrlProvider.getFullImageUrl(it) },
                    initialIndex = initialImageIndex,
                    onDismiss = { showFullscreenImage = false }
                )
            }
        }
    }
}

// Video Player Composable for Post Details
@androidx.media3.common.util.UnstableApi
@Composable
fun PostVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullscreen by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true // Auto-play when screen loads
            volume = 1f
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }
    
    // Clean up player when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(modifier = modifier) {
        // Video player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Hide default controls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable { showFullscreen = true }
        )
    }
    
    // Fullscreen video dialog
    if (showFullscreen) {
        FullscreenVideoPlayer(
            exoPlayer = exoPlayer,
            onDismiss = { showFullscreen = false }
        )
    }
}

// Fullscreen Video Player Dialog
@androidx.media3.common.util.UnstableApi
@Composable
fun FullscreenVideoPlayer(
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true // Show controls in fullscreen
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Fullscreen Image Viewer with Carousel Support
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullscreenImageViewer(
    imageUrls: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = if (imageUrls.isNotEmpty()) initialIndex.coerceIn(0, imageUrls.size - 1) else 0,
        pageCount = { imageUrls.size }
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (imageUrls.size == 1) {
                // Single image - no pager needed
                AsyncImage(
                    model = imageUrls.first(),
                    contentDescription = "Fullscreen Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() }
                )
            } else {
                // Carousel - use HorizontalPager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = imageUrls[page],
                        contentDescription = "Carousel Image ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Page indicator for carousel
                if (imageUrls.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Navigation dots for carousel (bottom center)
            if (imageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(imageUrls.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
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

// Enhanced Comment Item
@Composable
fun EnhancedCommentItem(comment: CommentResponse) {
    val DarkText = Color(0xFF1F2937)
    val MediumGray = Color(0xFF6B7280)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = BaseUrlProvider.getFullImageUrl(comment.authorAvatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = comment.authorName ?: comment.authorUsername ?: "Anonymous",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    color = DarkText,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatTimeAgo(comment.createdAt),
                    fontSize = 12.sp,
                    color = MediumGray
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPostDetailsScreen() {
    val navController = rememberNavController()

    // ---------- Mock UserProfile (owner) ----------
    val owner = UserProfile(
        _id = "user123",
        username = "john_doe",
        fullName = "John Doe",
        email = "john@example.com",
        followerCount = 120,
        followingCount = 80,
        profilePictureUrl = null,
        bio = "ko",
        postCount = 20
    )

    // ---------- Mock Post ----------
    val mockPost = PostResponse(
        _id = "post123",
        caption = "Fresh Pasta Recipe",
        mediaUrls = listOf("https://picsum.photos/400/300"),
        mediaType = "image",
        createdAt = "2025-01-01T12:00:00Z",
        updatedAt = "2025-01-01T12:00:00Z",
        version = 1,

        ownerId = owner,
        ownerModel = "User",

        viewsCount = 200,
        thumbnailUrl = null,
        duration = null,
        aspectRatio = "1:1",

        likeCount = 24,
        commentCount = 8,
        saveCount = 5,

        description = "Homemade fresh pasta with organic ingredients.",
        ingredients = listOf("Flour", "Eggs", "Olive Oil", "Salt"),
        postRating = 4.7,
        reviewsCount = 12
    )

    // ---------- Fake ViewModel ----------
    val fakeViewModel = object : PostsViewModel() {

        // Override initial posts
        override val posts = MutableStateFlow(listOf(mockPost))

        // Prevent API call in preview
        override fun fetchPosts() { }
    }

    DamProjectFinalTheme {
        PostDetailsScreen(
            navController = navController,
            postId = "post123",
            postsViewModel = fakeViewModel
        )
    }
}
