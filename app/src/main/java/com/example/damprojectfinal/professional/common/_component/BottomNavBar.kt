package com.example.damprojectfinal.professional.common._component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animated colors for smooth transitions
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFC107) else Color(0xFF1F2937), // Yellow when selected, dark gray when not
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFC107) else Color(0xFF6B7280), // Yellow when selected, gray when not
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )
    
    // Animated elevation for the selected icon circle
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )
    
    // Animated size for the circle
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 40.dp,
        animationSpec = tween(durationMillis = 300),
        label = "circleSize"
    )

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated offset for smooth pop-out effect
        val verticalOffset by animateDpAsState(
            targetValue = if (isSelected) (-12).dp else 0.dp,
            animationSpec = tween(durationMillis = 300),
            label = "verticalOffset"
        )

        Box(
            modifier = Modifier
                .offset(y = verticalOffset),
            contentAlignment = Alignment.Center
        ) {
            // Small white dot indicator above the icon when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            // Only show the elevated white circle with yellow border when selected
            if (isSelected) {
                // Elevated white circle with yellow border for selected state
                Card(
                    modifier = Modifier.size(circleSize),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                    border = BorderStroke(2.dp, Color(0xFFFFC107))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                // Simple icon for unselected state
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

// ---------------------------------------------------
// --- Component: Professional Bottom Navigation Bar ---
// ---------------------------------------------------

@Composable
fun ProfessionalBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String,
    professionalId: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF9F6)) // Creamy dark white background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 18.dp) // Increased vertical padding to make it thicker
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    isSelected = currentRoute.contains("home_screen_pro", ignoreCase = true),
                    onClick = {
                        navController.navigate("HOME_SCREEN_PRO/$professionalId") {
                            popUpTo("HOME_SCREEN_PRO/$professionalId") { inclusive = true }
                        }
                    }
                )

                // Chat/Messages
                BottomNavItem(
                    icon = Icons.Filled.Chat,
                    label = "Chat",
                    isSelected = currentRoute.contains("chat"),
                    onClick = {
                        navController.navigate("chatListPro")
                    }
                )

                // Add/Create New Item
                BottomNavItem(
                    icon = Icons.Filled.Add,
                    label = "Add",
                    isSelected = currentRoute.contains("create_content"),
                    onClick = {
                        navController.navigate("create_content_screen")
                    }
                )

                // Notifications
                BottomNavItem(
                    icon = Icons.Filled.Notifications,
                    label = "Notifications",
                    isSelected = currentRoute.contains("notification") || currentRoute.contains("notifications"),
                    onClick = {
                        navController.navigate("pro_notifications_screen")
                    }
                )

                // Menu Management
                BottomNavItem(
                    icon = Icons.Filled.MenuBook,
                    label = "Menu",
                    isSelected = currentRoute.contains("menu_management"),
                    onClick = {
                        navController.navigate("menu_management/$professionalId")
                    }
                )
            }
        }
    }
}