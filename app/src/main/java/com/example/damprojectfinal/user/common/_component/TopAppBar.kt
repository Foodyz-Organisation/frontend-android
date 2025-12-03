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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.ProRoutes.ORDERS_ROUTE
import com.example.damprojectfinal.core.api.ProfessionalApiService
import com.example.damprojectfinal.core.dto.auth.ProfessionalDto
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.core.repository.ProfessionalRepository
import com.example.damprojectfinal.user.common.viewmodel.SearchViewModel
import io.ktor.client.HttpClient

// -----------------------------------------------------------------------------
// MAIN TOP APP BAR COMPOSABLE
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String,
    openDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    currentUserId: String,
    onProfileClick: (userId: String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var showAddOptions by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            // Profile Avatar Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF4FB))
                    .clickable { onProfileClick(currentUserId) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Placeholder",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(24.dp)
                )
            }

            // App title
            Text(
                text = "Foodies",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color(0xFF1F2A37),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )

            // Search Button
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF334155))
            }

            // Notifications Icon
            NotificationIconWithDropdown(
                showNotifications = showNotifications,
                onToggle = { showNotifications = it },
                navController = navController
            )

            // Drawer Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF4FB))
                    .clickable { openDrawer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Drawer", tint = Color(0xFF334155))
            }
        }

        // Secondary Nav Bar
        SecondaryNavBar(navController = navController, currentRoute = currentRoute)

        // Add Options Popup (Placeholder)
        if (showAddOptions) {
            // ... (Add Options code remains unchanged)
        }
    }
}

// -----------------------------------------------------------------------------
// SECONDARY NAVBAR COMPOSABLES
// -----------------------------------------------------------------------------

@Composable
fun SecondaryNavBar(navController: NavController, currentRoute: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavIcon(Icons.Filled.Home, currentRoute == "home") { navController.navigate("home") }
        NavIcon(Icons.Filled.TrendingUp, currentRoute == "trends") { navController.navigate("trends") }
        NavIcon(Icons.Filled.PlayArrow, currentRoute == "reels") { navController.navigate("reels") }
        NavIcon(Icons.Filled.Chat, currentRoute == "chat") { navController.navigate("chat") }
        NavIcon(Icons.Filled.AttachMoney, currentRoute == ORDERS_ROUTE) {
            navController.navigate(ORDERS_ROUTE)
        }
    }
}

@Composable
fun NavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFFF0F0F0) else Color.Transparent
    val iconColor = if (selected) Color(0xFF334155) else Color(0xFF64748B)
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
    }
}

// -----------------------------------------------------------------------------
// SEARCH SCREEN COMPONENTS
// -----------------------------------------------------------------------------

// --- Data Model and Mock Data ---
data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    // ⭐ UPDATED TYPE: Change Int to ImageVector to match the Icons.Default.Restaurant usage.
    val imageUrl: ImageVector
)

private val mockRestaurants = listOf(
    // These now correctly match the ImageVector type:
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onItemClick(restaurant.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // ⭐ UPDATED: Replaced Image with Icon to use the ImageVector property
            Icon(
                imageVector = restaurant.imageUrl, // This is the ImageVector (e.g., Icons.Default.Restaurant)
                contentDescription = restaurant.name,
                // Set size and clipping on the Icon itself
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)), // Optional: Add a light background for visibility
                tint = Color(0xFF334155) // Optional: Set a color for the icon
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Block
            Column {
                Text(
                    text = restaurant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = restaurant.cuisine,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        // Right side: Radio Button/Selector (Circle placeholder from screenshot)
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, Color(0xFFD1D5DB), CircleShape)
                .clip(CircleShape)
        )
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC SEARCH OVERLAY COMPOSABLES (TRANSFORMED TO SEARCH RESTAURANTS SCREEN)
// -----------------------------------------------------------------------------
// In file: com.example.damprojectfinal.user.common._component/DynamicSearchOverlay.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSearchOverlay(
    navController: NavController,
    onDismiss: () -> Unit
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
        onDismiss()
        navController.navigate("client_profile_view/$professionalId")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Professionals", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Filter Click */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F7))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Search Input ---
            OutlinedTextField(
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    searchViewModel.searchByName(searchText) // Triggers the search
                },
                placeholder = { Text("Search by name...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Dynamic Content Display (Loading, Error, Results) ---
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage ?: "Error loading results", color = Color.Red)
                    }
                }
                searchResults.isEmpty() && searchText.isNotEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No professionals found")
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
            // ⭐ Attach the click handler using the professional's ID
            .clickable { onItemClick(professional.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ⭐ Display the Full Name
            Text(
                text = professional.fullName ?: "Unnamed Professional",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            // ⭐ Display the Email (as a secondary detail)
            Text(
                text = professional.email ?: "",
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // You can add more details here (e.g., cuisine, location) if they are in ProfessionalDto
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC NOTIFICATION DROPDOWN COMPOSABLES
// -----------------------------------------------------------------------------
@Composable
fun NotificationIconWithDropdown(
    showNotifications: Boolean,
    onToggle: (Boolean) -> Unit,
    navController: NavController
) {
    // Mock Notifications data
    val notifications = listOf(
        "New follower: Alex Smith",
        "Your post got 10 likes!",
        "Order #1001 confirmed."
    )

    Box {
        IconButton(onClick = { onToggle(!showNotifications) }) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color(0xFF334155))
        }

        DropdownMenu(
            expanded = showNotifications,
            onDismissRequest = { onToggle(false) }
        ) {
            if (notifications.isEmpty()) {
                DropdownMenuItem(text = { Text("No new notifications") }, onClick = { onToggle(false) })
            } else {
                notifications.forEach { notification ->
                    DropdownMenuItem(
                        text = { Text(notification) },
                        onClick = {
                            // Handle notification click (e.g., navigate to post/order)
                            onToggle(false)
                        }
                    )
                }
            }
            Divider()
            DropdownMenuItem(
                text = { Text("View All Notifications", fontWeight = FontWeight.Bold) },
                onClick = {
                    // Navigate to a dedicated notifications screen
                    navController.navigate("notifications_screen")
                    onToggle(false)
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW
// -----------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun TopAppBarPreview() {
    val navController = rememberNavController()
    TopAppBar(
        navController = navController,
        currentRoute = "home",
        openDrawer = {},
        onSearchClick = {},
        currentUserId = "MOCK_USER_ID",
        onProfileClick = {},
        onLogoutClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DynamicSearchOverlayPreview() {
    // Remove the unused mock repository definition:
    /*
    val mockRepository = object : ProfessionalRepository {
        override suspend fun searchByName(name: String) = emptyList<ProfessionalDto>()
        // Add other functions if needed
    }
    */

    DynamicSearchOverlay(
        navController = rememberNavController(),
        onDismiss = {},
        // REMOVE THIS LINE: repository = mockRepository
    )
}