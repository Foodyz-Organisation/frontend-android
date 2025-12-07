/*package com.example.damprojectfinal.user.feature_pro_profile.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// --- Design Colors ---
val PrimaryRed = Color(0xFFEF4444)
val BackgroundLight = Color(0xFFF9FAFB)
val CardBackground = Color(0xFFFFFFFF)
val PrimaryYellow = Color(0xFFFFC107) // Yellow color for primary actions
val DarkTextForYellow = Color(0xFF1F2937) // Dark text for contrast on yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRestaurantProfileScreen(
    professionalId: String, // ⭐ Add this
    restaurantDetails: RestaurantDetails,
    onBackClick: () -> Unit,
    onViewMenuClick: (professionalId: String) -> Unit // pass the ID
) {
    LaunchedEffect(professionalId) {
        Log.d("ClientProfile", "Opened profile with professionalId = $professionalId")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(restaurantDetails.name, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        // --- Bottom Bar: View Menu & Order Button ---
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = CardBackground) {
                Button(
                    onClick = {
                        Log.d("ClientProfile", "Navigating with professionalId = $professionalId")
                        onViewMenuClick(professionalId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DarkTextForYellow
                    )
                ) {
                    Text("View Menu & Order", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Image and Name Header (View-Only)
            item { HeaderSection(details = restaurantDetails, isEditable = false) }

            // 2. Ratings and Basic Info (View-Only)
            item { InfoSection(restaurantDetails, isEditable = false) }

            // 3. Core Business Details (View-Only)
            item {
                ManagementCard(title = "Core Business Details") {
                    ReadOnlyInfoRow(Icons.Default.Store, "Name", restaurantDetails.name)
                    ReadOnlyInfoRow(Icons.Default.Restaurant, "Cuisine", restaurantDetails.cuisine)
                    ReadOnlyInfoRow(Icons.Default.AttachMoney, "Price Range", restaurantDetails.priceRange)
                }
            }

            // 4. Public Description (View-Only)
            item {
                ManagementCard(title = "Public Description") {
                    Text(restaurantDetails.description, style = MaterialTheme.typography.bodyLarge, color = DarkText)
                }
            }

            // 5. Contact and Location Management (View-Only)
            item { ContactManagementSection(state = null, details = restaurantDetails, isEditable = false) }

            // 6. Service Times (View-Only)
            item {
                ManagementCard(title = "Service Times") {
                    ReadOnlyInfoRow(Icons.Default.DeliveryDining, "Delivery Time", restaurantDetails.deliveryTime)
                    ReadOnlyInfoRow(Icons.Default.TakeoutDining, "Takeaway Time", restaurantDetails.takeawayTime)
                    ReadOnlyInfoRow(Icons.Default.TableBar, "Dine-in", if (restaurantDetails.dineInAvailable) "Available" else "Not Available")
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ClientRestaurantProfilePreview() {
    ClientRestaurantProfileScreen(
        professionalId = "dummy_id", // ✅ required for preview
        restaurantDetails = mockChilis,
        onBackClick = {},
        onViewMenuClick = { id -> /* do nothing for preview */ }
    )
}*/
