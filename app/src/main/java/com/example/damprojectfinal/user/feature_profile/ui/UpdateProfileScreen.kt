package com.example.damprojectfinal.user.feature_profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.* // Make sure to import all required runtime features
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.core.`object`.FileUtil
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // --- 1. Collect ViewModel state ---
    val userResponse by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val initialProfile = userResponse

    // --- 2. Local UI state ---
    var username by remember { mutableStateOf(initialProfile?.username ?: "") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "") }
    var address by remember { mutableStateOf(initialProfile?.address ?: "") }

    var isActive by remember { mutableStateOf(initialProfile?.isActive ?: true) }
    
    // Profile photo state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                // Convert Uri to File and upload
                val fileWithMime = FileUtil.getFileWithMime(context, it)
                fileWithMime?.let { (file, _) ->
                    viewModel.uploadProfileImage(file)
                }
            }
        }
    )
    
    // Show success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }

    // --- 3. Sync initial state ---
    LaunchedEffect(initialProfile) {
        initialProfile?.let {
            username = it.username
            phone = it.phone ?: ""
            address = it.address ?: ""
            isActive = it.isActive
        }
    }

    // --- 4. TextField styling ---
    val primaryLightYellow = Color(0xFFFFD60A)
    val secondaryDarkText = Color(0xFF374151)
    val profileSecondary = Color(0xFF6B7280) // Gray for secondary text
    val placeholderText = Color(0xFFAAAAAA)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = primaryLightYellow,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        cursorColor = primaryLightYellow,
        focusedLabelColor = secondaryDarkText,
        unfocusedLabelColor = secondaryDarkText,
        focusedTextColor = secondaryDarkText,
        unfocusedTextColor = secondaryDarkText,
        focusedPlaceholderColor = placeholderText,
        unfocusedPlaceholderColor = placeholderText
    )

    // --- 5. Save changes handler ---
    val onSaveChanges: () -> Unit = {
        val updateRequest = UpdateUserRequest(
            username = username.takeIf { it != initialProfile?.username },
            phone = phone.takeIf { it != initialProfile?.phone },
            address = address.takeIf { it != initialProfile?.address },
            isActive = isActive.takeIf { it != initialProfile?.isActive }
        )

        // Only proceed if at least one field is changed
        if (listOf(
                updateRequest.username,
                updateRequest.phone,
                updateRequest.address,
                updateRequest.isActive
            ).any { it != null }) {

            // Get token safely
            val token = TokenManager(context).getAccessTokenBlocking()
            if (!token.isNullOrBlank()) {
                // Call ViewModel update
                viewModel.updateProfile(updateRequest, token)
            }
        }
    }

    // --- 6. Scaffold / UI ---
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = secondaryDarkText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = secondaryDarkText)
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            color = primaryLightYellow
                        )
                    } else {
                        TextButton(
                            onClick = onSaveChanges,
                            enabled = !isLoading && initialProfile != null
                        ) {
                            Text("Save", color = primaryLightYellow, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Update Account Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryDarkText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            errorMessage?.let { error ->
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            
            // Profile Photo Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Profile Photo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryDarkText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, primaryLightYellow, CircleShape)
                            .background(Color.White)
                            .clickable(enabled = !isLoading) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            initialProfile?.profilePictureUrl != null -> {
                                AsyncImage(
                                    model = BaseUrlProvider.getFullImageUrl(initialProfile.profilePictureUrl),
                                    contentDescription = "Current profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    placeholder = rememberVectorPainter(Icons.Default.Person),
                                    error = rememberVectorPainter(Icons.Default.Person)
                                )
                            }
                            else -> {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Add profile photo",
                                    modifier = Modifier.size(60.dp),
                                    tint = profileSecondary
                                )
                            }
                        }
                        
                        // Edit icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(primaryLightYellow)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change photo",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tap to change profile photo",
                        fontSize = 14.sp,
                        color = profileSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            CustomOutlinedTextField(username, { username = it }, "Username", "Enter your new username", Icons.Filled.Person, colors = textFieldColors, enabled = !isLoading)
            Spacer(Modifier.height(16.dp))
            CustomOutlinedTextField(phone, { phone = it }, "Phone Number", "e.g., +1 123-456-7890", Icons.Filled.Phone, keyboardType = KeyboardType.Phone, colors = textFieldColors, enabled = !isLoading)
            Spacer(Modifier.height(16.dp))
            CustomOutlinedTextField(address, { address = it }, "Address", "Full address including city/zip", Icons.Filled.LocationOn, singleLine = false, colors = textFieldColors, enabled = !isLoading)
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = primaryLightYellow)
                    Spacer(Modifier.width(16.dp))
                    Text("Account Active Status", fontWeight = FontWeight.SemiBold, color = secondaryDarkText)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = primaryLightYellow, checkedThumbColor = Color.White),
                    enabled = !isLoading
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onSaveChanges,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading && initialProfile != null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFD60A), Color(0xFFF59E0B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------------
// --- Private Composable Helpers (Updated to accept 'isError') ---
// ---------------------------------------------------------------------

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    colors: TextFieldColors,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        enabled = enabled
    )
}
