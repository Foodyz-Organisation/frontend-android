package com.example.damprojectfinal.user.feature_pro_profile.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel
import com.example.damprojectfinal.user.feature_follow.viewmodel.FollowViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// --- Design Colors (Matching Professional Profile) ---
val ProfessionalYellow = Color(0xFFF59E0B)
val ProfessionalYellowLight = Color(0xFFFFF9E6)
val ProfessionalYellowDark = Color(0xFFD97706)

// Tab enum for Reels/Photos
enum class UserViewProfileTab { REELS, PHOTOS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileView(
    professionalId: String,
    navController: NavController,
    viewModel: ProfessionalProfileViewModel = viewModel(
        factory = ProfessionalProfileViewModel.Factory(
            tokenManager = TokenManager(LocalContext.current),
            professionalApiService = RetrofitClient.professionalApiService,
            postsApiService = RetrofitClient.postsApiService
        )
    )
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val followViewModel = remember { FollowViewModel(RetrofitClient.followApiService, tokenManager) }
    val currentUserId = remember { tokenManager.getUserId() }

    // Load profile on first launch
    LaunchedEffect(professionalId) {
        Log.d("UserViewProfile", "Loading professional profile: $professionalId")
        viewModel.loadProfessionalProfile(professionalId)
        viewModel.fetchProfessionalPosts(professionalId)
        // Check following status only if not viewing own profile
        if (currentUserId != professionalId) {
            followViewModel.checkFollowingStatus(professionalId, "ProfessionalAccount")
        }
    }

    val profile by viewModel.profile.collectAsState()
    val photoPosts by viewModel.photoPosts.collectAsState()
    val reelPosts by viewModel.reelPosts.collectAsState()
    
    // Follow functionality - only show if not viewing own profile
    val isViewingOwnProfile = currentUserId == professionalId
    val followingStatus by followViewModel.followingStatus.collectAsState()
    val isLoadingFollow by followViewModel.loadingStates.collectAsState()
    val isFollowing = followingStatus[professionalId] ?: false
    val isFollowLoading = isLoadingFollow[professionalId] ?: false
    
    // Refresh profile when follow status changes to update follower count
    LaunchedEffect(isFollowing) {
        if (!isViewingOwnProfile) {
            viewModel.loadProfessionalProfile(professionalId)
        }
    }

    var selectedTab by remember { mutableStateOf(UserViewProfileTab.REELS) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            // Bottom Bar: View Menu & Order Button
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = {
                        Log.d("UserViewProfile", "Navigating to menu: $professionalId")
                        navController.navigate("menu_order_route/$professionalId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfessionalYellow,
                        contentColor = Color.White
                    )
                ) {
                    Text("View Menu & Order", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ========== HEADER SECTION WITH BACKGROUND IMAGE ==========
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    // Background Image
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(
                                profile?.imageUrl?.let { BaseUrlProvider.getFullImageUrl(it) }
                                    ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800"
                            )
                            .crossfade(true)
                            .build(),
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // Top App Bar with Back Button and Share Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Back Button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = { /* TODO: Share */ },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }

                    // Profile Picture - centered
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 140.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color.White, CircleShape)
                                .border(4.dp, ProfessionalYellow, CircleShape)
                                .padding(4.dp)
                        ) {
                            val avatarName = profile?.fullName?.replace(" ", "+") ?: "Restaurant"
                            val imageModel = profile?.profilePictureUrl
                                ?.let { BaseUrlProvider.getFullImageUrl(it) }
                                ?: "https://ui-avatars.com/api/?name=$avatarName&background=F59E0B&color=fff&size=200"

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Business Name at bottom
                    Text(
                        text = profile?.fullName ?: "Loading...",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp, bottom = 16.dp)
                    )
                }
            }

            // ========== PROFILE CONTENT SECTION ==========
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(top = 80.dp)
                ) {
                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        UserViewStatItem(
                            value = profile?.followerCount?.toString() ?: "0",
                            label = "Followers",
                            icon = Icons.Filled.People
                        )
                        UserViewStatItem(
                            value = profile?.followingCount?.toString() ?: "0",
                            label = "Following",
                            icon = Icons.Filled.PersonAdd
                        )
                        UserViewStatItem(
                            value = (photoPosts.size + reelPosts.size).toString(),
                            label = "Posts",
                            icon = Icons.Filled.PhotoCamera
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                    // Follow Button (only show if not viewing own profile)
                    if (!isViewingOwnProfile) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    followViewModel.toggleFollow(professionalId, "ProfessionalAccount")
                                },
                                enabled = !isFollowLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) Color.Gray else ProfessionalYellow,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isFollowing) "Following" else "Follow",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    }

                    // Description Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        if (!profile?.description.isNullOrBlank()) {
                            Text(
                                text = profile?.description ?: "",
                                fontSize = 15.sp,
                                color = Color(0xFF4B5563),
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        } else {
                            Text(
                                text = "No description available",
                                fontSize = 15.sp,
                                color = Color(0xFF9CA3AF),
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                    // Contact & Info Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Phone
                        profile?.phone?.let { phone ->
                            if (phone.isNotBlank()) {
                                UserViewInfoRow(
                                    icon = Icons.Filled.Phone,
                                    text = phone,
                                    iconColor = ProfessionalYellow
                                )
                            }
                        }

                        // Locations (multiple supported)
                        // Debug logging with SideEffect to ensure it runs on every composition
                        SideEffect {
                            android.util.Log.d("RestaurantProfile", "=== LOCATIONS DEBUG (SideEffect) ===")
                            android.util.Log.d("RestaurantProfile", "Profile: ${profile?.fullName}")
                            android.util.Log.d("RestaurantProfile", "Locations: ${profile?.locations}")
                            android.util.Log.d("RestaurantProfile", "Locations count: ${profile?.locations?.size}")
                            android.util.Log.d("RestaurantProfile", "Is null or empty: ${profile?.locations.isNullOrEmpty()}")
                        }
                        
                        if (!profile?.locations.isNullOrEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                profile?.locations?.forEachIndexed { index, location ->
                                    android.util.Log.d("RestaurantProfile", "Rendering location $index: name=${location.name}, address=${location.address}")
                                    
                                    // The backend stores address in the "name" field
                                    val displayText = location.name 
                                        ?: location.address 
                                        ?: "Lat: ${location.lat}, Lng: ${location.lon}"
                                    
                                    UserViewInfoRow(
                                        icon = Icons.Filled.LocationOn,
                                        text = displayText,
                                        iconColor = ProfessionalYellow
                                    )
                                }
                            }
                        } else if (!profile?.address.isNullOrBlank()) {
                            // Fallback to old single address field if locations array is empty
                            android.util.Log.d("RestaurantProfile", "Using fallback address: ${profile?.address}")
                            UserViewInfoRow(
                                icon = Icons.Filled.LocationOn,
                                text = profile?.address ?: "",
                                iconColor = ProfessionalYellow
                            )
                        } else {
                            android.util.Log.w("RestaurantProfile", "No locations or address found - showing 'No address provided'")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "No address provided",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }

                        // Hours
                        profile?.hours?.let { hours ->
                            if (hours.isNotBlank()) {
                                UserViewInfoRow(
                                    icon = Icons.Filled.Schedule,
                                    text = hours,
                                    iconColor = ProfessionalYellow
                                )
                            }
                        }
                    }

                    // Services Section
                    profile?.services?.let { services ->
                        if (services.delivery || services.takeaway || services.dineIn) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (services.delivery) {
                                        UserViewServiceChip("Delivery", Icons.Filled.DeliveryDining)
                                    }
                                    if (services.takeaway) {
                                        UserViewServiceChip("Takeaway", Icons.Filled.Restaurant)
                                    }
                                    if (services.dineIn) {
                                        UserViewServiceChip("Dine In", Icons.Filled.RestaurantMenu)
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                    // ========== TABS SECTION ==========
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.White,
                        contentColor = ProfessionalYellow,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = ProfessionalYellow,
                                height = 3.dp
                            )
                        },
                        divider = { }
                    ) {
                        Tab(
                            selected = selectedTab == UserViewProfileTab.REELS,
                            onClick = { selectedTab = UserViewProfileTab.REELS },
                            text = {
                                Text(
                                    "Reels",
                                    fontWeight = if (selectedTab == UserViewProfileTab.REELS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == UserViewProfileTab.REELS) ProfessionalYellow else Color(0xFF6B7280)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == UserViewProfileTab.PHOTOS,
                            onClick = { selectedTab = UserViewProfileTab.PHOTOS },
                            text = {
                                Text(
                                    "Photos",
                                    fontWeight = if (selectedTab == UserViewProfileTab.PHOTOS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == UserViewProfileTab.PHOTOS) ProfessionalYellow else Color(0xFF6B7280)
                                )
                            }
                        )
                    }

                    // ========== TAB CONTENT ==========
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        when (selectedTab) {
                            UserViewProfileTab.REELS -> {
                                if (reelPosts.isNotEmpty()) {
                                    UserViewPostsGrid(reelPosts, navController)
                                } else {
                                    UserViewEmptyState(
                                        icon = Icons.Filled.VideoLibrary,
                                        message = "No Reels available"
                                    )
                                }
                            }
                            UserViewProfileTab.PHOTOS -> {
                                if (photoPosts.isNotEmpty()) {
                                    UserViewPostsGrid(photoPosts, navController)
                                } else {
                                    UserViewEmptyState(
                                        icon = Icons.Filled.PhotoCamera,
                                        message = "No Photos available"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// ========== HELPER COMPOSABLES ==========

@Composable
fun UserViewStatItem(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = ProfessionalYellow,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun UserViewInfoRow(icon: ImageVector, text: String, iconColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun UserViewServiceChip(label: String, icon: ImageVector) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ProfessionalYellow, RoundedCornerShape(20.dp)),
        color = ProfessionalYellowLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ProfessionalYellow,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ProfessionalYellowDark
            )
        }
    }
}

@Composable
fun UserViewEmptyState(icon: ImageVector, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UserViewPostsGrid(
    posts: List<PostResponse>,
    navController: NavController
) {
    if (posts.isEmpty()) {
        UserViewEmptyState(
            icon = Icons.Filled.PhotoCamera,
            message = "No posts yet"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(posts) { post ->
                // Use thumbnail for videos/reels, otherwise use first media URL
                val rawUrl = if (post.mediaType == "reel" && post.thumbnailUrl != null) {
                    post.thumbnailUrl
                } else {
                    post.mediaUrls.firstOrNull()
                }
                val imageUrl = BaseUrlProvider.getFullImageUrl(rawUrl)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            // TODO: Navigate to post detail if needed
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
                }
            }
        }
    }
}

// --- Wrapper Screen for Navigation ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileViewScreen(
    professionalId: String,
    navController: NavController
) {
    RestaurantProfileView(
        professionalId = professionalId,
        navController = navController
    )
}
