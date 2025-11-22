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
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                // ⭐ ADDED NAVIGATION ICON HERE ⭐
                navigationIcon = {
                    IconButton(onClick = onBackClick) { // Calls the navigation action
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Return to Home Screen"
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ){ paddingValues ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Profile Header (Image and Name) ---
                ProfileHeader(user = userProfile, onEditProfileClick = onEditProfileClick)

                Spacer(modifier = Modifier.height(32.dp))

                // --- 2. Profile Details Card ---
                ProfileDetailsCard(user = userProfile)

                Spacer(modifier = Modifier.height(32.dp))

                // --- 3. Action Menu ---
                ActionMenu(
                    onSettingsClick = onEditProfileClick,
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserProfile, onEditProfileClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE15A))

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


            // Edit Button overlay (unchanged)
            FloatingActionButton(
                onClick = onEditProfileClick,
                modifier = Modifier.size(36.dp).offset(x = 4.dp, y = 4.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF374151)
        )
        Text(
            text = user.email,
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun ProfileDetailsCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            DetailRow(label = "Email", value = user.email, icon = Icons.Filled.Email)
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            DetailRow(label = "Member Since", value = user.joinDate, icon = Icons.Filled.CalendarMonth)
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            DetailRow(label = "Role", value = user.role, icon = Icons.Filled.Star)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
        }
    }
}

@Composable
fun ActionMenu(
    onSettingsClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            ActionMenuItem(Icons.Filled.Settings, "Account Settings", Color(0xFF374151), onSettingsClick)
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun ActionMenuItem(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, fontSize = 16.sp, color = color, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = Color(0xFF9CA3AF))
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