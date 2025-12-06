// app/src/main/java/com/example/damprojectfinal/user/feature_posts/ui/ReelItem.kt
package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout // <-- Ensure this import is present and correct
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import coil.compose.AsyncImage // <-- Ensure this import is present
import com.example.damprojectfinal.core.dto.normalUser.UserProfile

// THIS IS THE CRUCIAL LINE for the "UnstableApi" warnings within ReelItem
@OptIn(UnstableApi::class)
@Composable
fun ReelItem(
    reelPost: PostResponse,
    isCurrentItem: Boolean, // Indicates if this reel is currently in full view
    onReelClick: (String) -> Unit // Callback for clicks on the reel (e.g., pause/play)
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(reelPost.mediaUrls.firstOrNull() ?: ""))
            prepare()
            volume = 0f // Start muted, user can unmute later
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop the video
        }
    }

    // Effect to control playback based on `isCurrentItem`
    DisposableEffect(isCurrentItem) {
        if (isCurrentItem) {
            exoPlayer.playWhenReady = true // Autoplay when current
            exoPlayer.play()
            // Optional: Call incrementReelView API here via ViewModel
        } else {
            exoPlayer.pause() // Pause when not current
        }
        onDispose {
            // When composable leaves composition (e.g., scrolled away), ensure player is paused
            exoPlayer.pause()
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
            .clickable { onReelClick(reelPost._id) } // Handle reel click (e.g., pause/play toggle)
    ) {
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
                    model = "https://via.placeholder.com/40/0000FF/FFFFFF?text=P", // Placeholder profile pic
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape) // Add CircleShape for profile pic
                        .clickable { /* Handle profile click */ }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "FoodieUser123", // Placeholder username
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
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder, // Use filled for liked state
                contentDescription = "Like",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { /* Handle like click */ }
            )
            Text(text = "0", color = Color.White, fontSize = 12.sp) // Placeholder count

            // Comment
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { /* Handle comment click */ }
            )
            Text(text = "0", color = Color.White, fontSize = 12.sp) // Placeholder count

            // Share
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { /* Handle share click */ }
            )

            // Save
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder, // Use filled for saved state
                contentDescription = "Save",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { /* Handle save click */ }
            )

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