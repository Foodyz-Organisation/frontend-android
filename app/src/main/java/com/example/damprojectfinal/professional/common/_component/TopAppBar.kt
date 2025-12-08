package com.example.damprojectfinal.professional.common._component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ⭐ IMPORT ADDED: Necessary for the clickable modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
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
    currentRoute: String = "home", // Track current route
    onLogout: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(modifier = Modifier.background(Color.White).statusBarsPadding()) {
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

            // Menu Icon (trigger drawer)
            IconButton(
                onClick = {
                    Log.d("TopAppBar", "Menu IconButton clicked")
                    onMenuClick()
                }
            ) {
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
            NavTopIcon(Icons.Filled.Dashboard, "home", currentRoute == "home") {
                navController.navigate("HOME_SCREEN_PRO/$professionalId") {
                    popUpTo("HOME_SCREEN_PRO/$professionalId") { inclusive = true }
                }
            }

            // Add/Create New Item
            NavTopIcon(Icons.Filled.Add, "add_item", currentRoute == "add_item") {
                navController.navigate("create_content_screen")
            }

            // Events List (changed from Manage Orders)
            NavTopIcon(Icons.Filled.ListAlt, "manage_orders", currentRoute == "manage_orders") {
                Log.d("TopAppBar", "List icon clicked - navigating to event_list_remote")
                navController.navigate("event_list_remote") {
                    launchSingleTop = true
                }
            }

            // Chat/Messages
            NavTopIcon(Icons.Filled.Chat, "chat", currentRoute == "chat") {
                navController.navigate("chatList")
            }

            // Menu Management
            NavTopIcon(Icons.Filled.MenuBook, "menu", currentRoute == "menu") {
                navController.navigate("menu_management/$professionalId")
            }
        }
    }
}