package com.example.damprojectfinal.user.common._component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

// --- New Reusable Composable ---
@Composable
fun NotificationIconWithDropdown(
    showNotifications: Boolean,
    onToggle: (Boolean) -> Unit, // Callback to update the parent's state
    navController: Any? // Use Any for NavController or replace with appropriate type
) {
    // --- STATIC EXAMPLE DATA (Replace with ViewModel/Flow data later) ---
    val unreadCount = 3
    val dummyNotifications = listOf(
        "New follower: Alice",
        "Order ready for pickup!",
        "Your post got 10 likes!",
    )
    // ------------------------------------------------------------------

    Box {
        // --- 1. Stylish Icon Button ---
        IconButton(
            onClick = { onToggle(!showNotifications) },
            modifier = Modifier.size(44.dp) // Matches the Drawer Button size
        ) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF334155), // Dark gray tint
                modifier = Modifier.size(26.dp)
            )
        }

        // --- 2. Notification Badge ---
        if (unreadCount > 0) {
            Text(
                text = unreadCount.toString(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp) // Fine-tune offset for the 44dp icon
                    .clip(CircleShape)
                    .background(Color.Red)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // --- 3. Enhanced Dropdown Menu ---
        DropdownMenu(
            expanded = showNotifications,
            onDismissRequest = { onToggle(false) },
            modifier = Modifier
                .width(280.dp) // Fixed width for better appearance
                .clip(RoundedCornerShape(12.dp)),
            properties = PopupProperties(focusable = true)
        ) {
            Text(
                text = "Notifications ($unreadCount new)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1F2A37),
                modifier = Modifier.padding(16.dp)
            )
            Divider()

            dummyNotifications.take(3).forEach { message ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    },
                    onClick = { onToggle(false) /* Navigate to item */ },
                    modifier = Modifier.background(Color(0xFFEFF4FB).copy(alpha = 0.5f)) // Light background for list items
                )
                // Use a smaller divider between items
                Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
            }

            // View All Footer
            DropdownMenuItem(
                text = {
                    Text(
                        "View All",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onClick = {
                    onToggle(false)
                    // (Optional) Add navigation call here: navController?.navigate("notifications_route")
                }
            )
        }
    }
}
// END NotificationIconWithDropdown