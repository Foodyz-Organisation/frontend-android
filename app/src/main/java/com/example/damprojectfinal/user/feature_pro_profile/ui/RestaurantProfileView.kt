package com.example.damprojectfinal.user.feature_pro_profile.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

// --- Design Colors ---
val PrimaryRed = Color(0xFFEF4444)
val BackgroundLight = Color(0xFFF9FAFB)
val CardBackground = Color(0xFFFFFFFF)
val PrimaryYellow = Color(0xFFFFC107) // Yellow color for primary actions
val DarkTextForYellow = Color(0xFF1F2937) // Dark text for contrast on yellow
val DarkText = Color(0xFF1F2937) // Dark text color

// --- Data Classes ---
data class RestaurantDetails(
    val name: String,
    val cuisine: String,
    val priceRange: String,
    val description: String,
    val imageUrl: String? = null,
    val rating: Double? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val deliveryTime: String,
    val takeawayTime: String,
    val dineInAvailable: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileView(
    professionalId: String, // ⭐ Add this
    restaurantDetails: RestaurantDetails,
    onBackClick: () -> Unit,
    navController: NavController // Add NavController for navigation
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
                        Log.d("ClientProfile", "Navigating to menu with professionalId = $professionalId")
                        navController.navigate("menu_order_route/$professionalId")
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
// --- Preview Data ---
val mockChilis = RestaurantDetails(
    name = "Chili's Grill & Bar",
    cuisine = "American, Tex-Mex",
    priceRange = "$$",
    description = "A popular American casual dining restaurant chain known for its Tex-Mex cuisine, burgers, and margaritas.",
    imageUrl = "https://picsum.photos/400/300",
    rating = 4.5,
    phone = "+1 555-0123",
    email = "contact@chilis.com",
    address = "123 Main Street, City, State 12345",
    deliveryTime = "30-45 min",
    takeawayTime = "15-20 min",
    dineInAvailable = true
)

// --- Composable Components ---

@Composable
fun HeaderSection(
    details: RestaurantDetails,
    isEditable: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Restaurant Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                if (details.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(details.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Restaurant Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Restaurant",
                        modifier = Modifier.size(64.dp),
                        tint = PrimaryRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = details.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
    }
}

@Composable
fun InfoSection(
    details: RestaurantDetails,
    isEditable: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rating
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = PrimaryYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details.rating?.toString() ?: "N/A",
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            
            // Price Range
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Price",
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details.priceRange,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            }
            
            // Cuisine
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Cuisine",
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details.cuisine.split(",").firstOrNull() ?: "N/A",
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ManagementCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Divider(color = BackgroundLight)
            content()
        }
    }
}

@Composable
fun ReadOnlyInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryRed,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DarkText
            )
        }
    }
}

@Composable
fun ContactManagementSection(
    state: Any?,
    details: RestaurantDetails,
    isEditable: Boolean
) {
    ManagementCard(title = "Contact & Location") {
        if (details.phone != null) {
            ReadOnlyInfoRow(Icons.Default.Phone, "Phone", details.phone)
        }
        if (details.email != null) {
            ReadOnlyInfoRow(Icons.Default.Email, "Email", details.email)
        }
        if (details.address != null) {
            ReadOnlyInfoRow(Icons.Default.LocationOn, "Address", details.address)
        }
    }
}

// --- Wrapper Screen for Navigation ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileViewScreen(
    professionalId: String,
    navController: NavController
) {
    // TODO: Load actual restaurant details from API/ViewModel
    // For now, using mock data
    val restaurantDetails = remember(professionalId) {
        mockChilis.copy() // In real implementation, load from ViewModel
    }
    
    RestaurantProfileView(
        professionalId = professionalId,
        restaurantDetails = restaurantDetails,
        onBackClick = { navController.popBackStack() },
        navController = navController
    )
}

@Preview(showBackground = true)
@Composable
fun ClientRestaurantProfilePreview() {
    // Preview requires a mock NavController - using empty lambda for preview
    // In real usage, this will be provided by RestaurantProfileViewScreen
    // Note: Preview won't work with NavController, so we skip it here
}
