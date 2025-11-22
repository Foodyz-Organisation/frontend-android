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
import androidx.compose.runtime.* // Make sure to import all required runtime features
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.dto.user.UpdateUserRequest
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    // 1. Collect state from ViewModel
    val userResponse by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val initialProfile = userResponse

    // 2. Local UI state for input fields (Editable State)
    var username by remember { mutableStateOf(initialProfile?.username ?: "") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "") }
    var address by remember { mutableStateOf(initialProfile?.address ?: "") }

    // ⭐ PASSWORD FIELDS AND VALIDATION STATE ⭐
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var isActive by remember { mutableStateOf(initialProfile?.isActive ?: true) }
    var showPassword by remember { mutableStateOf(false) }

    // 3. Sync local state with ViewModel state upon initial load
    LaunchedEffect(initialProfile) {
        if (initialProfile != null) {
            username = initialProfile.username
            phone = initialProfile.phone ?: ""
            address = initialProfile.address ?: ""
            isActive = initialProfile.isActive
        }
    }

    // --- UI Styling ---
    val primaryLightYellow = Color(0xFFFFD60A)
    val secondaryDarkText = Color(0xFF374151)
    val placeholderText = Color(0xFFAAAAAA)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = primaryLightYellow,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        cursorColor = primaryLightYellow,
        focusedLabelColor = secondaryDarkText,
        unfocusedLabelColor = secondaryDarkText,
        unfocusedTextColor = secondaryDarkText,
        focusedTextColor = secondaryDarkText,
        unfocusedPlaceholderColor = placeholderText,
        focusedPlaceholderColor = placeholderText
    )

    // 4. Action Function
// In UpdateProfileScreen.kt

// 4. Action Function
    val onSaveChanges: () -> Unit = {
        // Wrap the core logic in a 'run' block that returns Unit (the default type)
        run {
            // Reset local password error
            passwordError = null

            // --- Password Validation ---
            val newPasswordEntered = password.isNotBlank()
            if (newPasswordEntered && password != confirmPassword) {
                passwordError = "Passwords do not match."
                return@run // ⭐ FIX: This returns only from the 'run' block, not the outer function/composable.
            }

            // --- Construct Request ---
            val updateRequest = UpdateUserRequest(
                username = if (username != initialProfile?.username) username else null,
                phone = if (phone != (initialProfile?.phone ?: "")) phone else null,
                address = if (address != (initialProfile?.address ?: "")) address else null,
                password = if (newPasswordEntered) password else null,
                isActive = if (isActive != initialProfile?.isActive) isActive else null
            )

            // Only proceed if there is data to update
            val token = "YOUR_ACCESS_TOKEN" // Replace with actual token retrieval

            if (listOf(updateRequest.username, updateRequest.phone, updateRequest.address, updateRequest.password, updateRequest.isActive).any { it != null }) {
                if (token.isNotBlank()) {
                    viewModel.updateProfile(updateRequest, token)
                    // Clear password fields immediately after sending the request
                    password = ""
                    confirmPassword = ""
                }
            } else {
                // Optionally show a snackbar: "No changes detected."
            }
        } // End of run block
    }
    // --- End of Action Function ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp).padding(end = 8.dp))
                    } else {
                        TextButton(
                            onClick = onSaveChanges,
                            enabled = !isLoading && initialProfile != null && passwordError == null
                        ) {
                            Text("Save")
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
                text = "Update Account Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryDarkText,
                modifier = Modifier.fillMaxWidth()
            )

            // Display ViewModel error or Password mismatch error
            if (errorMessage != null || passwordError != null) {
                val displayError = errorMessage ?: passwordError
                Text("Error: ${displayError!!}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // --- 1. Username Field ---
            CustomOutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                placeholder = "Enter your new username",
                icon = Icons.Filled.Person,
                colors = textFieldColors,
                enabled = !isLoading
            )

            Spacer(Modifier.height(16.dp))

            // --- 2. Phone Field ---
            CustomOutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "e.g., +1 123-456-7890",
                icon = Icons.Filled.Phone,
                keyboardType = KeyboardType.Phone,
                colors = textFieldColors,
                enabled = !isLoading
            )

            Spacer(Modifier.height(16.dp))

            // --- 3. Address Field ---
            CustomOutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address",
                placeholder = "Full address including city/zip",
                icon = Icons.Filled.LocationOn,
                singleLine = false,
                colors = textFieldColors,
                enabled = !isLoading
            )

            Spacer(Modifier.height(24.dp))

            // --- 4. New Password Field ---
            CustomPasswordTextField(
                value = password,
                onValueChange = { password = it; passwordError = null }, // Clear local error on change
                onToggleVisibility = { showPassword = !showPassword },
                label = "New Password (Leave blank to keep current)",
                placeholder = "New secure password",
                showPassword = showPassword,
                colors = textFieldColors,
                enabled = !isLoading,
                isError = passwordError != null // Highlight if error is set
            )

            Spacer(Modifier.height(16.dp))

            // ⭐ 5. Confirm Password Field ⭐
            CustomPasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null }, // Clear local error on change
                onToggleVisibility = { showPassword = !showPassword },
                label = "Confirm New Password",
                placeholder = "Re-enter new password",
                showPassword = showPassword,
                colors = textFieldColors,
                enabled = !isLoading,
                isError = passwordError != null // Highlight if error is set
            )

            Spacer(Modifier.height(32.dp))

            // --- 6. isActive Toggle ---
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
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active Status", tint = primaryLightYellow)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Account Active Status",
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryDarkText
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = primaryLightYellow,
                        checkedThumbColor = Color.White
                    ),
                    enabled = !isLoading
                )
            }

            Spacer(Modifier.height(40.dp))

            // 🟨 SAVE BUTTON
            Button(
                onClick = onSaveChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading && initialProfile != null && passwordError == null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD60A), Color(0xFFF59E0B))
                            )
                        ),
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

@Composable
private fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    label: String,
    placeholder: String,
    showPassword: Boolean,
    colors: TextFieldColors,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
            }
        },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        enabled = enabled,
        isError = isError // Apply error state
    )
}