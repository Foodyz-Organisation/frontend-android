package com.example.damprojectfinal.professional.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home // Changed from Dashboard to Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.damprojectfinal.ProRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProTopBarWithIcons(
    professionalId: String,
    navController: NavHostController,
    currentRoute: String = "home" // Track current route to highlight the correct icon
) {
    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Icon/Avatar
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
                    .padding(4.dp)
                    .clickable {
                        navController.navigate("professional_profile_screen/$professionalId")
                    },
                tint = Color(0xFF6B7280)
            )

            Text(
                text = "Foodyz Pro",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            // Search Icon (as per screenshot)
            IconButton(onClick = { /* TODO: Implement search */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }

            // Notifications with Badge (red dot as per screenshot)
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color.Red,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) {} // Just a dot, no number
                }
            ) {
                IconButton(onClick = { /* TODO: View notifications */ }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            }

            // Menu/Hamburger Icon (as per screenshot)
            IconButton(onClick = { /* TODO: Open Drawer or Settings */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }

        // --- Secondary Icon Row (5 Organised Icons matching screenshot) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home (Orange background, selected)
            NavTopIcon(Icons.Filled.Home, "home", currentRoute == "home") {
                navController.navigate("HOME_SCREEN_PRO/$professionalId") {
                    popUpTo("HOME_SCREEN_PRO/$professionalId") { inclusive = true }
                }
            }

            // Add/Create New post (Plus icon)
            NavTopIcon(Icons.Filled.Add, "add_item", currentRoute == ProRoutes.CREATE_CONTENT_SCREEN) {
                navController.navigate(ProRoutes.CREATE_CONTENT_SCREEN)
            }

            // List/Grid (Manage Orders) - using ListAlt as it's similar to the screenshot's icon
            NavTopIcon(Icons.Filled.ListAlt, "manage_orders", currentRoute == "manage_orders") {
                navController.navigate("orders_management_route")
            }

            // Chat/Messages
            NavTopIcon(Icons.Filled.Chat, "chat", currentRoute == "chat") {
                navController.navigate("chat_pro_route")
            }

            // Bell/Notifications (Changed from MenuBook to Notifications icon as per screenshot)
            NavTopIcon(Icons.Filled.Notifications, "notifications", currentRoute == "notifications") {
                // navController.navigate("notifications_route") // Define this route if needed
            }
        }
    }
}

@Composable
fun NavTopIcon(icon: ImageVector, description: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFFFFC107) else Color(0xFFF0F0F0)
    val iconColor = if (selected) Color.Black else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = iconColor, modifier = Modifier.size(24.dp))
    }
}


