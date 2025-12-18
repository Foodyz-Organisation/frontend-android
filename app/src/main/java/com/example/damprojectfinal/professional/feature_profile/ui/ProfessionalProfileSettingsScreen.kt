package com.example.damprojectfinal.professional.feature_profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.posts.PostsApiService
import com.example.damprojectfinal.core.dto.professionalUser.ProfessionalLocation
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.`object`.FileUtil
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel
import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import com.example.damprojectfinal.professional.feature_event.LocationData
import com.example.damprojectfinal.professional.feature_event.MapPickerScreen

// Colors are defined in ProfessionalProfileScreen.kt - using fully qualified names to avoid conflicts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileSettingsScreen(
    professionalId: String,
    navController: NavHostController,
    viewModel: ProfessionalProfileViewModel = viewModel(
        factory = ProfessionalProfileViewModel.Factory(
            tokenManager = TokenManager(LocalContext.current),
            professionalApiService = RetrofitClient.professionalApiService,
            postsApiService = RetrofitClient.postsApiService
        )
    )
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val updateError by viewModel.updateError.collectAsState()
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load profile on first launch
    LaunchedEffect(professionalId) {
        viewModel.loadProfessionalProfile(professionalId)
    }

    // State for form fields
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var hours by remember { mutableStateOf(profile?.hours ?: "") }
    var description by remember { mutableStateOf(profile?.description ?: "") }
    var locations by remember { mutableStateOf(profile?.locations ?: emptyList()) }
    var showMapPicker by remember { mutableStateOf(false) }
    var editingLocationIndex by remember { mutableStateOf<Int?>(null) }
    
    // State for selected images
    var selectedProfileImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBackgroundImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Update form when profile loads
    LaunchedEffect(profile) {
        phone = profile?.phone ?: ""
        hours = profile?.hours ?: ""
        description = profile?.description ?: ""
        locations = profile?.locations ?: emptyList()
    }

    // Show success/error messages
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully!",
                duration = SnackbarDuration.Short
            )
            // Clear selected image URIs to force refresh from server
            selectedProfileImageUri = null
            selectedBackgroundImageUri = null
            viewModel.resetUpdateStatus()
            // Reload profile to get updated data with new image URLs
            viewModel.loadProfessionalProfile(professionalId)
            // Wait for profile to reload before navigating back
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    LaunchedEffect(updateError) {
        updateError?.let { error ->
            snackbarHostState.showSnackbar(
                message = "Error: $error",
                duration = SnackbarDuration.Long
            )
            viewModel.resetUpdateStatus()
        }
    }

    // Image pickers
    val profileImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedProfileImageUri = it
        }
    }

    val backgroundImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedBackgroundImageUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("ProfileSettings", "Save button clicked")
                            Log.d("ProfileSettings", "Phone: $phone, Hours: $hours, Description: $description, Locations: ${locations.size}")
                            
                            // Convert URIs to Files with MIME types
                            val profilePictureFileWithMime = selectedProfileImageUri?.let { uri ->
                                Log.d("ProfileSettings", "Converting profile image URI to file: $uri")
                                val fileWithMime = FileUtil.getFileWithMime(context, uri)
                                if (fileWithMime == null) {
                                    Log.e("ProfileSettings", "Failed to convert profile image URI to file")
                                } else {
                                    Log.d("ProfileSettings", "Profile image file: ${fileWithMime.file.name}, MIME: ${fileWithMime.mimeType}")
                                }
                                fileWithMime
                            }
                            val backgroundImageFileWithMime = selectedBackgroundImageUri?.let { uri ->
                                Log.d("ProfileSettings", "Converting background image URI to file: $uri")
                                val fileWithMime = FileUtil.getFileWithMime(context, uri)
                                if (fileWithMime == null) {
                                    Log.e("ProfileSettings", "Failed to convert background image URI to file")
                                } else {
                                    Log.d("ProfileSettings", "Background image file: ${fileWithMime.file.name}, MIME: ${fileWithMime.mimeType}")
                                }
                                fileWithMime
                            }

                            Log.d("ProfileSettings", "Calling updateProfile...")
                            // Call updateProfile with all data
                            viewModel.updateProfile(
                                professionalId = professionalId,
                                phone = phone.takeIf { it.isNotBlank() },
                                hours = hours.takeIf { it.isNotBlank() },
                                address = null, // Can be added later if needed
                                description = description.takeIf { it.isNotBlank() },
                                profilePictureFile = profilePictureFileWithMime?.file,
                                profilePictureMimeType = profilePictureFileWithMime?.mimeType,
                                backgroundImageFile = backgroundImageFileWithMime?.file,
                                backgroundImageMimeType = backgroundImageFileWithMime?.mimeType,
                                locations = locations.takeIf { it.isNotEmpty() }
                            )
                            Log.d("ProfileSettings", "updateProfile called")
                        },
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, "Save Changes", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF59E0B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Profile Picture",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFFF59E0B), CircleShape)
                                .clickable { profileImagePicker.launch("image/*") }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(
                                        selectedProfileImageUri?.toString() 
                                            ?: profile?.profilePictureUrl?.let { BaseUrlProvider.getFullImageUrl(it) }
                                            ?: "https://ui-avatars.com/api/?name=${profile?.fullName?.replace(" ", "+") ?: "Professional"}&background=F59E0B&color=fff&size=200"
                                    )
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        TextButton(onClick = { profileImagePicker.launch("image/*") }) {
                            Text("Change Profile Picture", color = Color(0xFFF59E0B))
                        }
                    }
                }
            }

            // Background Image Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Background Image",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                                .clickable { backgroundImagePicker.launch("image/*") }
                        ) {
                            // Show selected image if available, otherwise show background image from server
                            val backgroundImageUrl = if (selectedBackgroundImageUri != null) {
                                selectedBackgroundImageUri.toString()
                            } else {
                                profile?.imageUrl?.let { BaseUrlProvider.getFullImageUrl(it) }
                                    ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800"
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(backgroundImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Background",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        TextButton(onClick = { backgroundImagePicker.launch("image/*") }) {
                            Text("Change Background Image", color = Color(0xFFF59E0B))
                        }
                    }
                }
            }

            // Description Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Description",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            placeholder = { Text("Enter description about your business") },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937),
                                focusedPlaceholderColor = Color(0xFF9CA3AF),
                                unfocusedPlaceholderColor = Color(0xFF9CA3AF)
                            )
                        )
                    }
                }
            }

            // Phone Number Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Phone Number",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter phone number") },
                            leadingIcon = {
                                Icon(Icons.Filled.Phone, null, tint = Color(0xFFF59E0B))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Working Hours Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Working Hours",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 9:00 AM - 5:00 PM") },
                            leadingIcon = {
                                Icon(Icons.Filled.Schedule, null, tint = Color(0xFFF59E0B))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedTextColor = Color(0xFF1F2937),
                                unfocusedTextColor = Color(0xFF1F2937)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(
                            "Example: 9:00 AM - 5:00 PM, Monday to Friday",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Locations Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Locations",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Button(
                                onClick = {
                                    editingLocationIndex = null
                                    showMapPicker = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Location")
                            }
                        }

                        if (locations.isEmpty()) {
                            Text(
                                "No locations added. Click 'Add Location' to add one.",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            locations.forEachIndexed { index, location ->
                                LocationItem(
                                    location = location,
                                    onEdit = {
                                        editingLocationIndex = index
                                        showMapPicker = true
                                    },
                                    onDelete = {
                                        locations = locations.filterIndexed { i, _ -> i != index }
                                    }
                                )
                                if (index < locations.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        val initialLocation = editingLocationIndex?.let { locations[it] }?.let {
            LocationData(it.lat, it.lon, it.name ?: "")
        }

        MapPickerScreen(
            initialLocation = initialLocation,
            onLocationSelected = { locationData ->
                val newLocation = ProfessionalLocation(
                    name = locationData.name,
                    address = null, // Can be set from reverse geocoding
                    lat = locationData.latitude,
                    lon = locationData.longitude
                )
                editingLocationIndex?.let { index ->
                    locations = locations.toMutableList().apply {
                        this[index] = newLocation
                    }
                } ?: run {
                    locations = locations + newLocation
                }
                showMapPicker = false
                editingLocationIndex = null
            },
            onDismiss = {
                showMapPicker = false
                editingLocationIndex = null
            }
        )
    }
}

@Composable
fun LocationItem(
    location: ProfessionalLocation,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            location.name?.let { name ->
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
            }
            location.address?.let { address ->
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            } ?: run {
                Text(
                    text = "Lat: ${location.lat}, Lon: ${location.lon}",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, "Edit", tint = Color(0xFFF59E0B))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

