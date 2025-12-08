package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.normalUser.ProfileUiState
import com.example.damprojectfinal.core.dto.normalUser.UserProfile
import com.example.damprojectfinal.core.dto.posts.PostResponse
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Define the custom yellow color from the image for consistency
val CustomYellow = Color(0xFFE5B338) // Closely matching the button color in the image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userId = remember { TokenManager(context).getUserId() ?: "" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.userProfile?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Profile")
                    }
                    IconButton(onClick = { /* TODO: Handle settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
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
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Tabs should be directly under the header content without an extra spacer
            if (uiState.userProfile != null) {
                ProfileTabs(uiState = uiState, onTabSelected = { viewModel.onTabSelected(it) })
            }

            // Content of the selected tab
            when (uiState.selectedTabIndex) {
                0 -> PostsGrid(uiState.userPosts, userId, navController, isSavedPosts = false)
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
                        PostsGrid(uiState.savedPosts, userId, navController, isSavedPosts = true)
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
    // Determine if it's the current user's profile
    val context = LocalContext.current
    // NOTE: Current user ID logic is commented out as it's not needed without the buttons
    // val currentUserId = remember { TokenManager(context).getUserId() }
    // val isOwnProfile = currentUserId == userProfile._id

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted vertical padding
    ) {
        // 1. Profile Picture and Basic Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(3.dp, CustomYellow, CircleShape) // Yellow border
            ) {
                AsyncImage(
                    model = userProfile.profilePictureUrl,
                    contentDescription = "Profile picture",
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Bio Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = userProfile.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userProfile.bio,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 2 // Restrict bio length
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Stats Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(count = userProfile.postCount, label = "Posts")
            ProfileStat(count = userProfile.followerCount, label = "Followers")
            ProfileStat(count = userProfile.followingCount, label = "Following")
        }

        // Removed Spacer and Action Button block entirely
        Spacer(modifier = Modifier.height(16.dp)) // Maintain some space before tabs start
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Increased font size for count and bolded for emphasis
        Text(
            text = count.toString(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        // Kept label smaller
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileTabs(uiState: ProfileUiState, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Posts", "Saved")
    // Added bottom padding to the TabRow for separation from the grid
    TabRow(
        selectedTabIndex = uiState.selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 0.dp) // Adjusted padding
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = uiState.selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                // Use CustomYellow for indicator and selected text to match the design style
                selectedContentColor = CustomYellow,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsGrid(
    posts: List<PostResponse>,
    userId: String,
    navController: NavController,
    isSavedPosts: Boolean = false
) {
    // Existing PostsGrid remains functional and visually correct for a gallery view
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
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(posts) { post ->
                val imageUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull())

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            // Navigate to a detail view for the post
                            val route = if (isSavedPosts) UserRoutes.ALL_SAVED_POSTS else UserRoutes.ALL_PROFILE_POSTS
                            navController.navigate("$route/$userId")
                        }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = rememberVectorPainter(Icons.Default.Image),
                        error = rememberVectorPainter(Icons.Default.BrokenImage)
                    )
                    // Optional: Overlay an icon for video posts or multiple images
                }
            }
        }
    }
}