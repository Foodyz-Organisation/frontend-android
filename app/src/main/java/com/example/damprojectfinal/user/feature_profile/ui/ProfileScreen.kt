package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
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

// Professional color scheme
val CustomYellow = Color(0xFFFFC107) // Professional golden yellow
val ProfileAccent = Color(0xFF1F2937) // Dark gray for text
val ProfileSecondary = Color(0xFF6B7280) // Medium gray
val ProfileBackground = Color(0xFFFAFAFA) // Light background
val ProfileCard = Color(0xFFFFFFFF) // White cards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userId = remember { TokenManager(context).getUserId() ?: "" }
    
    // Track if we should refresh (when returning from settings)
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        // Refresh when we're on the profile view route (after returning from settings)
        if (currentRoute?.contains("profile_view") == true) {
            viewModel.refreshProfile()
        }
    }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .verticalScroll(scrollState)
    ) {
        // TopAppBar - now scrolls with content
        TopAppBar(
            title = {
                Text(
                    text = uiState.userProfile?.username ?: "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ProfileAccent
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileAccent
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navController.navigate(UserRoutes.PROFILE_SETTINGS)
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = ProfileAccent
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ProfileCard,
                titleContentColor = ProfileAccent
            )
        )

        // Loading indicator
        if (uiState.isLoadingProfile || uiState.isLoadingPosts) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = CustomYellow,
                trackColor = Color(0xFFE5E7EB)
            )
        }

        // Error message
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Profile content
        uiState.userProfile?.let { userProfile ->
            ProfileHeader(
                userProfile = userProfile,
                viewModel = viewModel,
                navController = navController
            )
            
            // Tabs
            ProfileTabs(
                uiState = uiState,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            // Content of the selected tab
            AnimatedContent(
                targetState = uiState.selectedTabIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
                }
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> PostsGrid(
                        posts = uiState.userPosts,
                        userId = userId,
                        navController = navController,
                        isSavedPosts = false
                    )
                    1 -> {
                        if (uiState.isLoadingSavedPosts) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CustomYellow)
                            }
                        } else {
                            PostsGrid(
                                posts = uiState.savedPosts,
                                userId = userId,
                                navController = navController,
                                isSavedPosts = true
                            )
                        }
                    }
                }
            }
        } ?: run {
            if (!uiState.isLoadingProfile && uiState.errorMessage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CustomYellow)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userProfile: UserProfile,
    viewModel: UserViewModel = viewModel(),
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with background (beige/cream gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Background gradient (beige/cream tones - matching image)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF5F1E8),
                                    Color(0xFFE8DCC6)
                                )
                            )
                        )
                )
            }

            // White content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ProfileCard)
                    .padding(top = 50.dp, bottom = 24.dp)
            ) {
                // Handle only (centered, no full name)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "@${userProfile.username ?: "username"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = ProfileSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row (centered with dividers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStat(
                        count = userProfile.postCount,
                        label = "Posts"
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Divider(
                        modifier = Modifier
                            .height(35.dp)
                            .width(1.dp),
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    ProfileStat(
                        count = userProfile.followerCount,
                        label = "Followers"
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Divider(
                        modifier = Modifier
                            .height(35.dp)
                            .width(1.dp),
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    ProfileStat(
                        count = userProfile.followingCount,
                        label = "Following"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                Button(
                    onClick = {
                        val userId = userProfile._id
                        navController.navigate(
                            UserRoutes.PROFILE_UPDATE.replace(
                                "{userId}",
                                userId
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomYellow
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
        
        // Profile picture positioned above the line (on top of both sections)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 130.dp), // Position at the boundary between beige and white
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ProfileCard)
                    .border(3.dp, CustomYellow, CircleShape)
            ) {
                AsyncImage(
                    model = BaseUrlProvider.getFullImageUrl(userProfile.profilePictureUrl),
                    contentDescription = "Profile picture",
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun ProfileStat(
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatCount(count),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = ProfileAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = ProfileSecondary
        )
    }
}

// Helper function to format large numbers
fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
fun ProfileTabs(uiState: ProfileUiState, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Posts", "Saved")
    
    TabRow(
        selectedTabIndex = uiState.selectedTabIndex,
        containerColor = ProfileCard,
        contentColor = CustomYellow,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[uiState.selectedTabIndex])
                    .fillMaxWidth(0.5f)
                    .height(3.dp),
                color = CustomYellow
            )
        },
        divider = {
            HorizontalDivider(
                color = Color(0xFFE5E7EB),
                thickness = 0.5.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = uiState.selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                },
                selectedContentColor = ProfileAccent,
                unselectedContentColor = ProfileSecondary
            )
        }
    }
}
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
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isSavedPosts) Icons.Outlined.BookmarkBorder else Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = ProfileSecondary
                )
                Text(
                    text = if (isSavedPosts) "No saved posts yet" else "No posts yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProfileSecondary
                )
                Text(
                    text = if (isSavedPosts) "Posts you save will appear here" else "Start sharing your moments",
                    fontSize = 14.sp,
                    color = ProfileSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Calculate height based on number of posts (3 columns)
        // Each item is roughly square, so we estimate based on screen width / 3
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val itemSizeValue = screenWidth / 3f
        val rows = (posts.size + 2) / 3 // Ceiling division
        val spacingValue = (rows - 1) * 1f // Spacing between rows
        val gridHeight = ((rows * itemSizeValue) + spacingValue).dp
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(gridHeight),
            contentPadding = PaddingValues(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(posts) { post ->
                // Use thumbnail for videos/reels, otherwise use first media URL
                val rawUrl = if (post.mediaType == "reel" && post.thumbnailUrl != null) {
                    post.thumbnailUrl
                } else {
                    post.mediaUrls.firstOrNull()
                }
                val imageUrl = BaseUrlProvider.getFullImageUrl(rawUrl)
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
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
                    
                    // Overlay for video/multiple images indicator
                    if (post.mediaUrls.size > 1 || post.mediaType == "video") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = if (post.mediaUrls.size > 1) Icons.Outlined.Collections else Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}