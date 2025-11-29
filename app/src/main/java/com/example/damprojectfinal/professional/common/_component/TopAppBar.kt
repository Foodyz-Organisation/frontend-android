package com.example.damprojectfinal.professional.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ⭐ IMPORT ADDED: Necessary for the clickable modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.damprojectfinal.professional.common.NavTopIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProTopBarWithIcons(
    professionalId: String,
    navController: NavHostController,
    onLogout: () -> Unit // This is the callback to trigger logout
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
                    // ⭐ FIX APPLIED: Make the icon clickable and navigate
                    .clickable {
                        // Navigates to the Pro Profile Edit screen
                        navController.navigate("pro_profile_edit/$professionalId")
                    },
                tint = Color(0xFF6B7280)
            )

            Text(
                text = "Foodyz Pro",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            // Actions (Logout, Menu, Notifications)
            // ⭐ REPLACED: Search icon with Logout icon
            IconButton(onClick = onLogout) { // Call the onLogout callback directly
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
            }

            IconButton(onClick = { /* TODO: Open Drawer or Settings */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }

            // Notifications with Badge (Mocked)
            BadgedBox(badge = { Badge { Text("3") } }) {
                IconButton(onClick = { /* TODO: View notifications */ }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            }
        }

        // --- Secondary Icon Row (5 Organised Icons) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home/Dashboard (Orders)
            NavTopIcon(Icons.Filled.Dashboard, "home", true) {
                /* Current Screen, do nothing as this is the current screen */
                // The actual navigation back to home is better handled by popping to HOME_SCREEN_PRO route in AppNavigation
            }

            // Add/Create New Item
            NavTopIcon(Icons.Filled.Add, "add_item", false) {
                // Using the specific route for item creation
                navController.navigate("create_menu_item/$professionalId")
            }

            // Manage Orders (or another key list)
            NavTopIcon(Icons.Filled.ListAlt, "manage_orders", false) {
                navController.navigate("orders_management_route")
            }

            // Chat/Messages
            NavTopIcon(Icons.Filled.Chat, "chat", false) {
                navController.navigate("chat_pro_route")
            }

            // Menu Management (As requested)
            NavTopIcon(Icons.Filled.MenuBook, "menu", false) {
                navController.navigate("menu_management/$professionalId")
            }
        }
    }
}