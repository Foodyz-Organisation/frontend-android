package com.example.damprojectfinal.user.feature_posts.ui.post_management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import com.example.damprojectfinal.UserRoutes // <--- Ensure this import is correct
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
//
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Send
import com.example.damprojectfinal.core.dto.posts.CommentResponse
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Close
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.damprojectfinal.user.common._component.SharePostDialog
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.snapshotFlow

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
    
    // --- NEW: Comments Sheet State ---
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }
    val activeComments by postsViewModel.activeComments.collectAsState()
    val isCommentsLoading by postsViewModel.areCommentsLoading.collectAsState()

    // --- NEW: Share Dialog State ---
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedPostIdForSharing by remember { mutableStateOf<String?>(null) }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    // Track visible reel post IDs for auto-play
    var visibleReelIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Note: Snackbar for preference messages removed - preferences are now learned automatically

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
    
    // Track visible reel posts for auto-play
    LaunchedEffect(listState, posts) {
        snapshotFlow { 
            val layoutInfo = listState.layoutInfo
            val visibleItemIndices = layoutInfo.visibleItemsInfo.map { it.index }
            // Account for header item at index 0, so posts start at index 1
            visibleItemIndices.mapNotNull { itemIndex ->
                val postIndex = itemIndex - 1 // Subtract 1 for header
                if (postIndex >= 0 && postIndex < posts.size) {
                    val post = posts[postIndex]
                    if (post.mediaType == "reel") post._id else null
                } else null
            }.toSet()
        }.collect { newVisibleReelIds ->
            visibleReelIds = newVisibleReelIds
        }
    }

    // Note: Personalized feed banner removed - preferences are now learned automatically from interactions
    // Feed is automatically personalized by backend based on user interactions (like, save, comment, view)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Note: PersonalizedFeedBanner removed - feed is automatically personalized by backend

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
                    state = listState,
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
                            // Check if this reel is visible
                            val isReelVisible = post.mediaType == "reel" && visibleReelIds.contains(post._id)
                            
                            RecipeCard(
                                post = post,
                                isReelVisible = isReelVisible,
                                onPostClick = { postId ->
                                    navController.navigate("${UserRoutes.POST_DETAILS_SCREEN}/$postId")
                                },
                                onFavoriteClick = { postId ->
                                    // Handled internally in RecipeCard now
                                },
                                onCommentClick = { postId ->
                                    // Open Bottom Sheet
                                    selectedPostIdForComments = postId
                                    postsViewModel.loadComments(postId)
                                    showCommentsSheet = true
                                },
                                onShareClick = { postId ->
                                    selectedPostIdForSharing = postId
                                    showShareDialog = true
                                },
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
                                onOrderClick = { professionalId, foodType ->
                                    // Navigate to menu with optional highlightCategory
                                    // Set highlightCategory in savedStateHandle before navigation
                                    navController.currentBackStackEntry?.savedStateHandle?.set("highlightCategory", foodType)
                                    navController.navigate("menu_order_route/$professionalId")
                                },
                                postsViewModel = postsViewModel
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // --- NEW: Comments Sheet ---
        if (showCommentsSheet && selectedPostIdForComments != null) {
            val selectedPost = posts.find { it._id == selectedPostIdForComments }
            CommentsSheet(
                post = selectedPost,
                comments = activeComments,
                isLoading = isCommentsLoading,
                onAddComment = { text ->
                    selectedPostIdForComments?.let { postId ->
                        postsViewModel.createComment(postId, text)
                    }
                },
                onDismiss = {
                    showCommentsSheet = false
                    selectedPostIdForComments = null
                    postsViewModel.clearActiveComments()
                }
            )
        }
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
// Note: PreferFoodTypeButton removed - preferences are now learned automatically from user interactions
// (like, save, comment, view actions automatically update preferences)
// ------------------------------------------------------
// 🥗 Recipe Card (MODIFIED to accept onPostClick parameter)
// ------------------------------------------------------
@Composable
fun RecipeCard(
    post: PostResponse,
    isReelVisible: Boolean = false,
    onPostClick: (postId: String) -> Unit,
    onFavoriteClick: (postId: String) -> Unit,
    onCommentClick: (postId: String) -> Unit,
    onShareClick: (postId: String) -> Unit,
    onBookmarkClick: (postId: String) -> Unit,
    onEditClicked: (postId: String) -> Unit,
    onDeleteClicked: (postId: String) -> Unit,
    onOrderClick: ((professionalId: String, foodType: String?) -> Unit)? = null,
    postsViewModel: PostsViewModel = viewModel()
) {
    // Track state - use isLiked/isSaved from backend if available, otherwise default to false
    var isLiked by remember(post._id) { mutableStateOf(post.isLiked ?: false) }
    var isSaved by remember(post._id) { mutableStateOf(post.isSaved ?: false) }
    var likeCount by remember(post._id) { mutableStateOf(post.likeCount) }
    var saveCount by remember(post._id) { mutableStateOf(post.saveCount) }
    
    // Track mute state for reels (false = unmuted/sound on, true = muted/sound off)
    var isMuted by remember(post._id) { mutableStateOf(false) }

    LaunchedEffect(post.likeCount, post.saveCount, post.isLiked, post.isSaved) {
        likeCount = post.likeCount
        saveCount = post.saveCount
        // Use backend-provided isLiked/isSaved if available, otherwise keep current state
        post.isLiked?.let { isLiked = it }
        post.isSaved?.let { isSaved = it }
    }



    // Responsive Height Calculation
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val imageHeight = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        screenHeight * 0.7f
    } else {
        screenHeight * 0.5f // 50% of screen height for immersive feel on phones
    }

    // Magazine Design v2 - "Clean Stack" (No Obstruction)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp), // Standard margin
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 1. Image Header Section (Image + Floating User Info)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                // Media Display - Video for reels, Image for others
                if (post.mediaType == "reel" && post.mediaUrls.firstOrNull() != null) {
                    // Video Player for Reels
                    val videoUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull()) ?: ""
                    
                    // Main clickable area for post navigation
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onPostClick(post._id) }
                    ) {
                        FeedVideoPlayerWithVisibility(
                            videoUrl = videoUrl,
                            isVisible = isReelVisible,
                            isMuted = isMuted,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Sound Icon Overlay for Reels (Clickable - on top layer)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        val soundIconInteractionSource = remember { MutableInteractionSource() }
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(
                                    onClick = { isMuted = !isMuted },
                                    indication = null,
                                    interactionSource = soundIconInteractionSource
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                    contentDescription = if (isMuted) "Unmute" else "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Static Image for non-reel posts
                    val imageUrl = post.mediaUrls.firstOrNull()
                    AsyncImage(
                        model = BaseUrlProvider.getFullImageUrl(imageUrl),
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onPostClick(post._id) },
                        placeholder = rememberVectorPainter(Icons.Outlined.Image),
                        error = rememberVectorPainter(Icons.Outlined.BrokenImage)
                    )
                }

                // Floating Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            ) {
                                AsyncImage(
                                    model = BaseUrlProvider.getFullImageUrl(post.ownerId?.profilePictureUrl),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.ownerId?.username ?: "Chef",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }


                }
            }

            // 2. Bottom Content Section (White Background)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Action Icons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFFFC107) else Color(0xFF1F2937),
                            modifier = Modifier
                                .size(28.dp)
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
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onCommentClick(post._id) }
                        )
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onShareClick(post._id) }
                        )
                    }
                    
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) Color(0xFFFFC107) else Color(0xFF1F2937),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                isSaved = !isSaved
                                if (isSaved) {
                                    saveCount++
                                    postsViewModel.incrementSaveCount(post._id)
                                } else {
                                    saveCount--
                                    postsViewModel.decrementSaveCount(post._id)
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Likes Count
                Text(
                    text = "${formatCount(likeCount)} likes",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Caption
                if (post.caption.isNotEmpty()) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = Color(0xFF1F2937),
                            lineHeight = 24.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // View Comments
                if (post.commentCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "View all ${post.commentCount} comments",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF9CA3AF)
                        ),
                        modifier = Modifier.clickable { onCommentClick(post._id) }
                    )
                }

                // Order Button (only for ProfessionalAccount posts)
                if (post.ownerModel == "ProfessionalAccount" && post.ownerId?._id != null && onOrderClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onOrderClick(post.ownerId!!._id, post.foodType)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Order",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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

@Preview(showBackground = true, name = "Single Recipe Card Preview")
@Composable
fun RecipeCardPreview() {
    DamProjectFinalTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dummyPost = PostResponse(
                _id = "dummy_id",
                caption = "test supabase",
                mediaUrls = listOf("https://picsum.photos/400/600"),
                mediaType = "image",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
                version = 0,
                ownerId = com.example.damprojectfinal.core.dto.normalUser.UserProfile(
                    _id = "dummy_user_id",
                    username = "charlot",
                    fullName = "charlot",
                    profilePictureUrl = "https://picsum.photos/60",
                    followerCount = 0,
                    followingCount = 0,
                    postCount = 0,
                    bio = "",
                    phone = null,
                    address = null,
                    email = "charlot@gmail.com",
                    isActive = true
                ),
                ownerModel = "ProfessionalAccount",
                viewsCount = 0,
                thumbnailUrl = null,
                duration = null,
                aspectRatio = null,
                likeCount = 1,
                commentCount = 0,
                saveCount = 0,
                foodType = "Seafood",
                price = null,
                preparationTime = null,
                description = null,
                ingredients = null,
                postRating = null,
                reviewsCount = null
            )
            
            // Mock ViewModel for preview (no-op methods)
            val mockViewModel = object : PostsViewModel() {
                // Prevent API calls in preview
                override fun fetchPosts() {
                    // No-op for preview
                }
                override fun incrementLikeCount(postId: String) {
                    // No-op for preview
                }
                override fun decrementLikeCount(postId: String) {
                    // No-op for preview
                }
                override fun incrementSaveCount(postId: String) {
                    // No-op for preview
                }
                override fun decrementSaveCount(postId: String) {
                    // No-op for preview
                }
            }
            
            val navController = rememberNavController()
            RecipeCard(
                post = dummyPost,
                onPostClick = { postId -> println("Navigate to Post Details for $postId") },
                onFavoriteClick = { postId -> println("Favorite clicked for $postId") },
                onCommentClick = { postId -> println("Comment clicked for $postId") },
                onShareClick = {},
                onBookmarkClick = { postId -> println("Bookmark clicked for $postId") },
                onEditClicked = { postId ->
                    val encodedCaption = URLEncoder.encode(dummyPost.caption, StandardCharsets.UTF_8.toString())
                    navController.navigate("${UserRoutes.EDIT_POST_SCREEN.replace("{postId}", postId).replace("{initialCaption}", encodedCaption)}")
                },
                onDeleteClicked = { postId -> println("Delete clicked for $postId") },
                postsViewModel = mockViewModel
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    post: PostResponse?,
    comments: List<CommentResponse>,
    isLoading: Boolean,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f) // Taller sheet like in screenshot
                .padding(bottom = 20.dp)
        ) {
            // 1. Header with Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6))

            // 2. Post Summary Snippet (Gray Box)
            if (post != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)), // Light gray
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = post.caption.ifEmpty { "No caption" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${post.likeCount} likes   ${post.commentCount} comments",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }

            // 3. Comments List
            if (isLoading && comments.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (comments.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }

            // 4. Input Field (Bottom)
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding() // Key for keyboard handling
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar (Optional, placeholder for now)
                
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50), // Pill shape
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.LightGray, // Keep it subtle
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                         IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText)
                                    newCommentText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF4B5563))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentResponse) {
    Row(modifier = Modifier.fillMaxWidth()) {
       // Avatar
       val authorAvatar = comment.authorAvatar
       AsyncImage(
            model = BaseUrlProvider.getFullImageUrl(authorAvatar),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
            contentScale = ContentScale.Crop
       )
       Spacer(modifier = Modifier.width(12.dp))
       Column {
           Row(verticalAlignment = Alignment.CenterVertically) {
               Text(
                   text = comment.authorUsername ?: "User",
                   fontWeight = FontWeight.Bold,
                   style = MaterialTheme.typography.bodyMedium
               )
               Spacer(modifier = Modifier.width(8.dp))
               Text(
                   text = "Just now", 
                   style = MaterialTheme.typography.bodySmall,
                   color = Color.Gray
               )
           }
           Text(
               text = comment.text,
               style = MaterialTheme.typography.bodyMedium,
               color = Color(0xFF1F2937)
           )
       }
    }
}

// ------------------------------------------------------
// 🎥 Feed Video Player with Visibility Detection
// ------------------------------------------------------
@OptIn(UnstableApi::class)
@Composable
fun FeedVideoPlayerWithVisibility(
    videoUrl: String,
    isVisible: Boolean,
    isMuted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            volume = 1f // Start with sound on (unmuted)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop
            playWhenReady = false // Don't auto-play until visible
        }
    }
    
    // Control volume based on mute state
    DisposableEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
        onDispose {
            // Cleanup handled in the final DisposableEffect
        }
    }
    
    // Control playback based on visibility
    DisposableEffect(isVisible) {
        if (isVisible) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            exoPlayer.playWhenReady = false
        }
        onDispose {
            // Cleanup handled in the final DisposableEffect
        }
    }
    
    // Manage Lifecycle - release player when composable is disposed
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