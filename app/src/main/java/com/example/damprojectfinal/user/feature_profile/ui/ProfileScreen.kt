// src/main/java/com/example/damprojectfinal/feature_profile.ui/ProfileScreen.kt
package com.example.damprojectfinal.feature_profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // <-- NEW IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells // <-- NEW IMPORT
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // <-- NEW IMPORT
import androidx.compose.foundation.lazy.grid.items // <-- NEW IMPORT
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person // <-- Ensure this is imported for placeholder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.* // Using Material 3 for Composables
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter // <-- Ensure this is imported for placeholder
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.R // Assuming you have a default profile pic drawable
import com.example.damprojectfinal.core.dto.normalUser.ProfileUiState
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme // Assuming your app's theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBack // <-- NEW IMPORT
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.UserRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController, // If you plan to use NavController for navigation
    viewModel: UserViewModel = viewModel() // ViewModel provided by Hilt or default
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userId = remember { TokenManager(context).getUserId() ?: "" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.userProfile?.username ?: "Profile") },
                navigationIcon = { // <-- NEW: Back button
                    IconButton(onClick = { navController.popBackStack() }) { // <-- Navigate back
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                // No back button directly on the profile as per your last clarification
                // actions = { /* Settings or other actions could go here */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoadingProfile || uiState.isLoadingPosts) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            uiState.userProfile?.let { userProfile ->
                ProfileHeader(userProfile = userProfile)
            } ?: run {
                if (!uiState.isLoadingProfile && uiState.errorMessage == null) {
                    // Show a message or a skeleton loader if profile data is still loading
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() // Or a more elaborate skeleton
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp)) // Spacer between header and tabs
            if (uiState.userProfile != null) { // Only show tabs if profile is loaded
                ProfileTabs(uiState = uiState, onTabSelected = { viewModel.onTabSelected(it) })
            }

            // Content of the selected tab
            when (uiState.selectedTabIndex) {
                0 -> PostsGrid(uiState.userPosts, userId, navController, isSavedPosts = false) // For "Posts" tab
                1 -> {
                    if (uiState.isLoadingSavedPosts) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        PostsGrid(uiState.savedPosts, userId, navController, isSavedPosts = true) // For "Saved" tab
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(96.dp)) {
                AsyncImage(
                    model = userProfile.profilePictureUrl,
                    contentDescription = "Profile picture",
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile picture",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp) // Offset to position correctly
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { /* TODO: Handle share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Profile")
                }
                IconButton(onClick = { /* TODO: Handle settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userProfile.fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userProfile.bio,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(count = userProfile.postCount, label = "Posts")
            ProfileStat(count = userProfile.followerCount, label = "Followers")
            ProfileStat(count = userProfile.followingCount, label = "Following")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Navigate to Edit Profile screen */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Edit Profile", fontSize = 16.sp)
        }
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}

@Composable
fun ProfileTabs(uiState: ProfileUiState, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Posts", "Saved")
    TabRow(
        selectedTabIndex = uiState.selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = uiState.selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(title) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For LazyVerticalGrid
@Composable
fun PostsGrid(
    posts: List<PostResponse>,
    userId: String,
    navController: NavController,
    isSavedPosts: Boolean = false
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSavedPosts) "No saved posts yet." else "No posts yet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3 columns for the grid
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(2.dp), // Small padding around the grid
            verticalArrangement = Arrangement.spacedBy(2.dp), // Space between rows
            horizontalArrangement = Arrangement.spacedBy(2.dp) // Space between columns
        ) {
            items(posts) { post ->
                // Use the first media URL for the grid item
                val imageUrl = post.mediaUrls.firstOrNull()

                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Ensures items are square
                        .clickable {
                            // Navigate to appropriate screen based on tab
                            if (isSavedPosts) {
                                navController.navigate("${UserRoutes.ALL_SAVED_POSTS}/$userId")
                            } else {
                                navController.navigate("${UserRoutes.ALL_PROFILE_POSTS}/$userId")
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        // Placeholder/Error for individual post images
                        placeholder = rememberVectorPainter(Icons.Default.Image), // Using a generic image icon
                        error = rememberVectorPainter(Icons.Default.BrokenImage) // Broken image icon for errors
                    )
                    // You might want to overlay an icon for video posts here
                    // e.g., if (post.mediaType == "reel") { Icon(Icons.Default.PlayArrow, ...) }
                }
            }
        }
    }
}

@Composable
fun SavedPostsContent() {
    // Placeholder for saved posts content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Saved Posts Content (Coming Soon)", style = MaterialTheme.typography.bodyLarge)
    }
}


// --- PREVIEW ---
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    DamProjectFinalTheme() {
//        val dummyProfile = UserProfile(
//            _id = "123",
//            username = "mohamedali_foodie",
//            fullName = "Mohamed Ali",
//            bio = "Food enthusiast 🍕 | Exploring the best flavors 😋 | Love trying new restaurants ❤️",
//            profilePictureUrl = "https://picsum.photos/200/300", // Dummy URL
//            followerCount = 2400,
//            followingCount = 892,
//            postCount = 127,
//            phone = null, email = null, address = null, isActive = true
//        )
//        val dummyPosts = listOf(
//            PostResponse(
//                _id = "p1",
//                caption = "Delicious pizza",
//                mediaUrls = listOf("https://picsum.photos/id/10/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 10,
//                commentCount = 2,
//                saveCount = 5,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            ),
//            PostResponse(
//                _id = "p2",
//                caption = "Healthy bowl",
//                mediaUrls = listOf("https://picsum.photos/id/20/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 15,
//                commentCount = 3,
//                saveCount = 7,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            ),
//            PostResponse(
//                _id = "p3",
//                caption = "Pancakes",
//                mediaUrls = listOf("https://picsum.photos/id/30/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 12,
//                commentCount = 1,
//                saveCount = 6,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            ),
//            PostResponse(
//                _id = "p4",
//                caption = "Green Salad",
//                mediaUrls = listOf("https://picsum.photos/id/40/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 8,
//                commentCount = 0,
//                saveCount = 3,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            ),
//            PostResponse(
//                _id = "p5",
//                caption = "Raspberry Cake",
//                mediaUrls = listOf("https://picsum.photos/id/50/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 20,
//                commentCount = 5,
//                saveCount = 10,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            ),
//            PostResponse(
//                _id = "p6",
//                caption = "Grilled Skewers",
//                mediaUrls = listOf("https://picsum.photos/id/60/200/300"),
//                mediaType = "image",
//                createdAt = "",
//                updatedAt = "",
//                version = 1,
//                likeCount = 18,
//                commentCount = 4,
//                saveCount = 9,
//                userId = dummyProfile // <-- ADDED THE userId HERE
//            )
//        )
//        val dummyUiState = ProfileUiState(
//            userProfile = dummyProfile,
//            userPosts = dummyPosts,
//            isLoadingProfile = false,
//            isLoadingPosts = false,
//            errorMessage = null,
//            selectedTabIndex = 0
//        )
//
//        Column {
//            ProfileHeader(userProfile = dummyProfile)
//            Spacer(modifier = Modifier.height(16.dp))
//            ProfileTabs(uiState = dummyUiState, onTabSelected = {})
//            PostsGrid(dummyUiState.userPosts)
//        }
//    }
//}
