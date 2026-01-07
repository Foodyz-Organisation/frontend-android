package com.example.damprojectfinal.professional.feature_posts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
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
import com.example.damprojectfinal.UserRoutes

@Composable
fun CreateContentScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            CreateContentTopBar(navController = navController)
        },
        containerColor = Color(0xFFF7F7F7) // Light background color to match the screenshot
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp), // Increased horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Increased space from top

            // Large Plus Icon
            Box(
                modifier = Modifier
                    .size(80.dp) // Larger size
                    .clip(CircleShape)
                    .background(Color(0xFFFFC107)), // Orange background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Content",
                    tint = Color.Black, // Black icon
                    modifier = Modifier.size(48.dp) // Larger icon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Content",
                style = MaterialTheme.typography.headlineMedium, // Changed to headlineMedium for larger title
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2A37)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose what you'd like to create",
                style = MaterialTheme.typography.bodyLarge, // Changed to bodyLarge for better readability
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp)) // Increased space before cards

            // Add Post Card
            CreateContentOptionCard(
                icon = Icons.Filled.AddAPhoto, // Specific icon for post
                title = "Add Post",
                description = "Share a photo or video",
                onClick = {
                    navController.navigate(UserRoutes.CREATE_POST)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add Event Card
            CreateContentOptionCard(
                icon = Icons.Filled.EmojiEvents, // Icon for event
                title = "Add Event",
                description = "Create a special event",
                onClick = {
                    navController.navigate("event_list_remote")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add Box-Deal Card
            CreateContentOptionCard(
                icon = Icons.Filled.Redeem, // Icon for deal
                title = "Add Box-Deal",
                description = "Offer a special deal",
                onClick = {
                    navController.navigate("pro_deals")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContentTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("") }, // Empty title to center the back button and remove default text
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController.popBackStack() } // Pop back stack on click
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back",
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Transparent background
    )
}

@Composable
fun CreateContentOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // Fixed height for consistent look
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Adjusted padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Icon background size
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF7EA)), // Light orange background
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFFFC107), // Orange icon
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp, // Larger title
                        color = Color(0xFF1F2A37)
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium, // Body medium for description
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
