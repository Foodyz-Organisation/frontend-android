package com.example.damprojectfinal.user.common._component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.core.repository.ProfessionalRepository
import com.example.damprojectfinal.user.common.viewmodel.SearchViewModel
import io.ktor.client.HttpClient
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import coil.request.ImageRequest
import androidx.compose.ui.draw.rotate
import kotlinx.coroutines.delay

// --- Design Colors/Constants ---
private val PrimaryDark = Color(0xFF1F2A37) // Dark Gray for text/icons
private val YellowAccent = Color(0xFFFFC107)  // Primary Yellow (Used for accents/dot)
private val GrayInactive = Color(0xFF64748B) // Gray for inactive elements
private val LightBackground = Color.White    // General background

// Search Field Colors
private val YellowBorder = Color(0xFFFACC15) // Light yellow when focused
private val VeryPaleYellow = Color(0xFFFDE68A) // Very pale yellow when unfocused

@Composable
fun NotificationIconWithDot(
    onClick: () -> Unit,
    hasNew: Boolean,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = PrimaryDark,
                modifier = Modifier.size(iconSize)
            )
        }
        if (hasNew) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30)) // Red Dot
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 4.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// ANIMATED FLIP HEADER COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun AnimatedFlipHeader(
    onEventsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEvents by remember { mutableStateOf(false) }
    var rotationY by remember { mutableStateOf(0f) }
    var emojiOffsetFraction by remember { mutableStateOf(0f) }
    var showEmoji by remember { mutableStateOf(false) }
    var sweepDirection by remember { mutableStateOf(false) } // Start false, toggles to true first (left to right)
    
    // Animate rotation
    val animatedRotation by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )
    
    // Animate emoji position as a fraction (0 to 1)
    val animatedEmojiOffsetFraction by animateFloatAsState(
        targetValue = emojiOffsetFraction,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "emojiOffset"
    )
    
    // Auto-flip between Foodyz and Events with single emoji sweep animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Show current text for 3 seconds
            
            // Alternate sweep direction
            sweepDirection = !sweepDirection
            
            if (sweepDirection) {
                // LEFT to RIGHT: foodyz → Events
                // Set initial position
                emojiOffsetFraction = 0f
                delay(100) // Wait for position to initialize
                
                // Show emoji and start animation
                showEmoji = true
                delay(50)
                emojiOffsetFraction = 1f
                
                // Wait for emoji to reach center (600ms = 50% of 1200ms)
                delay(600)
                
                // Trigger flip animation exactly when emoji is at center
                rotationY = rotationY + 180f
                
                // Change text at midpoint of flip: foodyz → Events
                delay(300)
                showEvents = true
                
                // Wait for emoji to complete journey (300ms remaining of 1200ms total)
                delay(300)
                
            } else {
                // RIGHT to LEFT: Events → foodyz
                // Set initial position
                emojiOffsetFraction = 1f
                delay(100) // Wait for position to initialize
                
                // Show emoji and start animation
                showEmoji = true
                delay(50)
                emojiOffsetFraction = 0f
                
                // Wait for emoji to reach center (600ms = 50% of 1200ms)
                delay(600)
                
                // Trigger flip animation exactly when emoji is at center
                rotationY = rotationY + 180f
                
                // Change text at midpoint of flip: Events → foodyz
                delay(300)
                showEvents = false
                
                // Wait for emoji to complete journey (300ms remaining of 1200ms total)
                delay(300)
            }
            
            // Hide emoji
            showEmoji = false
            delay(100)
        }
    }
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxWidthPx: Float = with(density) {
            maxWidth.toPx()
        }
        
        // Calculate emoji position based on container width
        // Works for both directions because:
        // - LEFT to RIGHT: emojiOffsetFraction goes 0→1 (left edge to right edge)
        // - RIGHT to LEFT: emojiOffsetFraction goes 1→0 (right edge to left edge)
        val totalWidth: Float = maxWidthPx.plus(100f)
        val position: Float = animatedEmojiOffsetFraction.times(totalWidth)
        val emojiXOffset: Float = position.minus(100f)
        
        // Single emoji sweeping across the screen
        if (showEmoji) {
            Text(
                text = "🎊",
                fontSize = 32.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { androidx.compose.ui.unit.IntOffset(emojiXOffset.toInt(), 0) }
            )
        }
        
        // Center text with flip animation
        Box(
            modifier = Modifier
                .clickable(enabled = showEvents) {
                    if (showEvents) {
                        onEventsClick()
                    }
                }
                .graphicsLayer {
                    this.rotationY = animatedRotation
                    cameraDistance = 12f * density.density
                },
            contentAlignment = Alignment.Center
        ) {
            // Show the appropriate text based on rotation
            val isFlipped = (animatedRotation % 360) >= 90 && (animatedRotation % 360) < 270
            
            if (isFlipped) {
                // Show flipped content (Events or Foodyz depending on state)
                Box(
                    modifier = Modifier.graphicsLayer {
                        this.rotationY = 180f
                    }
                ) {
                    if (showEvents) {
                        Text(
                            text = " Events ",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color(0xFFFBBF24)
                            )
                        )
                    } else {
                        Text(
                            text = "foodyz",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color(0xFFFBBF24)
                            )
                        )
                    }
                }
            } else {
                // Show normal content (not flipped)
                if (showEvents) {
                    Text(
                        text = " Events",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color(0xFFFBBF24)
                        )
                    )
                } else {
                    Text(
                        text = "foodyz",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color(0xFFFBBF24)
                        )
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CUSTOM TOP BAR (FIXED PADDING)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String,
    openDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onReelsClick: () -> Unit,
    currentUserId: String,
    onLogoutClick: () -> Unit,
    profilePictureUrl: String? = null,
    hasUnreadNotifications: Boolean = false,
    hasUnreadMessages: Boolean = false,
    showNavBar: Boolean = true
) {
    var showAddOptions by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    
    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth > 600

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBackground) // Use clean white background
            // 🌟 FIX 1: Use windowInsetsPadding for status bars to replace hardcoded 48.dp
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = if (isSmallScreen) 8.dp else 12.dp,
                    horizontal = if (isSmallScreen) 8.dp else 14.dp
                )
        ) {
            // '+' button moved to left side
            IconButton(
                onClick = { navController.navigate(UserRoutes.CREATE_POST) },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Content",
                    tint = PrimaryDark,
                    modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                )
            }

            // App title (Center) - Animated Flip Header
            AnimatedFlipHeader(
                onEventsClick = {
                    navController.navigate(UserRoutes.EVENT_LIST_REMOTE)
                },
                modifier = Modifier.align(Alignment.Center)
            )

            // Right side icons
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Button
                IconButton(onClick = onSearchClick) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = PrimaryDark,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }

                // Drawer Button
                IconButton(onClick = openDrawer) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Drawer",
                        tint = PrimaryDark,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
            }
        }

        // Secondary Nav Bar - Back at the top
        if (showNavBar) {
            SecondaryNavBar(
                navController = navController,
                currentRoute = currentRoute,
                onReelsClick = onReelsClick,
                hasUnreadMessages = hasUnreadMessages,
                hasUnreadNotifications = hasUnreadNotifications
            )
        }

        // Add Options Popup (Unchanged)
        if (showAddOptions) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showAddOptions = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .wrapContentHeight()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Create New", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { /* Add Post */ }) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Post",
                                        tint = Color(0xFF2563EB)
                                    )
                                }
                                Text("Post", fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { /* Add Photo */ }) {
                                    Icon(
                                        Icons.Filled.PhotoCamera,
                                        contentDescription = "Photo",
                                        tint = Color(0xFF10B981)
                                    )
                                }
                                Text("Photo", fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { /* Add Video */ }) {
                                    Icon(
                                        Icons.Filled.Videocam,
                                        contentDescription = "Video",
                                        tint = Color(0xFFF59E0B)
                                    )
                                }
                                Text("Video", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECONDARY NAVBAR COMPOSABLES (Responsive)
// -----------------------------------------------------------------------------
@Composable
fun SecondaryNavBar(
    navController: NavController,
    currentRoute: String,
    onReelsClick: () -> Unit,
    hasUnreadMessages: Boolean = false,
    hasUnreadNotifications: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth >= 600
    val isLargeTablet = screenWidth >= 840
    
    // Responsive sizing
    val iconSize = if (isTablet) 28.dp else 24.dp
    // Adjust padding to ensure items aren't too spread out or cramped
    val horizontalPadding = if (isTablet) 32.dp else 16.dp
    val verticalPadding = if (isTablet) 16.dp else 8.dp
    
    // Bottom Navigation with "Pro" aesthetic
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIcon(
            icon = Icons.Filled.Home,
            selected = currentRoute == UserRoutes.HOME_SCREEN,
            iconSize = iconSize,
            description = "Home"
        ) {
            navController.navigate(UserRoutes.HOME_SCREEN) {
                popUpTo(UserRoutes.HOME_SCREEN) { inclusive = true }
                launchSingleTop = true
            }
        }
        
        NavIcon(
            icon = Icons.Filled.PlayArrow,
            selected = currentRoute == UserRoutes.REELS_SCREEN,
            iconSize = iconSize,
            description = "Reels"
        ) {
            onReelsClick()
        }
        
        NavIcon(
            icon = Icons.Filled.TrendingUp,
            selected = currentRoute == UserRoutes.TRENDS_SCREEN,
            iconSize = iconSize,
            description = "Trends"
        ) {
            navController.navigate(UserRoutes.TRENDS_SCREEN)
        }
        
        NavIcon(
            icon = Icons.Filled.Message,
            selected = currentRoute == "chatList",
            showBadge = hasUnreadMessages,
            iconSize = iconSize,
            description = "Messages"
        ) {
            navController.navigate("chatList")
        }
        
        NavIcon(
            icon = Icons.Filled.Notifications,
            selected = currentRoute == UserRoutes.NOTIFICATIONS_SCREEN,
            showBadge = hasUnreadNotifications,
            iconSize = iconSize,
            description = "Notifications"
        ) {
            navController.navigate(UserRoutes.NOTIFICATIONS_SCREEN) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}

@Composable
fun NavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    showBadge: Boolean = false,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    description: String? = null,
    onClick: () -> Unit
) {
    // Animation for smooth color transitions
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0xFFFFC107) else Color.Transparent, // Solid Yellow vs Transparent
        animationSpec = tween(durationMillis = 300)
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF1F2937) else Color(0xFF9CA3AF), // Dark Gray on Yellow vs Gray on White
        animationSpec = tween(durationMillis = 300)
    )

    // Using Box for the touch target and visual container
    Box(
        modifier = Modifier
            .clip(CircleShape) // Fully rounded for a premium feel
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp), // Comfortable touch area
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )

        // Notification Badge (Red Dot)
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .border(1.dp, Color.White, CircleShape) // Add white border for separation
            )
        }
    }
}



// -----------------------------------------------------------------------------
// SEARCH SCREEN COMPONENTS (DESIGN IMPROVED)
// -----------------------------------------------------------------------------

// --- Data Model and Mock Data (Unchanged) ---
data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val imageUrl: ImageVector
)

private val mockRestaurants = listOf(
    Restaurant("1", "Chili's", "American Grill", Icons.Default.Restaurant),
    Restaurant("2", "Zink", "Modern Fusion", Icons.Default.Restaurant),
    Restaurant("3", "The Corner", "Cafe & Bistro", Icons.Default.Restaurant),
    Restaurant("4", "Cristy Naan", "Indian Cuisine", Icons.Default.Restaurant),
)

@Composable
fun RestaurantListItem(
    restaurant: Restaurant,
    onItemClick: (restaurantId: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Slightly less rounded corners
            .background(LightBackground)
            .clickable { onItemClick(restaurant.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp), // Adjusted padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon/Image Area (Modernized look)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)) // Increased corner radius for a softer look
                    .background(Color(0xFFF3F4F6)), // Very light gray background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier.size(32.dp),
                    tint = PrimaryDark
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Block
            Column {
                Text(
                    text = restaurant.name,
                    fontWeight = FontWeight.SemiBold, // Slightly lighter font weight
                    fontSize = 18.sp, // Slightly larger font
                    color = PrimaryDark
                )
                Text(
                    text = restaurant.cuisine,
                    fontSize = 14.sp,
                    color = GrayInactive
                )
            }
        }

        // Right side: Radio Button/Selector (Circle placeholder - Simplified design)
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    2.dp,
                    Color(0xFFD1D5DB),
                    CircleShape
                ) // Kept the border/circle structure
                .clip(CircleShape)
        )
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC SEARCH OVERLAY COMPOSABLES (FIXED PADDING AND COLORS)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSearchOverlay(
    onDismiss: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    // 🌟 FIX 2: Add the modifier parameter to the function signature
    modifier: Modifier = Modifier
) {
    // --- Dependencies & ViewModel Setup ---
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val client = remember { HttpClient() }
    val apiService = remember { ProfessionalApiService(client, tokenManager) }
    val repository = remember { ProfessionalRepository(apiService) }

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(
            ProfessionalRepository(KtorClient.professionalApiService)
        )
    )

    // --- State Management ---
    var searchText by remember { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val errorMessage by searchViewModel.errorMessage.collectAsState()

    // --- Navigation ---
    val navigateToProfessionalProfile: (professionalId: String) -> Unit = { professionalId ->
        onDismiss() // Close the overlay first
        onNavigateToProfile(professionalId)
    }

    Scaffold(
        topBar = {
            // Animated TopAppBar that hides title when search is active
            var isSearchFocused by remember { mutableStateOf(false) }
            
            CenterAlignedTopAppBar(
                title = { Text("Search Professionals", fontWeight = FontWeight.SemiBold, color = PrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Filter Click */ }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
            )
        },
        containerColor = Color(0xFFF8F8FB),
        // 🌟 FIX 3: Apply the passed-in modifier to the root Scaffold
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Apply paddingValues from Scaffold to the main content Column
                .padding(paddingValues)
        ) {
            // Animated search field with smooth entrance
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { -it }
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { -it }
                )
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { newValue ->
                        searchText = newValue
                        if (newValue.isNotEmpty()) {
                            searchViewModel.searchByName(newValue)
                        } else {
                            // Clear search results when text is empty
                            searchViewModel.clearSearch()
                        }
                    },
                    placeholder = { Text("Search by name...", color = GrayInactive) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = YellowAccent
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchText = ""
                                    searchViewModel.clearSearch()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = GrayInactive
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowBorder,
                        unfocusedBorderColor = VeryPaleYellow,
                        cursorColor = YellowBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedPlaceholderColor = GrayInactive,
                        unfocusedPlaceholderColor = GrayInactive
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Dynamic Content Display (Loading, Error, Results) ---
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Use Yellow Accent for loading indicator
                        CircularProgressIndicator(color = YellowAccent)
                    }
                }

                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Error loading results",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                text = "Please check your connection and try again",
                                color = GrayInactive,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                searchResults.isEmpty() && searchText.isNotEmpty() && !isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No professionals found", color = GrayInactive)
                    }
                }
                
                searchText.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Type a name to search for professionals",
                            color = GrayInactive,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { professional ->
                            ProfessionalListItem(
                                professional = professional,
                                onItemClick = navigateToProfessionalProfile
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalListItem(
    professional: ProfessionalDto,
    onItemClick: (professionalId: String) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(professional.id) },
        // Use White background for the card
        colors = CardDefaults.cardColors(containerColor = LightBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp) // Rounded card corners
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                if (professional.profilePictureUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(BaseUrlProvider.getFullImageUrl(professional.profilePictureUrl))
                            .crossfade(true)
                            .build(),
                        contentDescription = professional.fullName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = rememberVectorPainter(Icons.Default.Store),
                        error = rememberVectorPainter(Icons.Default.Store)
                    )
                } else {
                    Icon(
                        Icons.Filled.Store,
                        contentDescription = "Professional Icon",
                        tint = PrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Display the Full Name (Restaurant Name)
                Text(
                    text = professional.fullName ?: "Unnamed Professional",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryDark
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC NOTIFICATION DROPDOWN COMPOSABLES (DESIGN UNCHANGED AS IT'S A SYSTEM COMPONENT)
// -----------------------------------------------------------------------------
@Composable
fun NotificationButton(
    hasNew: Boolean,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Icon with dot
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryDark
                )
            }

            if (hasNew) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107)) // Yellow dot
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
        }

        // Dropdown anchored to this same Box
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val notifications = listOf(
                "New follower: Alex Smith",
                "Your post got 10 likes!",
                "Order #1001 confirmed."
            )

            notifications.forEach { notification ->
                DropdownMenuItem(
                    text = { Text(notification) },
                    onClick = { expanded = false }
                )
            }

            Divider()
            DropdownMenuItem(
                text = { Text("View All Notifications", fontWeight = FontWeight.Bold) },
                onClick = {
                    navController.navigate("notifications_screen")
                    expanded = false
                }
            )
        }
    }
}