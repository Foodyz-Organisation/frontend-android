package com.example.damprojectfinal.user.feature_posts.ui.post_management


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.posts.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.CommentResponse
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import kotlinx.coroutines.launch // For rememberCoroutineScope
import androidx.compose.foundation.Canvas // For the Canvas composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
// import com.example.damprojectfinal.core.dto.normalUser.UserProfile // <-- REMOVED THIS IMPORT
import com.example.damprojectfinal.core.dto.posts.PostOwnerDetails // <-- NEW IMPORT: Use PostOwnerDetails

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    // --- Fetch Post Details ---
    LaunchedEffect(postId) {
        isLoading = true
        errorMessage = null
        try {
            // First, try to get from ViewModel's cache
            post = postsViewModel.posts.value.find { it._id == postId }
            // If not in cache, fetch from API. Need to pass viewerId (current user's ID) for isLiked/isSaved flags.
            // Assuming your `getPostById` (or `findOne` in backend) now expects a viewerId.
            // You'll need `TokenManager` here to get the current user's ID.
            // For now, I'll pass null or an empty string as a placeholder.
            // IMPORTANT: If your `getPostById` API call still doesn't take viewerId,
            // the `isLiked`/`isSaved` flags won't be set correctly for single post view.
            if (post == null) {
                // You would typically get the current user's ID from TokenManager here
                // val currentUserId = TokenManager.getUserId()
                // post = RetrofitClient.postsApiService.getPostById(postId, currentUserId) // If your API supports it
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


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Post Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                }
            )
        },
        bottomBar = {
            // --- Sticky Bottom Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface) // Use surface color
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Handle Discover Our Plates click */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Discover our plates", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        // Focus on comment input, or scroll to comments section
                        // For now, let's just trigger a focus logic if we had one
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111827)),
                    border = BorderStroke(1.dp, Color(0xFF111827)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Leave Comment")
                }
            }
            // --- End Sticky Bottom Buttons ---
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Make content scrollable
            ) {
                val currentPost = post!!

                // 1. Poster's Profile Info (Section 1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar/Image
                    AsyncImage(
                        // --- MODIFIED LINE ---
                        // Use actual profile image from PostOwnerDetails. Handle nullability.
                        model = currentPost.ownerId?.profilePictureUrl,
                        // --- END MODIFIED LINE ---
                        contentDescription = "Author Avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray), // Placeholder background
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        // Username (using fullName as per your latest UI decisions)
                        Text(
                            // --- MODIFIED LINE ---
                            // Use author's full name from PostOwnerDetails. Handle nullability.
                            text = currentPost.ownerId?.fullName ?: "Unknown", // Provide a default if fullName is null
                            // --- END MODIFIED LINE ---
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1F2937)
                        )
                    }
                    Spacer(Modifier.weight(1f)) // Pushes content to the right
                    // Profile Rating (if present, hardcoded for now)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFACC15))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("4.8", fontWeight = FontWeight.Bold, color = Color.White) // Hardcoded for now
                    }
                }

                // 2. Post Media (Image/Video)
                if (currentPost.mediaType == "reel" && currentPost.mediaUrls.firstOrNull() != null) {
                    // Show video player for reels
                    PostVideoPlayer(
                        videoUrl = currentPost.mediaUrls.firstOrNull()!!,
                        thumbnailUrl = currentPost.thumbnailUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 8.dp)
                    )
                } else {
                    // Show image for non-reel posts
                    val imageUrlToLoad = currentPost.mediaUrls.firstOrNull()
                    AsyncImage(
                        model = imageUrlToLoad,
                        contentDescription = currentPost.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Adjust height as needed
                            .padding(vertical = 8.dp)
                    )
                }

                // 3. Post Details (Caption, Price, Rating/Reviews)
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = currentPost.caption,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "29 TND", // Hardcoded price
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF111827)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp))
                            Text(
                                text = "${currentPost.postRating ?: "4.9"} • ${currentPost.reviewsCount ?: 5} reviews", // Placeholder rating/reviews
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 4. Description (Conditional)
                currentPost.description?.takeIf { it.isNotBlank() }?.let { descriptionText ->
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(descriptionText, fontSize = 14.sp, color = Color(0xFF374151))
                    }
                }

                // 5. Ingredients (Conditional)
                currentPost.ingredients?.takeIf { it.isNotEmpty() }?.let { ingredientsList ->
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.height(8.dp))
                        ingredientsList.forEach { ingredient ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(modifier = Modifier.size(6.dp), onDraw = {
                                    drawCircle(color = Color(0xFFFACC15))
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ingredient, fontSize = 14.sp, color = Color(0xFF374151))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // 6. Reviews / Comments Section (integrated from previous plan)
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Comments List Display
                    if (isLoadingComments) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (commentsError != null) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(commentsError!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = fetchComments) { Text("Retry") }
                        }
                    } else if (comments.isEmpty()) {
                        Text("No comments yet. Be the first to comment!", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        comments.forEach { comment ->
                            CommentItem(comment = comment)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Comment Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            label = { Text("Add a comment...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    scope.launch {
                                        postsViewModel.createComment(currentPost._id, newCommentText)
                                        newCommentText = "" // Clear input field
                                        fetchComments() // Re-fetch comments to show the new one
                                        postsViewModel.fetchPosts() // Update main feed's comment count
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send Comment")
                        }
                    }
                }
            }
        }
    }
}

// Video Player Composable for Post Details
@OptIn(UnstableApi::class)
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
@OptIn(UnstableApi::class)
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

// Individual Comment Item Composable (moved out for better organization)
@Composable
fun CommentItem(comment: CommentResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Placeholder for user avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray) // Replace with AsyncImage for real avatars
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Anonymous User", // Placeholder: replace with actual username later
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = comment.text,
                fontSize = 14.sp
            )
            // You could add timestamp here:
            // Text(
            //    text = "2 hours ago", // Format comment.createdAt
            //    fontSize = 12.sp,
            //    color = Color.Gray
            // )
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
