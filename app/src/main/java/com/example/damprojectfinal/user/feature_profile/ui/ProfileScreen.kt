package com.example.damprojectfinal.user.feature_profile.ui

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
import androidx.compose.ui.graphics.vector.rememberVectorPainter // <-- Ensure this is imported for placeholder
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.normalUser.ProfileUiState
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.example.damprojectfinal.core.dto.posts.PostResponse
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBack // <-- NEW IMPORT
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.UserRoutes
import androidx.compose.runtime.setValue

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
                ProfileHeader(userProfile = userProfile, viewModel = viewModel)
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
fun ProfileHeader(
    userProfile: UserProfile,
    viewModel: UserViewModel = viewModel()
) {
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

        // Show Edit Profile button if viewing own profile, otherwise show Follow/Unfollow
        val context = LocalContext.current
        val currentUserId = remember { TokenManager(context).getUserId() }
        val isOwnProfile = currentUserId == userProfile._id

        if (isOwnProfile) {
            Button(
                onClick = { /* TODO: Navigate to Edit Profile screen */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Edit Profile", fontSize = 16.sp)
            }
        } else {
            // Follow/Unfollow button
            var isFollowing by remember { mutableStateOf(false) }

            // Reset following state when userProfile changes
            LaunchedEffect(userProfile._id) {
                isFollowing = false // Reset to false when viewing a different profile
            }

            Button(
                onClick = {
                    isFollowing = !isFollowing
                    if (isFollowing) {
                        viewModel.followUser(userProfile._id)
                    } else {
                        viewModel.unfollowUser(userProfile._id)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = if (isFollowing) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                } else {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                }
            ) {
                Text(if (isFollowing) "Unfollow" else "Follow", fontSize = 16.sp)
            }
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
