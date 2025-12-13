package com.example.damprojectfinal.user.feature_posts.ui.post_management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// --- IMPORTS FOR FIXING ERRORS ---
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import com.example.damprojectfinal.UserRoutes // <--- Ensure this import is correct
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
// --- END IMPORTS FOR FIXING ERRORS ---


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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { onPostClick(post._id) }, // <--- This now calls the correct parameter
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                ) {
                    val rawUrl = if (post.mediaType == "reel" && post.thumbnailUrl != null) {
                        post.thumbnailUrl
                    } else {
                        post.mediaUrls.firstOrNull()
                    }
                    val imageUrlToLoad = BaseUrlProvider.getFullImageUrl(rawUrl)

                    // Debug logging
                    android.util.Log.d("PostsHomeScreen", "📸 Post ID: ${post._id}")
                    android.util.Log.d("PostsHomeScreen", "   Raw URL: $rawUrl")
                    android.util.Log.d("PostsHomeScreen", "   Full URL: $imageUrlToLoad")

                    AsyncImage(
                        model = imageUrlToLoad,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Preparation Time Badge (if available)
                    post.preparationTime?.let { prepTime ->
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = "Preparation Time",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$prepTime minutes",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) Color(0xFFE91E63) else Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(28.dp)
                            .clickable {
                                val wasLiked = isLiked
                                isLiked = !isLiked
                                if (isLiked) {
                                    likeCount++
                                    postsViewModel.incrementLikeCount(post._id)
                                } else {
                                    likeCount--
                                    postsViewModel.decrementLikeCount(post._id)
                                }
                                // Sync with ViewModel after API call
                                scope.launch {
                                    try {
                                        kotlinx.coroutines.delay(100)
                                        val updatedPost =
                                            postsViewModel.posts.value.find { it._id == post._id }
                                        if (updatedPost != null) {
                                            // State already updated optimistically
                                        }
                                    } catch (e: Exception) {
                                        // Silently handle error - state already updated optimistically
                                    }
                                }
                            }
                    )

                    // Rating Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFACC15))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "4.9",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // 2. Content Section (below image)
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.caption,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF111827),
                            modifier = Modifier.weight(1f)
                        )

                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Post Options",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { showOptionsMenu = true }
                            )
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showOptionsMenu = false
                                        onEditClicked(post._id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showOptionsMenu = false
                                        onDeleteClicked(post._id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Report") },
                                    onClick = {
                                        showOptionsMenu = false
                                        println("Report clicked for post: ${post._id}")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Price (if available)
                        if (post.price != null) {
                            Text(
                                text = "${post.price}TND",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF111827)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Comment",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onCommentClick(post._id) }
                                )
                                if (post.commentCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = post.commentCount.toString(), fontSize = 14.sp)
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(onClick = onShareClick)
                            )
                            // Prefer Food Type Button
                            PreferFoodTypeButton(
                                post = post,
                                postsViewModel = postsViewModel,
                                modifier = Modifier.size(24.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isSaved) Color(0xFFFFC107) else Color(0xFF6B7280),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val wasSaved = isSaved
                                            isSaved = !isSaved
                                            if (isSaved) {
                                                saveCount++
                                                postsViewModel.incrementSaveCount(post._id)
                                            } else {
                                                saveCount--
                                                postsViewModel.decrementSaveCount(post._id)
                                            }
                                            // Sync with ViewModel after API call
                                            scope.launch {
                                                try {
                                                    kotlinx.coroutines.delay(100)
                                                    val updatedPost =
                                                        postsViewModel.posts.value.find { it._id == post._id }
                                                    if (updatedPost != null) {
                                                        // State already updated optimistically
                                                    }
                                                } catch (e: Exception) {
                                                    // Silently handle error - state already updated optimistically
                                                }
                                            }
                                        }
                                )
                                if (saveCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = saveCount.toString(), fontSize = 14.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (likeCount > 0) {
                                    Text(text = likeCount.toString(), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

// ------------------------------------------------------
// 👁️ Preview Composable
// ------------------------------------------------------

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