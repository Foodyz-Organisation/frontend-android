package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel

// --- Custom Colors for Food App Vibe ---
val FoodPrimary = Color(0xFFFF5722) // Vibrant Orange-Red for food app primary (e.g., brand color)
val FoodAccent = Color(0xFFFF9800) // Orange Accent (e.g., placeholder or secondary highlight)
val FoodBackground = Color(0xFFF9F9F9) // Light grey background
val FoodCard = Color.White
val FoodTextPrimary = Color(0xFF212121) // Darker text for readability
val FoodTextSecondary = Color(0xFF757575) // Grey text

// --- UI Data Model (Used for display) ---
data class UserProfile(
    val name: String,
    val email: String,
    val joinDate: String = "",
    val role: String,
    val profilePictureUrl: String? = null // Nullable for icon logic
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel,
    // Note: If you want to use the ViewModel's uploadImage function,
    // you should change this signature to:
    // onImagePickerRequest: (userId: String) -> Unit,
    onEditProfileClick: () -> Unit = {},
) {
    val userResponse by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val userProfile: UserProfile = if (isLoading) {
        UserProfile(name = "Loading...", email = "", joinDate = "Loading...", role = "...")
    } else if (userResponse != null) {
        val rawProfilePictureUrl = userResponse!!.profilePictureUrl

        // ⭐ Prepend BASE_URL if the URL is a relative path (e.g., /uploads/image.jpg) ⭐
        val fullProfilePictureUrl = if (!rawProfilePictureUrl.isNullOrBlank()) {
            "http://10.0.2.2:3000" + rawProfilePictureUrl
        } else {
            null
        }

        UserProfile(
            name = userResponse!!.username,
            email = userResponse!!.email,
            // ⭐ FIX: Removed reference to 'createdAt' which caused an error ⭐
            joinDate = "Member since Unknown Date",
            // ⭐ FIX: Assuming the DTO property is 'role' (lowercase) ⭐
            role = userResponse!!.role,
            profilePictureUrl = fullProfilePictureUrl
        )
    } else {
        UserProfile(name = "User Not Found", email = "Error", joinDate = "N/A", role = "Guest")
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.ExtraBold, color = FoodTextPrimary) }, // Bolder text
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FoodBackground), // Use light background
                // ⭐ ADDED NAVIGATION ICON HERE ⭐
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // Calls the navigation action
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Return to Home Screen",
                            tint = FoodTextPrimary
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(FoodBackground) // Apply Food Background
    ){ paddingValues ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FoodPrimary)
            }
        } else if (errorMessage != null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Error: ${errorMessage!!}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp), // Use horizontal padding on the column
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Profile Header (Image and Name) ---
                ProfileHeader(user = userProfile, onEditProfileClick = onEditProfileClick)

                Spacer(modifier = Modifier.height(24.dp)) // Reduced spacing

                // --- 2. Profile Details Card (Used for metrics/quick info in food app style) ---
                ProfileDetailsCard(user = userProfile)

                Spacer(modifier = Modifier.height(24.dp)) // Reduced spacing

                // --- 3. Action Menu ---
                // Reorganized for better grouping
                ActionMenu(
                    onSettingsClick = onEditProfileClick,
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserProfile, onEditProfileClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(FoodAccent) // Use Accent color as placeholder background

            // ⭐ CONDITIONAL DISPLAY: Image or Icon ⭐
            if (!user.profilePictureUrl.isNullOrBlank()) {
                // If URL is present, try to load the image
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(user.profilePictureUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            } else {
                // If URL is null or blank, show a default icon
                Icon(
                    imageVector = Icons.Filled.Person, // Default person icon
                    contentDescription = "Default Profile Picture",
                    tint = Color.White,
                    modifier = imageModifier
                        .size(120.dp)
                        .padding(20.dp)
                )
            }


            // Edit Button overlay (improved FAB styling)
            FloatingActionButton(
                onClick = onEditProfileClick,
                modifier = Modifier.size(40.dp).offset(x = 4.dp, y = 4.dp), // Slightly larger FAB
                containerColor = FoodPrimary, // Use Food Primary color
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            fontSize = 26.sp, // Larger name
            fontWeight = FontWeight.Black, // Stronger weight
            color = FoodTextPrimary
        )
        Text(
            text = user.email,
            fontSize = 16.sp, // Slightly larger email text
            color = FoodTextSecondary
        )
    }
}

// Renamed/repurposed to serve as a 'Metrics' card common in food/ecommerce apps.
@Composable
fun ProfileDetailsCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Slightly smaller radius
        colors = CardDefaults.cardColors(containerColor = FoodCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Higher elevation for pop
    ) {
        // Instead of a vertical list of profile data, we show a horizontal metrics summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mock Data for food app metrics (using DetailRow's logic but adjusted for horizontal layout)
            MetricItem(value = "15", label = "Orders", icon = Icons.Filled.ListAlt)
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = FoodBackground)
            MetricItem(value = "8", label = "Favorites", icon = Icons.Filled.Favorite)
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = FoodBackground)
            MetricItem(value = "4.9", label = "Rating", icon = Icons.Filled.Star)
        }
    }
}

// Helper composable for the metrics items in ProfileDetailsCard
@Composable
fun MetricItem(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = FoodPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FoodTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = FoodTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}


// Original DetailRow kept for completeness, though it is not used in the new ProfileDetailsCard
@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = FoodAccent, modifier = Modifier.size(24.dp)) // Changed tint
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = FoodTextSecondary) // Changed color
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = FoodTextPrimary) // Changed color
        }
    }
}


@Composable
fun ActionMenu(
    onSettingsClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // --- Essential Food Actions Group ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FoodCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                ActionMenuItem(Icons.Filled.ListAlt, "My Orders", FoodTextPrimary, {}) // Added new action
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = FoodBackground)
                ActionMenuItem(Icons.Filled.LocationOn, "Delivery Addresses", FoodTextPrimary, {}) // Added new action
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = FoodBackground)
                ActionMenuItem(Icons.Filled.Payment, "Payment Methods", FoodTextPrimary, {}) // Added new action
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Settings/Support Group ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FoodCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                ActionMenuItem(Icons.Filled.Settings, "Account Settings", FoodTextPrimary, onSettingsClick)
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = FoodBackground)
                // Added Logout action
                ActionMenuItem(Icons.Filled.Logout, "Log Out", Color.Red, {})
            }
        }
    }
}

@Composable
fun ActionMenuItem(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp), // Taller button
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon with a colorful background circle for modern look
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        // Use FoodPrimary for most actions, but a softer red for Logout
                        .background(
                            if (icon == Icons.Filled.Logout) Color.Red.copy(alpha = 0.1f) else FoodPrimary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (icon == Icons.Filled.Logout) Color.Red else FoodPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    title,
                    fontSize = 16.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
            // Chevron is always a secondary color unless it's the Logout button
            val chevronTint = if (icon == Icons.Filled.Logout) Color.Red else FoodTextSecondary.copy(alpha = 0.6f)
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = chevronTint)
        }
    }
}

// ---------------------------------------------------------------------
// --- PREVIEWS ---
// ---------------------------------------------------------------------

@Preview(showBackground = true, name = "Profile Header Preview - With Image")
@Composable
fun ProfileHeaderWithImagePreview() {
    val mockUser = UserProfile(
        name = "Jane Doe",
        email = "jane.doe@example.com",
        joinDate = "October 2024",
        role = "Standard User",
        profilePictureUrl = "https://picsum.photos/id/1011/200/200"
    )
    MaterialTheme {
        ProfileHeader(user = mockUser, onEditProfileClick = {})
    }
}

@Preview(showBackground = true, name = "Profile Header Preview - No Image")
@Composable
fun ProfileHeaderNoImagePreview() {
    val mockUser = UserProfile(
        name = "Alex Smith",
        email = "alex.smith@example.com",
        joinDate = "January 2024",
        role = "Admin",
        profilePictureUrl = null
    )
    MaterialTheme {
        ProfileHeader(user = mockUser, onEditProfileClick = {})
    }
}