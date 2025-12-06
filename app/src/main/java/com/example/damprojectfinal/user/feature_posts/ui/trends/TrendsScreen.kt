package com.example.damprojectfinal.user.feature_posts.ui.trends


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Using Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.posts.PostResponse
// import com.example.damprojectfinal.core.dto.normalUser.UserProfile // <-- REMOVED THIS IMPORT
import com.example.damprojectfinal.core.dto.posts.PostOwnerDetails // <-- NEW IMPORT: Use PostOwnerDetails
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme // Assuming your app's theme
import androidx.media3.common.MediaItem // ExoPlayer
import androidx.media3.exoplayer.ExoPlayer // ExoPlayer
import androidx.media3.ui.PlayerView // ExoPlayer integration with AndroidView
import androidx.compose.ui.viewinterop.AndroidView // ExoPlayer integration
import androidx.compose.runtime.DisposableEffect // For player cleanup
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.damprojectfinal.core.dto.posts.TrendingPostsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.platform.LocalLifecycleOwner


// Helper composable for a single trending post item
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrendingPostItem(post: PostResponse) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Background for full-screen video
    ) {
        val mediaUrl = post.mediaUrls.firstOrNull()

        // --- Media Display ---
        if (post.mediaType == "reel" && mediaUrl != null) {
            val context = LocalContext.current
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(mediaUrl))
                    prepare()
                    playWhenReady = true // Autoplay
                    repeatMode = ExoPlayer.REPEAT_MODE_ALL // Loop video
                }
            }

            // Clean up player on dispose
            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // No controls for reel-like experience
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

        } else if (post.mediaType == "image" && mediaUrl != null) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = post.caption,
                contentScale = ContentScale.Fit, // Fit to screen height
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder for unsupported media type or missing URL
            Text(
                text = "Media not supported or missing",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- Overlay Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp) // Adjust based on bottom navigation/system bars
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            // Author Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AsyncImage(
                    model = post.ownerId?.profilePictureUrl, // <-- CORRECTED: Use ownerId
                    contentDescription = "Author Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = post.ownerId?.fullName ?: "Unknown User", // <-- CORRECTED: Use ownerId
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // You can add a follow button here if desired, but for now we're simplifying
                // Spacer(modifier = Modifier.width(8.dp))
                // Button(...)
            }

            // Caption
            Text(
                text = post.caption ?: "",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            // Display interactivityScore if you included it in PostResponse.
            // Text(text = "Score: ${post.interactivityScore}", color = Color.White)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrendsScreen(
    navController: NavController,
    viewModel: TrendingPostsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trending") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f), // Transparent background over video
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black // Ensure background is black for full-screen content
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.posts.isEmpty() -> {
                    Text(
                        text = "No trending posts found.",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val pagerState = rememberPagerState(pageCount = { uiState.posts.size })
                    VerticalPager(state = pagerState) { page ->
                        TrendingPostItem(post = uiState.posts[page])
                    }
                }
            }
        }
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun TrendingPostItemPreview() {
//    DamProjectFinalTheme() {
//        // Create a PostOwnerDetails object for the dummy post
//        val dummyOwner = PostOwnerDetails( // <-- CORRECTED: Use PostOwnerDetails
//            _id = "author_id",
//            username = "FoodieUser123",
//            fullName = "Foodie User",
//            profilePictureUrl = "https://picsum.photos/id/1005/60/60",
//            followerCount = 100,
//            followingCount = 50,
//            email = "foodie@example.com" // Added email as it's part of PostOwnerDetails
//        )
//        val dummyPost = PostResponse(
//            _id = "post_id",
//            caption = "Delicious pizza baking in the wood-fired oven!",
//            mediaUrls = listOf("https://picsum.photos/id/1070/720/1280"), // Sample image URL
//            mediaType = "image", // or "reel" for video
//            ownerId = dummyOwner, // <-- CORRECTED: Use ownerId
//            ownerModel = "UserAccount", // <-- NEW: Add ownerModel
//            createdAt = "", updatedAt = "", version = 1, likeCount = 0, commentCount = 0, saveCount = 0
//            // Add isLiked and isSaved if you want to preview their default state
//            ,isLiked = false, isSaved = false
//        )
//        TrendingPostItem(post = dummyPost)
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun TrendsScreenPreview() {
//    DamProjectFinalTheme() {
//        val navController = rememberNavController()
//        val dummyOwner = PostOwnerDetails( // <-- CORRECTED: Use PostOwnerDetails for preview
//            _id = "author_id",
//            username = "FoodieUser123",
//            fullName = "Foodie User",
//            profilePictureUrl = "https://picsum.photos/id/1005/60/60",
//            followerCount = 100,
//            followingCount = 50,
//            email = "foodie@example.com"
//        )
//        val dummyPosts = listOf(
//            PostResponse(
//                _id = "post1", caption = "Amazing pizza", mediaUrls = listOf("https://picsum.photos/id/1070/720/1280"),
//                mediaType = "image", ownerId = dummyOwner, ownerModel = "UserAccount", createdAt = "", updatedAt = "", version = 1, likeCount = 100, commentCount = 20, saveCount = 30
//            ),
//            PostResponse(
//                _id = "post2", caption = "Fresh pasta recipe", mediaUrls = listOf("https://picsum.photos/id/1080/720/1280"),
//                mediaType = "image", ownerId = dummyOwner, ownerModel = "UserAccount", createdAt = "", updatedAt = "", version = 1, likeCount = 80, commentCount = 15, saveCount = 25
//            )
//        )
//
//        // --- SIMPLIFIED PREVIEW VIEWMODEL CREATION ---
//        val mockViewModel = object : TrendingPostsViewModel() {
//
//            // Override the uiState property directly with a MutableStateFlow of dummy data
//            override val uiState: StateFlow<TrendingPostsUiState> = MutableStateFlow(
//                TrendingPostsUiState(
//                    posts = dummyPosts,
//                    isLoading = false,
//                    errorMessage = null
//                )
//            ).asStateFlow() // Convert MutableStateFlow to StateFlow
//
//            // Provide no-op implementations for functions that would trigger API calls
//            override fun fetchTrendingPosts(limit: Int) { /* Do nothing in preview */ }
//            override fun refreshTrendingPosts() { /* Do nothing in preview */ }
//        }
//        // --- END SIMPLIFIED PREVIEW VIEWMODEL CREATION ---
//
//        // Pass the mock ViewModel to the TrendsScreen
//        TrendsScreen(navController = navController, viewModel = mockViewModel)
//    }
