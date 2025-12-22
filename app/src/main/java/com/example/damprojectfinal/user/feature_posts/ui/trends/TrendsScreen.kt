package com.example.damprojectfinal.user.feature_posts.ui.trends


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
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
        val mediaUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull())
        val isCarousel = (post.mediaType == "carousel" || post.mediaUrls.size > 1) && post.mediaType != "reel"

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

        } else if (isCarousel && post.mediaUrls.isNotEmpty()) {
            // Carousel post - use HorizontalPager for horizontal scrolling
            val carouselPagerState = rememberPagerState(
                pageCount = { post.mediaUrls.size }
            )
            
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = carouselPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val imageUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls[page])
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "${post.caption} - Image ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Page indicator at the top (Instagram-style)
                if (post.mediaUrls.size > 1) {
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
                        repeat(post.mediaUrls.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (carouselPagerState.currentPage == index) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (carouselPagerState.currentPage == index) 
                                            Color.White 
                                        else 
                                            Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }

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
            // Author Info - Only show if ownerId is not null and is UserProfile type
            val ownerId = post.ownerId
            val userProfile = ownerId as? UserProfile
            
            if (userProfile != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = userProfile.profilePictureUrl,
                        contentDescription = "Author Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = userProfile.username ?: "User",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            // Caption
            Text(
                text = post.caption ?: "",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
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