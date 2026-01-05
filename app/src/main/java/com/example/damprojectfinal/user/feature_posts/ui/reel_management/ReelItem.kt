package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout // <-- Ensure this import is present and correct
import androidx.navigation.NavController
import com.example.damprojectfinal.core.dto.posts.PostResponse
import coil.compose.AsyncImage // <-- Ensure this import is present
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.ReelsViewModel

import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api

// THIS IS THE CRUCIAL LINE for the "UnstableApi" warnings within ReelItem
@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelItem(
    reelPost: PostResponse,
    isCurrentItem: Boolean, // Indicates if this reel is currently in full view
    onReelClick: (String) -> Unit, // Callback for clicks on the reel (e.g., pause/play)
    onCommentClick: (String) -> Unit, // NEW: Callback for comment clicks
    onShareClick: (String) -> Unit, // NEW: Callback for share clicks
    navController: NavController,
    postsViewModel: PostsViewModel,
    reelsViewModel: ReelsViewModel
) {
    // Track like and save state - use isLiked/isSaved from backend if available
    var isLiked by remember(reelPost._id) { mutableStateOf(reelPost.isLiked ?: false) }
    var isSaved by remember(reelPost._id) { mutableStateOf(reelPost.isSaved ?: false) }
    var likeCount by remember(reelPost._id) { mutableStateOf(reelPost.likeCount) }
    var saveCount by remember(reelPost._id) { mutableStateOf(reelPost.saveCount) }
    
    val scope = rememberCoroutineScope()
    
    // Update counts when post changes (from API updates)
    LaunchedEffect(reelPost.likeCount, reelPost.saveCount, reelPost.isLiked, reelPost.isSaved) {
        likeCount = reelPost.likeCount
        saveCount = reelPost.saveCount
        // Use backend-provided isLiked/isSaved if available, otherwise keep current state
        reelPost.isLiked?.let { isLiked = it }
        reelPost.isSaved?.let { isSaved = it }
    }
    val context = LocalContext.current
    // Track playback state for this specific reel
    var isPlaying by remember(reelPost._id) { mutableStateOf(true) }
    
    val videoUrl = BaseUrlProvider.getFullImageUrl(reelPost.mediaUrls.firstOrNull()) ?: ""
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            volume = 0f // Start muted, user can unmute later
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop the video
        }
    }

    // Reset playing state when item becomes current
    LaunchedEffect(isCurrentItem) {
        if (isCurrentItem) {
            isPlaying = true // Auto-play when becoming current
        }
    }
    
    // Effect to control playback based on `isCurrentItem` and `isPlaying`
    DisposableEffect(isCurrentItem, isPlaying) {
        if (isCurrentItem && isPlaying) {
            exoPlayer.playWhenReady = true // Autoplay when current and playing
            exoPlayer.play()
            // Optional: Call incrementReelView API here via ViewModel
        } else {
            exoPlayer.pause() // Pause when not current or when paused
        }
        onDispose {
            // When composable leaves composition (e.g., scrolled away), ensure player is paused
            exoPlayer.pause()
        }
    }
    
    // Handle click to toggle playback
    val handleReelClick: () -> Unit = {
        if (isCurrentItem) {
            // Only toggle if this is the current item
            isPlaying = !isPlaying
            reelsViewModel.togglePlayback(reelPost._id)
        } else {
            // If not current item, just notify (shouldn't happen, but handle it)
            onReelClick(reelPost._id)
        }
    }

    // Ensure player is released when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { handleReelClick() } // Handle reel click (e.g., pause/play toggle)
    ) {
        // Back button (aligned to top-start, padded below status bar) - Enhanced design
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // --- ExoPlayer PlayerView ---
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Hide default controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        )

        // --- UI Overlays (Placeholders) ---
        // User Info (Bottom-Left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.7f) // Occupy 70% of width
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = reelPost.ownerId?.profilePictureUrl ?: "https://via.placeholder.com/40/0000FF/FFFFFF?text=P",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape) // Add CircleShape for profile pic
                        .clickable { /* Handle profile click */ }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = reelPost.ownerId?.fullName ?: reelPost.ownerId?.username ?: "Unknown User",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { /* Handle username click */ }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Follow",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable { /* Handle follow click */ }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = reelPost.caption,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Interaction Buttons (Right-Side)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFE91E63) else Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            val wasLiked = isLiked
                            isLiked = !isLiked
                            if (isLiked) {
                                likeCount++
                                postsViewModel.incrementLikeCount(reelPost._id)
                            } else {
                                likeCount--
                                postsViewModel.decrementLikeCount(reelPost._id)
                            }
                            // Update ReelsViewModel after API call completes
                            scope.launch {
                                try {
                                    // Wait a bit for PostsViewModel to update, then sync
                                    kotlinx.coroutines.delay(100)
                                    val updatedPost = postsViewModel.posts.value.find { it._id == reelPost._id }
                                    if (updatedPost != null) {
                                        reelsViewModel.updateReel(updatedPost)
                                    }
                                } catch (e: Exception) {
                                    // Silently handle error - state already updated optimistically
                                }
                            }
                        }
                )
                Text(
                    text = likeCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            onCommentClick(reelPost._id)
                        }
                )
                Text(
                    text = reelPost.commentCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Share
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onShareClick(reelPost._id) }
            )

            // Save
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Color(0xFFFFC107) else Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            val wasSaved = isSaved
                            isSaved = !isSaved
                            if (isSaved) {
                                saveCount++
                                postsViewModel.incrementSaveCount(reelPost._id)
                            } else {
                                saveCount--
                                postsViewModel.decrementSaveCount(reelPost._id)
                            }
                            // Update ReelsViewModel after API call completes
                            scope.launch {
                                try {
                                    // Wait a bit for PostsViewModel to update, then sync
                                    kotlinx.coroutines.delay(100)
                                    val updatedPost = postsViewModel.posts.value.find { it._id == reelPost._id }
                                    if (updatedPost != null) {
                                        reelsViewModel.updateReel(updatedPost)
                                    }
                                } catch (e: Exception) {
                                    // Silently handle error - state already updated optimistically
                                }
                            }
                        }
                )
                Text(
                    text = saveCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // More Options
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More Options",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { /* Handle more options click */ }
            )
        }
    }
}

// ------------------------------------------------------
// 👁️ Preview Composable (MUST BE TOP-LEVEL, no longer nested)
// ------------------------------------------------------

//@UnstableApi
//@Preview(showBackground = true, name = "Reel Item Preview")
//@Composable
//fun ReelItemPreview() {
//    DamProjectFinalTheme {
//        val dummyReel = PostResponse(
//            _id = "dummy_reel_id",
//            caption = "Delicious food preparation video! #foodie #cooking #recipe",
//            mediaUrls = listOf("android.resource://com.example.damprojectfinal/raw/sample_video"),
//            mediaType = "reel",
//            createdAt = "2025-11-28T10:00:00Z",
//            updatedAt = "2025-11-28T10:00:00Z",
//            version = 0,
//            viewsCount = 12345,
//            thumbnailUrl = null,
//            duration = 60.0,
//            aspectRatio = "9:16",
//            likeCount = 0,
//            commentCount = 0,
//            saveCount = 0,
//            // --- ADD THE MISSING 'userId' PARAMETER HERE ---
//            userId = UserProfile( // Provide a dummy UserProfile for the reel's author
//                _id = "dummy_user_id_reel",
//                username = "ReelCreator",
//                fullName = "Reel Creator Name",
//                profilePictureUrl = "https://picsum.photos/id/70/60/60", // Example image
//                followerCount = 5000,
//                followingCount = 100,
//                bio = "Short video enthusiast",
//                postCount = 20,
//                phone = null, address = null, email = null, isActive = true
//            )
//            // --- END ADDITION ---
//        )
//        ReelItem(reelPost = dummyReel, isCurrentItem = true) { /* No-op for preview */ }
//    }
//}