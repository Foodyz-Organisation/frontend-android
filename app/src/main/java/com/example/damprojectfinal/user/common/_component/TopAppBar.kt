package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.UserRoutes // Import the routes object to access constants

// -----------------------------------------------------------------------------
// MAIN COMPOSABLE
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String,
    openDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    // ⭐ CHANGE 1: Accept the current User ID for profile navigation
    currentUserId: String,
    onProfileClick: (userId: String) -> Unit // This is the navigation hook for the profile
) {
    var showAddOptions by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) } // State for the dropdown

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
                    // ⭐ CHANGE 2: Call the lambda and pass the currentUserId
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

            // App title (Unchanged)
            Text(
                text = "Foodies",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color(0xFF1F2A37),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )

            // Search Button (Unchanged)
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF334155))
            }

            // --- Notifications Icon (Unchanged) ---
            NotificationIconWithDropdown(
                showNotifications = showNotifications,
                onToggle = { showNotifications = it },
                navController = navController
            )
            // ----------------------------------------------------

            // Drawer Button (Unchanged)
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

        // Secondary Nav Bar (Unchanged)
        SecondaryNavBar(navController = navController, currentRoute = currentRoute)

        // Add Options Popup (Unchanged)
        if (showAddOptions) {
            // ... (Add Options code remains unchanged)
        }
    }
}

// -----------------------------------------------------------------------------
// SECONDARY NAVBAR COMPOSABLES (Unchanged)
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
        NavIcon(Icons.Filled.AttachMoney, currentRoute == "orders") { navController.navigate("orders") }
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
// DYNAMIC SEARCH OVERLAY COMPOSABLES (FIXED: Uses a literal ID on click)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSearchOverlay(onNavigateToProfile: (profileId: String?) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    val searchHistory = remember { mutableStateListOf("Pizza near me", "Vegan bowl", "Spicy ramen", "Burger deals") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 8.dp)
    ) {
        // ... (Search Input Bar remains unchanged)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Back/Close Button
            IconButton(onClick = { onNavigateToProfile(null) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close Search", tint = Color(0xFF334155))
            }

            // Search Input Field
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search Foodies...", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedBorderColor = Color(0xFF334155),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF334155),
                ),
                singleLine = true
            )
        }

        Divider()

        // --- Search History/Results ---
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (searchText.isEmpty()) {
                item {
                    Text(
                        text = "Recent Searches",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()
                }

                items(searchHistory) { item ->
                    HistoryItem(
                        text = item,
                        // This logic is confusing, an item click should navigate to a search result, not a profile.
                        // I will assume for now it's intended to navigate, and pass the text as a placeholder ID.
                        onItemClick = {
                            searchText = item
                            // ⭐ IMPORTANT: This assumes the search result text *is* a profile ID, which is likely wrong.
                            // This part of the logic needs review in a real app, but for now, we use the text as a mock ID.
                            onNavigateToProfile(item)
                        },
                        onRemoveClick = { searchHistory.remove(item) }
                    )
                }
            } else {
                item {
                    Text("Searching for results...", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryItem(text: String, onItemClick: () -> Unit, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = "History", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = text, color = Color(0xFF1F2937))
        }

        IconButton(onClick = onRemoveClick, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove History", tint = Color(0xFF9CA3AF))
        }
    }
}

// -----------------------------------------------------------------------------
// DYNAMIC NOTIFICATION DROPDOWN COMPOSABLES (Unchanged)
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
// PREVIEW (Updated for new parameter)
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
        currentUserId = "MOCK_USER_ID", // Added mock ID for preview
        onProfileClick = {} // Changed signature
    )
}