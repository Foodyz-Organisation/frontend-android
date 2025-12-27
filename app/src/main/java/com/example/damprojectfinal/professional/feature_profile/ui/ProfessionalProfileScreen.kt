package com.example.damprojectfinal.professional.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.damprojectfinal.core.api.BaseUrlProvider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.ProRoutes
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel

enum class ProfessionalProfileTab { REELS, PHOTOS }

// Yellow accent color matching the app theme
val ProfessionalYellow = Color(0xFFF59E0B)
val ProfessionalYellowLight = Color(0xFFFFF9E6)
val ProfessionalYellowDark = Color(0xFFD97706)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    navController: NavHostController,
    viewModel: ProfessionalProfileViewModel = viewModel(
        factory = ProfessionalProfileViewModel.Factory(
            tokenManager = TokenManager(LocalContext.current),
            professionalApiService = RetrofitClient.professionalApiService,
            postsApiService = RetrofitClient.postsApiService
        )
    )
) {
    val professionalId = TokenManager(LocalContext.current).getUserId() ?: "unknown"
    val context = LocalContext.current

    // Load profile on first launch
    LaunchedEffect(professionalId) {
        viewModel.loadProfessionalProfile(professionalId)
        viewModel.fetchProfessionalPosts(professionalId)
    }

    // Reload profile when returning from settings screen
    LaunchedEffect(navController.currentBackStackEntry) {
        val route = navController.currentBackStackEntry?.destination?.route
        // Check if route matches the professional profile screen (with or without professionalId parameter)
        if (route?.startsWith("professional_profile_screen") == true) {
            // Reload profile when returning to this screen
            viewModel.loadProfessionalProfile(professionalId)
        }
    }

    val profile by viewModel.profile.collectAsState()
    val selectedProfileImageUri by viewModel.selectedProfileImageUri.collectAsState()
    val photoPosts by viewModel.photoPosts.collectAsState()
    val reelPosts by viewModel.reelPosts.collectAsState()

    var selectedTab by remember { mutableStateOf(ProfessionalProfileTab.REELS) }

    Scaffold(
        containerColor = Color.White
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
                        .height(260.dp) // slightly shorter header, like user side
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

                    // Gradient Overlay for better text readability
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

                    // Top App Bar with Back Button and Settings Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Back Button on the left
                        IconButton(
                            onClick = { 
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Settings Button on the right
                        IconButton(
                            onClick = { 
                                navController.navigate("professional_profile_management/${professionalId}")
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }

                    // Profile Picture - centered like user profile (on the white card)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 140.dp), // similar to user profile (130dp)
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
                            // Build a fallback avatar name for placeholder
                            val avatarName = profile?.fullName?.replace(" ", "+") ?: "Professional"
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

                    // Business Name at bottom of header
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
                        .padding(top = 80.dp) // space for overlapping profile picture (aligned like user)
                ) {
                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = profile?.followerCount?.toString() ?: "0",
                            label = "Followers",
                            icon = Icons.Filled.People
                        )
                        StatItem(
                            value = profile?.followingCount?.toString() ?: "0",
                            label = "Following",
                            icon = Icons.Filled.PersonAdd
                        )
                        StatItem(
                            value = (photoPosts.size + reelPosts.size).toString(),
                            label = "Posts",
                            icon = Icons.Filled.PhotoCamera
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

                    // Description Section - Right after stats
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // Description/Bio - Always show, even if empty
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
                                InfoRow(
                                    icon = Icons.Filled.Phone,
                                    text = phone,
                                    iconColor = ProfessionalYellow
                                )
                            }
                        }

                        // Locations (multiple supported)
                        if (!profile?.locations.isNullOrEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                profile?.locations?.forEach { location ->
                                    // Backend stores address in the "name" field
                                    val displayText = location.name 
                                        ?: location.address 
                                        ?: "Lat: ${location.lat}, Lng: ${location.lon}"
                                    
                                    InfoRow(
                                        icon = Icons.Filled.LocationOn,
                                        text = displayText,
                                        iconColor = ProfessionalYellow
                                    )
                                }
                            }
                        } else if (!profile?.address.isNullOrBlank()) {
                            // Fallback to old single address field if locations array is empty
                            InfoRow(
                                icon = Icons.Filled.LocationOn,
                                text = profile?.address ?: "",
                                iconColor = ProfessionalYellow
                            )
                        } else {
                            // Show placeholder if no addresses
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
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        // Hours
                        profile?.hours?.let { hours ->
                            if (hours.isNotBlank()) {
                                InfoRow(
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
                                        ServiceChip("Delivery", Icons.Filled.DeliveryDining)
                                    }
                                    if (services.takeaway) {
                                        ServiceChip("Takeaway", Icons.Filled.Restaurant)
                                    }
                                    if (services.dineIn) {
                                        ServiceChip("Dine In", Icons.Filled.RestaurantMenu)
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
                            selected = selectedTab == ProfessionalProfileTab.REELS,
                            onClick = { selectedTab = ProfessionalProfileTab.REELS },
                            text = {
                                Text(
                                    "Reels",
                                    fontWeight = if (selectedTab == ProfessionalProfileTab.REELS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == ProfessionalProfileTab.REELS) ProfessionalYellow else Color(0xFF6B7280)
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == ProfessionalProfileTab.PHOTOS,
                            onClick = { selectedTab = ProfessionalProfileTab.PHOTOS },
                            text = {
                                Text(
                                    "Photos",
                                    fontWeight = if (selectedTab == ProfessionalProfileTab.PHOTOS) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == ProfessionalProfileTab.PHOTOS) ProfessionalYellow else Color(0xFF6B7280)
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
                            ProfessionalProfileTab.REELS -> {
                                if (reelPosts.isNotEmpty()) {
                                    PostsGrid(reelPosts, professionalId, navController)
                                } else {
                                    EmptyState(
                                        icon = Icons.Filled.VideoLibrary,
                                        message = "No Reels available"
                                    )
                                }
                            }
                            ProfessionalProfileTab.PHOTOS -> {
                                if (photoPosts.isNotEmpty()) {
                                    PostsGrid(photoPosts, professionalId, navController)
                                } else {
                                    EmptyState(
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

@Composable
fun StatItem(value: String, label: String, icon: ImageVector) {
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
fun InfoRow(icon: ImageVector, text: String, iconColor: Color) {
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
fun ServiceChip(label: String, icon: ImageVector) {
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
fun AboutTabContent(profile: com.example.damprojectfinal.core.dto.professionalUser.ProfessionalUserAccount?) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (profile?.description.isNullOrBlank()) {
                EmptyState(
                    icon = Icons.Filled.Info,
                    message = "No description available"
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "About ${profile?.fullName ?: "this business"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = profile?.description ?: "",
                        fontSize = 15.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Additional info cards
        item {
            if (!profile?.address.isNullOrBlank() || !profile?.phone.isNullOrBlank() || !profile?.hours.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProfessionalYellowLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Contact Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        profile?.phone?.let { phone ->
                            if (phone.isNotBlank()) {
                                InfoRow(Icons.Filled.Phone, phone, ProfessionalYellow)
                            }
                        }
                        profile?.address?.let { address ->
                            if (address.isNotBlank()) {
                                InfoRow(Icons.Filled.LocationOn, address, ProfessionalYellow)
                            }
                        }
                        profile?.hours?.let { hours ->
                            if (hours.isNotBlank()) {
                                InfoRow(Icons.Filled.Schedule, hours, ProfessionalYellow)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsGrid(
    posts: List<PostResponse>,
    professionalId: String,
    navController: NavHostController
) {
    if (posts.isEmpty()) {
        EmptyState(
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
                            navController.navigate("${ProRoutes.ALL_PROFILE_POSTS}/$professionalId")
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
