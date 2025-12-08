package com.example.damprojectfinal.professional.feature_profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.damprojectfinal.core.api.BaseUrlProvider
import android.util.Log
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.ProRoutes
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel

enum class ProfessionalProfileTab { ABOUT, REELS, PHOTOS }

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

    Log.d("ProfileScreen", "ProfessionalProfileScreen recomposed for ID: $professionalId")

    LaunchedEffect(professionalId) {
        Log.d("ProfileScreen", "LaunchedEffect triggered for ID: $professionalId")
        viewModel.loadProfessionalProfile(professionalId)
        viewModel.fetchProfessionalPosts(professionalId)
    }

    val profile by viewModel.profile.collectAsState()
    val selectedProfileImageUri by viewModel.selectedProfileImageUri.collectAsState()
    val photoPosts by viewModel.photoPosts.collectAsState()
    val reelPosts by viewModel.reelPosts.collectAsState()

    var selectedTab by remember { mutableStateOf(ProfessionalProfileTab.REELS) } // Default to Reels as in your screenshot

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            Log.d("ProfileScreen", "Gallery result: $uri")
            viewModel.setSelectedProfileImageUri(uri)
            if (uri != null) {
                // TODO: Initiate profile image upload (e.g., call viewModel.uploadProfileImage)
            }
        }
    )

    Scaffold(
        topBar = {
            Log.d("ProfileScreen", "Rendering TopAppBar")
            TopAppBar(
                title = { /* Empty title */ },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("ProfileScreen", "Back button clicked")
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { Log.d("ProfileScreen", "Bookmark button clicked") }) {
                        Icon(imageVector = Icons.Filled.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                    }
                    IconButton(onClick = { Log.d("ProfileScreen", "Share button clicked") }) {
                        Icon(imageVector = Icons.Filled.FileUpload, contentDescription = "Share", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Log.d("ProfileScreen", "Rendering LazyColumn with padding: $paddingValues")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // --- UPDATED: Profile Header Section with LARGE Profile Picture and Name ---
            item {
                Log.d("ProfileScreen", "Rendering Header item. Profile name: ${profile?.fullName}")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp) // Height for the top 1/3 section
                        .background(Color.Black) // Default background if no image
                        .clickable { // Making the entire top section clickable to pick image
                            Log.d("ProfileScreen", "Large profile area clicked, launching gallery.")
                            galleryLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.BottomStart // Align content (name) to bottom-start
                ) {
                    // Large Profile Image
                    val profileImageModel = selectedProfileImageUri ?: "https://picsum.photos/id/237/200/300" // Use selected URI first, then static URL
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileImageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Professional Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // Crop to fill the entire Box
                    )

                    // Overlay for TopAppBar padding to avoid content clash
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f))
                    )

                    // Business Name - pulled up over the white space
                    Text(
                        text = profile?.fullName ?: "Loading...",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp)
                            .offset(y = (-32).dp) // move text up; adjust value to match your design
                    )
                }
            }

            // --- Combined Business Information Section and Tabs (Continuous White Card) ---
            item {
                Log.d("ProfileScreen", "Rendering Combined Business Info and Tabs item. Profile null? ${profile == null}")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-32).dp) // Pull this section up to overlap the top section
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) // Rounded top corners
                        .background(MaterialTheme.colorScheme.surface) // White background for the entire card
                        .padding(bottom = paddingValues.calculateBottomPadding()) // Apply bottom padding for Scaffold
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        profile?.let { currentProfile ->
                            Log.d("ProfileScreen", "Profile data available for business info: ${currentProfile.fullName}")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rating: N/A",
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Price Range: N/A",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Cuisine: N/A",
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ServiceOptionCard(
                                    icon = Icons.Filled.Info,
                                    title = "Info",
                                    subtitle = "View Details",
                                    color = Color(0xFFFFE0B2)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Show Follow/Unfollow or View Profile Details based on whether viewing own profile
                            val currentUserId = TokenManager(context).getUserId()
                            val isOwnProfile = currentUserId == professionalId

                            if (isOwnProfile) {
                                Button(
                                    onClick = { Log.d("ProfileScreen", "View Profile Details clicked") },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "View Profile Details",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White, modifier = Modifier.padding(start = 8.dp))
                                }
                            } else {
                                var isFollowing by remember { mutableStateOf(false) } // TODO: Get actual follow state from backend

                                // Reset following state when professionalId changes
                                LaunchedEffect(professionalId) {
                                    isFollowing = false // Reset to false when viewing a different profile
                                }

                                Button(
                                    onClick = {
                                        isFollowing = !isFollowing
                                        if (isFollowing) {
                                            viewModel.followProfessional(professionalId)
                                        } else {
                                            viewModel.unfollowProfessional(professionalId)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color(0xFF757575) else Color(0xFFFF5722)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isFollowing) "Unfollow" else "Follow",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            ContactInfoRow(
                                icon = Icons.Filled.Email,
                                text = currentProfile.email,
                                iconTint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Spacer(modifier = Modifier.height(24.dp))
                        } ?: run {
                            Log.d("ProfileScreen", "Profile data not yet available, showing progress indicator in business info.")
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            }
                        }
                    }

                    Log.d("ProfileScreen", "Rendering Tabs section. Current tab: $selectedTab")
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color.Black,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = Color(0xFFFF9800)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == ProfessionalProfileTab.ABOUT,
                            onClick = {
                                Log.d("ProfileScreen", "About tab clicked")
                                selectedTab = ProfessionalProfileTab.ABOUT
                            },
                            text = { Text("About", color = if (selectedTab == ProfessionalProfileTab.ABOUT) Color.Black else Color.Gray) }
                        )
                        Tab(
                            selected = selectedTab == ProfessionalProfileTab.REELS,
                            onClick = {
                                Log.d("ProfileScreen", "Reels tab clicked")
                                selectedTab = ProfessionalProfileTab.REELS
                            },
                            text = { Text("Reels", color = if (selectedTab == ProfessionalProfileTab.REELS) Color.Black else Color.Gray) }
                        )
                        Tab(
                            selected = selectedTab == ProfessionalProfileTab.PHOTOS,
                            onClick = {
                                Log.d("ProfileScreen", "Photos tab clicked")
                                selectedTab = ProfessionalProfileTab.PHOTOS
                            },
                            text = { Text("Photos", color = if (selectedTab == ProfessionalProfileTab.PHOTOS) Color.Black else Color.Gray) }
                        )
                    }

                    // Placeholder for tab content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Placeholder height for content
                            .background(MaterialTheme.colorScheme.surface) // White background for content area
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedTab) {
                            ProfessionalProfileTab.ABOUT -> {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "About ${profile?.fullName ?: "this professional"}:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            ProfessionalProfileTab.REELS -> {
                                if (reelPosts.isNotEmpty()) {
                                    PostsGrid(reelPosts, professionalId, navController)
                                } else {
                                    Text("No Reels available.", color = Color.Gray)
                                }
                            }
                            ProfessionalProfileTab.PHOTOS -> {
                                if (photoPosts.isNotEmpty()) {
                                    PostsGrid(photoPosts, professionalId, navController)
                                } else {
                                    Text("No Photos available.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- Helper Composables (No changes) ---

@Composable
fun ServiceOptionCard(icon: ImageVector, title: String, subtitle: String, color: Color) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = title, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ContactInfoRow(icon: ImageVector, text: String, iconTint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For LazyVerticalGrid
@Composable
fun PostsGrid(
    posts: List<PostResponse>,
    professionalId: String,
    navController: NavHostController
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No posts yet.", style = MaterialTheme.typography.bodyLarge)
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
                val imageUrl = BaseUrlProvider.getFullImageUrl(post.mediaUrls.firstOrNull())

                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // Ensures items are square
                        .clickable {
                            // Navigate to AllProfilePosts screen
                            navController.navigate("${ProRoutes.ALL_PROFILE_POSTS}/$professionalId")
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
