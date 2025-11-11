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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String,
    openDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
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
            // Profile Avatar Button (Unchanged)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF4FB))
                    .clickable { navController.navigate("profile") },
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

            // --- Notifications Icon (NEW ENHANCED VERSION) ---
            NotificationIconWithDropdown(
                showNotifications = showNotifications,
                onToggle = { showNotifications = it }, // This updates the local state
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
                                    Icon(Icons.Filled.Edit, contentDescription = "Post", tint = Color(0xFF2563EB))
                                }
                                Text("Post", fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { /* Add Photo */ }) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Photo", tint = Color(0xFF10B981))
                                }
                                Text("Photo", fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { /* Add Video */ }) {
                                    Icon(Icons.Filled.Videocam, contentDescription = "Video", tint = Color(0xFFF59E0B))
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
// DYNAMIC SEARCH OVERLAY COMPOSABLES (FIXED)
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
            .padding(top = 8.dp) // Pushes content down slightly from the top
    ) {
        // --- Search Input Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Back/Close Button
            // Calls onNavigateToProfile(null) to signal 'just close'
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
                        // FIX APPLIED: On item click, execute search and trigger navigation with the item text (ID)
                        onItemClick = {
                            searchText = item
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
        // FIX 1: Pass an empty lambda for onSearchClick (already correct)
        onSearchClick = {},
        // *** FIX 2: Add the missing onProfileClick parameter! ***
        onProfileClick = {}
    )
}