package com.example.damprojectfinal.professional.feature_posts

// File: com.example.damprojectfinal.user.feature_posts.ui.components/PostGrid.kt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.posts.PostResponse // Import your PostResponse
import com.example.damprojectfinal.core.dto.normalUser.UserProfile // Import UserProfile for dummy data if ownerId uses it
import com.example.damprojectfinal.user.feature_posts.ui.post_management.AppMediaType // Import your AppMediaType
import com.example.damprojectfinal.core.dto.posts.PostOwnerDetails // NEW IMPORT for dummy data


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostGrid(
    posts: List<PostResponse>,
    onPostClick: (String) -> Unit // Callback when a post is clicked
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Provide a height for the empty state
                .background(MaterialTheme.colorScheme.surface), // White background
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No posts found.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columns for Instagram-like grid
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = Dp.Infinity) // Ensure a minimum height but can expand
            .background(MaterialTheme.colorScheme.surface), // White background
        horizontalArrangement = Arrangement.spacedBy(1.dp), // Thin space between grid items
        verticalArrangement = Arrangement.spacedBy(1.dp), // Thin space between grid items
        userScrollEnabled = false // Prevent grid from scrolling independently within LazyColumn
    ) {
        items(posts, key = { it._id }) { post ->
            // Use the first media URL for display
            val imageUrl = if (post.mediaType == AppMediaType.REEL.value) {
                post.thumbnailUrl ?: post.mediaUrls.firstOrNull() // Use thumbnail for reels if available
            } else {
                post.mediaUrls.firstOrNull()
            }

            if (imageUrl != null) {
                Card(
                    modifier = Modifier
                        .aspectRatio(1f) // Square aspect ratio
                        .clickable { onPostClick(post._id) },
                    elevation = CardDefaults.cardElevation(0.dp), // No shadow
                    shape = MaterialTheme.shapes.small // No rounded corners for grid items
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Placeholder for posts without media URL
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Media", color = Color.Gray)
                }
            }
        }
    }
}

//// Preview for PostGrid (with dummy data)
//@Preview(showBackground = true)
//@Composable
//fun PreviewPostGrid() {
//    val dummyPosts = listOf(
//        PostResponse(
//            _id = "1", caption = "Photo 1", mediaUrls = listOf("https://picsum.photos/200/300"), mediaType = AppMediaType.IMAGE.value,
//            createdAt = "", updatedAt = "", version = 0, ownerId = UserProfile(_id = "user1", username = "user1", email = "a@b.com", followerCount = 0, followingCount = 0), ownerModel = "User", likeCount = 0, commentCount = 0, saveCount = 0
//        ),
//        PostResponse(
//            _id = "2", caption = "Reel 1", mediaUrls = listOf("https://picsum.photos/id/237/200/300"), mediaType = AppMediaType.REEL.value,
//            thumbnailUrl = "https://picsum.photos/id/237/200/300",
//            createdAt = "", updatedAt = "", version = 0, ownerId = UserProfile(_id = "user1", username = "user1", email = "a@b.com", followerCount = 0, followingCount = 0), ownerModel = "User", likeCount = 0, commentCount = 0, saveCount = 0
//        ),
//        PostResponse(
//            _id = "3", caption = "Photo 2", mediaUrls = listOf("https://picsum.photos/240/300"), mediaType = AppMediaType.IMAGE.value,
//            createdAt = "", updatedAt = "", version = 0, ownerId = UserProfile(_id = "user1", username = "user1", email = "a@b.com", followerCount = 0, followingCount = 0), ownerModel = "User", likeCount = 0, commentCount = 0, saveCount = 0
//        ),
//        PostResponse(
//            _id = "4", caption = "Photo 3", mediaUrls = listOf("https://picsum.photos/200/340"), mediaType = AppMediaType.IMAGE.value,
//            createdAt = "", updatedAt = "", version = 0, ownerId = UserProfile(_id = "user1", username = "user1", email = "a@b.com", followerCount = 0, followingCount = 0), ownerModel = "User", likeCount = 0, commentCount = 0, saveCount = 0
//        ),
//        PostResponse(
//            _id = "5", caption = "Reel 2", mediaUrls = listOf("https://picsum.photos/id/238/200/300"), mediaType = AppMediaType.REEL.value,
//            thumbnailUrl = "https://picsum.photos/id/238/200/300",
//            createdAt = "", updatedAt = "", version = 0, ownerId = UserProfile(_id = "user1", username = "user1", email = "a@b.com", followerCount = 0, followingCount = 0), ownerModel = "User", likeCount = 0, commentCount = 0, saveCount = 0
//        )
//    )
//    MaterialTheme {
//        PostGrid(posts = dummyPosts, onPostClick = {})
//    }
//}
