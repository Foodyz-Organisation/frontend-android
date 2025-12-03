package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.R

// Define the items for the drawer
data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val drawerItems = listOf(
    DrawerItem(Icons.Default.Home, "Home", "home_route"),
    DrawerItem(Icons.Default.ChatBubble, "Chats", "chatList"),
    DrawerItem(Icons.Default.Settings, "Settings", "settings_route"),
    DrawerItem(Icons.Default.Favorite, "Favorites", "favorites_route"),
    DrawerItem(Icons.Default.Person, "Profile", "profile_route"),
    DrawerItem(Icons.Default.Help, "Help & Support", "help_route"),
)

// 🔑 NEW: Define the Professional Signup Item
val proSignupItem = DrawerItem(
    Icons.Default.Fastfood, // Use a relevant icon for restaurant/food
    "Signup as Professional",
    "pro_signup_route" // Use the route defined in your NavHost
)

@Composable
fun AppDrawer(
    onCloseDrawer: () -> Unit,
    navigateTo: (String) -> Unit,
    currentRoute: String, // Used to highlight the current screen
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFFFFFFF),
        modifier = Modifier.width(300.dp)
    ) {
        // --- 1. Drawer Header (Profile and App Info) ---
        DrawerHeader(
            onProfileClick = {
                navigateTo("user_profile_route") // Use defined route
                onCloseDrawer()
            }
        )

        // --- 2. Navigation Items ---
        Spacer(Modifier.height(8.dp))
        drawerItems.forEach { item ->
            DrawerMenuItem(
                item = item,
                isSelected = currentRoute == item.route,
                onClick = {
                    navigateTo(item.route)
                    onCloseDrawer()
                }
            )
        }

        // --- 🔑 3. Pro Application Button ---
        Spacer(Modifier.height(16.dp))
        // Highlighting the Pro application option with a distinct section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Divider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            DrawerMenuItem(
                item = proSignupItem,
                isSelected = currentRoute == proSignupItem.route,
                onClick = {
                    navigateTo(proSignupItem.route)
                    onCloseDrawer()
                }
            )
        }

        // --- 4. Footer (Logout/Divider) ---
        Spacer(Modifier.weight(1f)) // Pushes the footer to the bottom
        Divider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        DrawerFooter(onClickLogout = {
            onLogout()
            onCloseDrawer()
        })
    }
}
@Composable
fun DrawerHeader(onProfileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0)) // Very light gray background
            .padding(24.dp)
            .clickable(onClick = onProfileClick)
    ) {
        // Profile Image (using a placeholder for now)
        Image(
            painter = painterResource(id = R.drawable.profile), // Use your actual profile image resource
            contentDescription = "User Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFFFCC00), CircleShape) // Yellow border accent
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "John Doe", // Placeholder Name
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1F2A37)
        )
        Text(
            text = "john.doe@example.com", // Placeholder Email
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun DrawerMenuItem(item: DrawerItem, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFFFCC00).copy(alpha = 0.2f) else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF1F2A37) else Color(0xFF334155)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = item.label,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DrawerFooter(onClickLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickLogout)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Logout",
            tint = Color.Red,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = "Logout",
            fontWeight = FontWeight.SemiBold,
            color = Color.Red,
            fontSize = 16.sp
        )
    }
}
