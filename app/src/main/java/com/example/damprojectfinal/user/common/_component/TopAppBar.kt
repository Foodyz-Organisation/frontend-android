package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.core.repository.ProfessionalRepository
import com.example.damprojectfinal.user.common.viewmodel.SearchViewModel
import io.ktor.client.HttpClient

// --- Design Colors/Constants ---
private val PrimaryDark = Color(0xFF1F2A37) // Dark Gray for text/icons
private val YellowAccent = Color(0xFFFFC107)  // Primary Yellow (Used for accents/dot)
private val GrayInactive = Color(0xFF64748B) // Gray for inactive elements
private val LightBackground = Color.White    // General background

// Search Field Colors
private val YellowBorder = Color(0xFFFACC15) // Light yellow when focused
private val VeryPaleYellow = Color(0xFFFDE68A) // Very pale yellow when unfocused

@Composable
fun NotificationIconWithDot(onClick: () -> Unit, hasNew: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = PrimaryDark)
        }
        if (hasNew) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(YellowAccent) // Yellow Dot
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 4.dp)
            )
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
    onLogoutClick: () -> Unit
) {
    var showAddOptions by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    val hasNewNotifications = true // Assume true for visual display

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBackground) // Use clean white background
            // 🌟 FIX 1: Use windowInsetsPadding for status bars to replace hardcoded 48.dp
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                // Removed top padding here, as it's handled by windowInsetsPadding above
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // Profile Avatar Button (Unchanged)
            Box(
                modifier = Modifier
                    .size(40.dp) // Smaller size
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)) // Simple gray background
                    .clickable { onProfileClick(currentUserId) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Placeholder",
                    tint = PrimaryDark,
                    modifier = Modifier.size(20.dp) // Smaller icon
                )
            }

            // App title (Unchanged)
            Text(
                text = "Foodies",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp, // Larger title
                color = PrimaryDark,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )

            // '+' navigates directly to post creation
            IconButton(onClick = { navController.navigate(UserRoutes.CREATE_POST) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Content", tint = PrimaryDark)
            }

            // Search Button
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = PrimaryDark)
            }

            // Notifications Icon (Using new helper for dot)
            NotificationIconWithDot(
                onClick = { showNotifications = !showNotifications },
                hasNew = hasNewNotifications
            )

            // Drawer Button (Simplified Styling)
            IconButton(onClick = openDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = "Drawer", tint = PrimaryDark)
            }
        }

        // Secondary Nav Bar (Unchanged)
        SecondaryNavBar(
            navController = navController,
            currentRoute = currentRoute,
            onReelsClick = onReelsClick
        )

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
// SECONDARY NAVBAR COMPOSABLES (Unchanged)
// -----------------------------------------------------------------------------
@Composable
fun SecondaryNavBar(
    navController: NavController,
    currentRoute: String,
    onReelsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavIcon(Icons.Filled.Home, currentRoute == UserRoutes.HOME_SCREEN) {
            navController.navigate(UserRoutes.HOME_SCREEN) {
                popUpTo(UserRoutes.HOME_SCREEN) {
                    inclusive = true
                } // optional: clear back stack
                launchSingleTop = true
            }
        }
        NavIcon(Icons.Filled.PlayArrow, currentRoute == UserRoutes.REELS_SCREEN) { onReelsClick() }
        NavIcon(Icons.Filled.TrendingUp, currentRoute == UserRoutes.TRENDS_SCREEN) { navController.navigate(UserRoutes.TRENDS_SCREEN) }
        NavIcon(
            Icons.Filled.Chat,
            currentRoute == "chatList"
        ) { navController.navigate("chatList") }
        NavIcon(
            Icons.Filled.AttachMoney,
            currentRoute == UserRoutes.ORDERS_ROUTE // highlight if current route is orders_history_route
        ) {
            navController.navigate(UserRoutes.ORDERS_ROUTE) {
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
    onClick: () -> Unit
) {
    // New, modern color scheme for selection
    val backgroundColor =
        if (selected) Color(0xFFFFF9C4) else Color.Transparent // light yellow background
    val iconColor =
        if (selected) Color(0xFFFFD700) else Color.Gray // golden yellow icon when selected

    // Using Box to ensure the background color covers a larger, clickable area
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor) // <-- This applies the light yellow background
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        ) // <-- This applies the dark blue icon tint
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
    val client = remember { HttpClient() }
    val apiService = remember { ProfessionalApiService(client) }
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
            // Use a standard Material3 TopAppBar to correctly handle system insets
            CenterAlignedTopAppBar(
                title = { Text("Search Professionals", fontWeight = FontWeight.SemiBold, color = PrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryDark)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Filter Click */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = PrimaryDark)
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
            OutlinedTextField(
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    searchViewModel.searchByName(searchText) // Triggers the search
                },
                placeholder = { Text("Search by name...", color = GrayInactive) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = GrayInactive
                    )
                },
                shape = RoundedCornerShape(12.dp), // Rounded corners
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp)
            )

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
                        Text(text = errorMessage ?: "Error loading results", color = Color.Red)
                    }
                }

                searchResults.isEmpty() && searchText.isNotEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No professionals found", color = GrayInactive)
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
            // Placeholder Avatar/Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Work,
                    contentDescription = "Professional Icon",
                    tint = PrimaryDark
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Display the Full Name
                Text(
                    text = professional.fullName ?: "Unnamed Professional",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryDark
                )
                // Display the Email
                Text(
                    text = professional.email ?: "Email not provided",
                    fontSize = 14.sp,
                    color = GrayInactive,
                    modifier = Modifier.padding(top = 2.dp)
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