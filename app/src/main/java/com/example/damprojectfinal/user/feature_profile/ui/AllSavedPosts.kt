package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import kotlinx.coroutines.launch
import com.example.damprojectfinal.UserRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSavedPosts(
    navController: NavController,
    userId: String,
    postsViewModel: PostsViewModel = viewModel()
) {
    var posts by remember { mutableStateOf<List<PostResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Fetch saved posts when screen loads
    LaunchedEffect(userId) {
        isLoading = true
        errorMessage = null
        try {
            posts = RetrofitClient.postsApiService.getSavedPosts()
        } catch (e: Exception) {
            errorMessage = "Failed to load saved posts: ${e.localizedMessage ?: e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Posts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                Text("No saved posts available.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    SavedPostItem(
                        post = post,
                        onLikeClick = { postId ->
                            if (post.likeCount > 0) {
                                postsViewModel.decrementLikeCount(postId)
                            } else {
                                postsViewModel.incrementLikeCount(postId)
                            }
                        },
                        onCommentClick = { postId ->
                            navController.navigate("${UserRoutes.COMMENT_SCREEN}/$postId")
                        },
                        onUnsaveClick = { postId ->
                            scope.launch {
                                postsViewModel.decrementSaveCount(postId)
                                // Remove from list after unsaving
                                posts = posts.filter { it._id != postId }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedPostItem(
    post: PostResponse,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onUnsaveClick: (String) -> Unit
) {
    var isLiked by remember(post._id, post.likeCount) { mutableStateOf(post.likeCount > 0) }
    var likeCount by remember(post._id, post.likeCount) { mutableStateOf(post.likeCount) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Post Image
            val imageUrlToLoad = if (post.mediaType == "reel" && post.thumbnailUrl != null) {
                post.thumbnailUrl
            } else {
                post.mediaUrls.firstOrNull()
            }

            AsyncImage(
                model = imageUrlToLoad,
                contentDescription = post.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            // Icons Row: Like/Comment on left, Unsave on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Like and Comment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like Icon with count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            if (isLiked) {
                                likeCount++
                                onLikeClick(post._id)
                            } else {
                                likeCount--
                                onLikeClick(post._id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFE91E63) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = likeCount.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }

                    // Comment Icon with count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentClick(post._id) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = post.commentCount.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }

                // Right side: Unsave button (highlighted as clicked)
                IconButton(
                    onClick = { onUnsaveClick(post._id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Unsave",
                        tint = Color(0xFFFFC107), // Highlighted color (yellow/orange)
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Caption
            Text(
                text = post.caption,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

