package com.example.damprojectfinal.user.feature_posts.ui.post_management

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import com.example.damprojectfinal.UserRoutes // <--- Ensure this import is correct
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Helper to format large numbers (e.g., 52200 -> "52.2K")
fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

@Composable
fun PostsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    postsViewModel: PostsViewModel = viewModel(),
    selectedFoodType: String? = null,
    headerContent: @Composable () -> Unit
) {
    val posts by postsViewModel.posts.collectAsState()
    val isLoading by postsViewModel.isLoading.collectAsState()
    val errorMessage by postsViewModel.errorMessage.collectAsState()
    val snackbarMessage by postsViewModel.snackbarMessage.collectAsState()
    val userPreferences by postsViewModel.userPreferences.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            postsViewModel.clearSnackbarMessage()
        }
    }

    // Fetch posts when food type filter changes
    LaunchedEffect(selectedFoodType) {
        if (selectedFoodType == null) {
            // Fetch all posts
            postsViewModel.fetchPosts()
        } else {
            // Fetch posts filtered by food type
            postsViewModel.fetchPostsByFoodType(selectedFoodType)
        }
    }

    // Optional: Show personalized feed indicator
    val showPersonalizedIndicator = userPreferences.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional: Personalized Feed Banner
            if (showPersonalizedIndicator) {
                PersonalizedFeedBanner(
                    preferredTypes = userPreferences,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    if (selectedFoodType == null) {
                        postsViewModel.fetchPosts()
                    } else {
                        postsViewModel.fetchPostsByFoodType(selectedFoodType)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            headerContent()
                        }
                    }

                    if (isLoading && posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                            }
                        }
                    } else if (errorMessage != null) {
                        item {
                            Column(
                                modifier = Modifier.fillParentMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error: $errorMessage",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(onClick = { postsViewModel.fetchPosts() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else if (posts.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No posts to display.", modifier = Modifier.padding(16.dp))
                            }
                        }
                    } else {
                        // Actual posts items
                        items(posts) { post ->
                            RecipeCard(
                                post = post,
                                onPostClick = { postId ->
                                    navController.navigate("${UserRoutes.POST_DETAILS_SCREEN}/$postId")
                                },
                                onFavoriteClick = { postId ->
                                    // Handled internally in RecipeCard now
                                },
                                onCommentClick = { postId ->
                                    navController.navigate("${UserRoutes.POST_DETAILS_SCREEN}/$postId")
                                },
                                onShareClick = { /* TODO: Implement share functionality */ },
                                onBookmarkClick = { postId ->
                                    // Handled internally in RecipeCard now
                                },
                                onEditClicked = { postId ->
                                    val encodedCaption = URLEncoder.encode(
                                        post.caption,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate(
                                        "${
                                            UserRoutes.EDIT_POST_SCREEN.replace(
                                                "{postId}",
                                                postId
                                            ).replace("{initialCaption}", encodedCaption)
                                        }"
                                    )
                                },
                                onDeleteClicked = { postId -> postsViewModel.deletePost(postId) },
                                postsViewModel = postsViewModel
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar Host - placed in Box to use align
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ------------------------------------------------------
// 🎯 Personalized Feed Banner (Optional)
// ------------------------------------------------------
@Composable
fun PersonalizedFeedBanner(
        preferredTypes: List<String>,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Personalized for you",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "${preferredTypes.size} preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }

// ------------------------------------------------------
// ⭐ Prefer Food Type Button Component
// ------------------------------------------------------
@Composable
fun PreferFoodTypeButton(
        post: PostResponse,
        postsViewModel: PostsViewModel,
        modifier: Modifier = Modifier
    ) {
        val isPreferred by remember(post._id) {
            derivedStateOf {
                postsViewModel.isPostFoodTypePreferred(post._id)
            }
        }

        val isPreferring by postsViewModel.preferringPosts.collectAsState()
        val isLoading = isPreferring.contains(post._id)

        val icon = if (isPreferred) {
            Icons.Filled.Star
        } else {
            Icons.Outlined.Star
        }

        val color = if (isPreferred) {
            Color(0xFFFFC107) // Amber/Gold color for preferred
        } else {
            Color(0xFF6B7280) // Gray for not preferred
        }

        Icon(
            imageVector = icon,
            contentDescription = if (isPreferred) "Remove from preferences" else "Add to preferences",
            tint = color,
            modifier = modifier
                .clickable(
                    enabled = !isLoading && post.foodType != null,
                    onClick = {
                        if (!isPreferred) {
                            postsViewModel.preferFoodType(post._id)
                        }
                    }
                )
        )
    }

// ------------------------------------------------------
// 🥗 Recipe Card (MODIFIED to accept onPostClick parameter)
// ------------------------------------------------------
@Composable
fun RecipeCard(
        post: PostResponse, // Accepts a PostResponse object
        onPostClick: (postId: String) -> Unit, // <--- CRITICAL FIX: onPostClick parameter added here
        onFavoriteClick: (postId: String) -> Unit,
        onCommentClick: (postId: String) -> Unit,
        onShareClick: () -> Unit,
        onBookmarkClick: (postId: String) -> Unit,
        onEditClicked: (postId: String) -> Unit,
        onDeleteClicked: (postId: String) -> Unit,
        postsViewModel: PostsViewModel = viewModel()
    ) {
        // Track like and save state - same logic as ReelItem
        var isLiked by remember(post._id) { mutableStateOf(post.likeCount > 0) }
        var isSaved by remember(post._id) { mutableStateOf(post.saveCount > 0) }
        var likeCount by remember(post._id) { mutableStateOf(post.likeCount) }
        var saveCount by remember(post._id) { mutableStateOf(post.saveCount) }
        val scope = rememberCoroutineScope()

        // Update counts when post changes (from API updates)
        LaunchedEffect(post.likeCount, post.saveCount) {
            likeCount = post.likeCount
            saveCount = post.saveCount
            // Update like/save state based on counts
            isLiked = post.likeCount > 0
            isSaved = post.saveCount > 0
        }

        var showOptionsMenu by remember { mutableStateOf(false) }

        // Professional color scheme with enhanced palette
        val AccentYellow = Color(0xFFFFC107)
        val AccentYellowLight = Color(0xFFFFF8E1)
        val DarkText = Color(0xFF1F2937)
        val MediumGray = Color(0xFF6B7280)
        val LightGray = Color(0xFFF3F4F6)
        val CardBackground = Color(0xFFFFFFFF)
        val SurfaceElevated = Color(0xFFFAFAFA)
        val ShadowColor = Color(0x1A000000)
        
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "scale"
        )
        
        val elevation by animateDpAsState(
            targetValue = if (isPressed) 2.dp else 8.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "elevation"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = elevation.toPx()
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onPostClick(post._id)
                },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1E8)) // Beige background like in image
        ) {
            // Main Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Large image height
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                val rawUrl = if (post.mediaType == "reel" && post.thumbnailUrl != null) {
                    post.thumbnailUrl
                } else {
                    post.mediaUrls.firstOrNull()
                }
                val imageUrlToLoad = BaseUrlProvider.getFullImageUrl(rawUrl)

                // Main Image
                AsyncImage(
                    model = imageUrlToLoad,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay at bottom for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 200f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Bottom overlay content: Profile info (left), Caption (center), Engagement metrics (right)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                            // Left: Profile picture, name, and handle
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Profile picture with orange border
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800), CircleShape) // Orange border
                                        .padding(2.dp)
                                ) {
                                    AsyncImage(
                                        model = BaseUrlProvider.getFullImageUrl(post.ownerId?.profilePictureUrl),
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        placeholder = rememberVectorPainter(Icons.Filled.Person),
                                        error = rememberVectorPainter(Icons.Filled.Person)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Name and handle
                                Column {
                                    Text(
                                        text = post.ownerId?.fullName ?: post.ownerId?.username ?: "Unknown",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "@${post.ownerId?.username ?: "user"}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            
                            // Right: Engagement metrics (vertical)
                            Column(
                                modifier = Modifier.padding(start = 16.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Likes
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Likes",
                                        tint = if (isLiked) Color(0xFFE91E63) else Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                isLiked = !isLiked
                                                if (isLiked) {
                                                    likeCount++
                                                    postsViewModel.incrementLikeCount(post._id)
                                                } else {
                                                    likeCount--
                                                    postsViewModel.decrementLikeCount(post._id)
                                                }
                                            }
                                    )
                                    Text(
                                        text = formatCount(likeCount),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Comments
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Comments",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { onCommentClick(post._id) }
                                    )
                                    Text(
                                        text = formatCount(post.commentCount),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                    }
                    
                    // Caption in the center-bottom
                    Text(
                        text = post.caption,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 60.dp, bottom = 50.dp) // Position below profile info
                            .fillMaxWidth(0.7f) // Take 70% width
                    )
                }
            }
        }
    }

@Preview(showBackground = true, name = "Food Posts Screen Preview")
@Composable
fun PostsScreenPreview() {
        DamProjectFinalTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                PostsScreen(navController = navController, headerContent = {})
            }
        }
    }

//
//@Preview(showBackground = true, name = "Single Recipe Card Preview")
//@Composable
//fun RecipeCardPreview() {
//    DamProjectFinalTheme {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            val dummyPost = PostResponse(
//                _id = "dummy_id",
//                caption = "Spicy Wok Noodle Bowl",
//                mediaUrls = listOf("https://picsum.photos/300/200"),
//                mediaType = "image",
//                createdAt = "2025-01-01T00:00:00Z",
//                updatedAt = "2025-01-01T00:00:00Z",
//                version = 0,
//                viewsCount = 0,
//                thumbnailUrl = null,
//                duration = null,
//                aspectRatio = null,
//                likeCount = 12,
//                commentCount = 5,
//                saveCount = 3,
//                // --- ADD THE MISSING 'userId' PARAMETER HERE ---
//                userId = UserProfile(
//                    _id = "dummy_user_id",
//                    username = "preview_user",
//                    fullName = "Preview User",
//                    profilePictureUrl = "https://picsum.photos/60", // Example image
//                    followerCount = 100,
//                    followingCount = 50,
//                    bio = "A preview user for testing",
//                    postCount = 5,
//                    phone = null, address = null, email = null, isActive = true
//                )
//                // --- END ADDITION ---
//            )
//            val navController = rememberNavController()
//            RecipeCard(
//                post = dummyPost,
//                onPostClick = { postId -> println("Navigate to Post Details for $postId") },
//                onFavoriteClick = { postId -> println("Favorite clicked for $postId") },
//                onCommentClick = { postId -> println("Comment clicked for $postId") },
//                onShareClick = {},
//                onBookmarkClick = { postId -> println("Bookmark clicked for $postId") },
//                onEditClicked = { postId ->
//                    val encodedCaption = URLEncoder.encode(dummyPost.caption, StandardCharsets.UTF_8.toString())
//                    navController.navigate("${UserRoutes.EDIT_POST_SCREEN}/$postId/$encodedCaption")
//                },
//                onDeleteClicked = { postId -> println("Delete clicked for $postId") }
//            )
//        }
//    }
//}