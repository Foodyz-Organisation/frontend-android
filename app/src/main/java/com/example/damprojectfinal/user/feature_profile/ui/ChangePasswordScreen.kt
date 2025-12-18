package com.example.damprojectfinal.user.feature_profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel

// Color constants
private val primaryLightYellow = Color(0xFFFFD60A)
private val secondaryDarkText = Color(0xFF374151)
private val placeholderText = Color(0xFFAAAAAA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    
    // Password fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Password visibility
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show success snackbar
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Password updated successfully!",
                duration = SnackbarDuration.Short
            )
            // Navigate back after a short delay
            kotlinx.coroutines.delay(1500)
            onBackClick()
        }
    }
    
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
    
    val onSavePassword: () -> Unit = {
        passwordError = null
        
        // Validation
        when {
            currentPassword.isBlank() -> {
                passwordError = "Current password is required"
            }
            newPassword.isBlank() -> {
                passwordError = "New password is required"
            }
            newPassword.length < 6 -> {
                passwordError = "New password must be at least 6 characters"
            }
            newPassword != confirmPassword -> {
                passwordError = "Passwords do not match"
            }
            else -> {
                // Update password
                val token = TokenManager(context).getAccessTokenBlocking()
                if (!token.isNullOrBlank()) {
                    val updateRequest = UpdateUserRequest(password = newPassword)
                    viewModel.updateProfile(updateRequest, token)
                    
                    // Clear fields
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                } else {
                    passwordError = "Failed to get authentication token"
                }
            }
        }
    }
    
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
                title = {
                    Text(
                        "Change Password",
                        fontWeight = FontWeight.Bold,
                        color = secondaryDarkText
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = secondaryDarkText
                        )
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
                            onClick = onSavePassword,
                            enabled = passwordError == null
                        ) {
                            Text(
                                "Save",
                                color = primaryLightYellow,
                                fontWeight = FontWeight.SemiBold
                            )
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
                "Update Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryDarkText,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(8.dp))
            
            (errorMessage ?: passwordError)?.let { error ->
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Current Password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; passwordError = null },
                label = { Text("Current Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            if (showCurrentPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                enabled = !isLoading
            )
            
            Spacer(Modifier.height(16.dp))
            
            // New Password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; passwordError = null },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                enabled = !isLoading,
                isError = passwordError != null
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                enabled = !isLoading,
                isError = passwordError != null
            )
            
            Spacer(Modifier.height(40.dp))
            
            // Save Button
            Button(
                onClick = onSavePassword,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading && passwordError == null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFD60A), Color(0xFFF59E0B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Update Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

