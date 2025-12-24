package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.feature_auth.viewmodels.SignupViewModel
import com.example.damprojectfinal.R
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = viewModel(),
    onGoogleSignIn: () -> Unit = {},
    onFacebookSignIn: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val legacyGoogleAuthHelper = remember { com.example.damprojectfinal.core.utils.LegacyGoogleAuthHelper(context) }
    val scope = rememberCoroutineScope()
    
    val uiState = viewModel.uiState
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // ✅ FIX: Removed the redundant inner remember {} block.
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Activity Result Launcher for Google Sign-In (must be after snackbarHostState)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        legacyGoogleAuthHelper.handleSignInResult(
            data = result.data,
            onSuccess = { accountInfo ->
                // Pre-fill registration form with Google data
                accountInfo.email?.let { viewModel.updateEmail(it) }
                accountInfo.displayName?.let { viewModel.updateUsername(it) }
                
                // Show success message
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Google account connected! Please complete registration.",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onError = { errorMessage ->
                // Show error to user
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        )
    }


    // --- Side Effect: Handle API Response and Navigation (Kept) ---
    LaunchedEffect(uiState.signupSuccess, uiState.error) {
        if (uiState.signupSuccess) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Registration Successful! Please log in.",
                actionLabel = "Login",
                duration = SnackbarDuration.Short
            )
            navController.navigate("login_route") {
                popUpTo("signup_route") { inclusive = true }
            }
            viewModel.resetState()
        } else if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error!!,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            viewModel.resetState()
        }
    }

    // 🎨 Custom Colors (Kept the perfect design)
    val primaryLightYellow = Color(0xFFFFD60A).copy(alpha = 0.6f)
    val secondaryDarkText = Color(0xFF6B7280)
    val placeholderText = Color(0xFFAAAAAA)

    // Custom text field colors (Kept the perfect design)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = primaryLightYellow,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        cursorColor = primaryLightYellow,
        focusedLabelColor = secondaryDarkText,
        unfocusedLabelColor = secondaryDarkText,
        unfocusedTextColor = placeholderText,
        focusedTextColor = secondaryDarkText,
        unfocusedPlaceholderColor = placeholderText,
        focusedPlaceholderColor = placeholderText
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))

            // --- App Logo ---
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo_name),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- Header (Updated text) ---
            Text(
                text = "Create Your Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryDarkText
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sign up to start your foodie journey",
                color = secondaryDarkText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // --- 1. ACCOUNT IDENTITY CATEGORY ---
            CategoryHeader("Identity")

            // Full Name (Updated icon)
            CustomOutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = "Full Name",
                placeholder = "John Doe",
                icon = Icons.Filled.Person, // 🔑 Updated Icon for User
                isError = uiState.error != null && uiState.username.isBlank(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            // Email
            CustomOutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = "Email Address",
                placeholder = "your.email@example.com",
                icon = Icons.Filled.Email,
                isError = uiState.error != null && uiState.email.isBlank(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(32.dp))

            // --- 2. SECURITY (PASSWORD) CATEGORY ---
            CategoryHeader("Security")

            // Password
            CustomPasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                placeholder = "Enter a secure password",
                showPassword = showPassword,
                onToggleVisibility = { showPassword = !showPassword },
                isError = uiState.error != null && uiState.password.isBlank(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            // Confirm Password
            CustomPasswordTextField(
                value = uiState.password,
                onValueChange = { /* Usually only validation check */ },
                onToggleVisibility = { showConfirmPassword = !showConfirmPassword },
                label = "Confirm Password",
                placeholder = "Re-enter password",
                showPassword = showConfirmPassword,
                isError = false,
                colors = textFieldColors
            )

            Spacer(Modifier.height(32.dp))

            // --- 3. CONTACT & ADDRESS CATEGORY (Optional) ---
            CategoryHeader("Contact & Location (Optional)")

            // Phone
            CustomOutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::updatePhone,
                label = "Phone Number",
                placeholder = "+1 234 567 890",
                icon = Icons.Filled.Phone,
                isError = false,
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            // Address
            CustomOutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::updateAddress,
                label = "Address",
                placeholder = "123 Main Street, City",
                icon = Icons.Filled.Home,
                isError = false,
                colors = textFieldColors
            )

            Spacer(Modifier.height(40.dp))

            // 🟨 SIGN UP BUTTON (Updated text)
            Button(
                onClick = viewModel::signupUser,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFE15A), Color(0xFFF59E0B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Social Login and Footer (Kept) ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(text = "  Or continue with  ", color = secondaryDarkText, style = MaterialTheme.typography.bodyLarge)
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialButton(
                    modifier = Modifier.weight(1f),
                    text = "Google",
                    icon = { Icon(painter = painterResource(id = R.drawable.google), contentDescription = null, tint = secondaryDarkText) },
                    onClick = {
                        // Launch legacy Google Sign-In (browser-based)
                        val signInIntent = legacyGoogleAuthHelper.getSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already have an account? ", color = secondaryDarkText)
                TextButton(onClick = onNavigateToLogin) {
                    Text(text = "Login", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------
// --- Private Composable Helpers (Kept as they were perfect) ---
// ---------------------------------------------------------------------

@Composable
private fun CategoryHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB87300),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean,
    colors: TextFieldColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors
    )
}

@Composable
private fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    onToggleVisibility: () -> Unit,
    label: String,
    placeholder: String,
    showPassword: Boolean,
    isError: Boolean,
    colors: TextFieldColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
            }
        },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = colors
    )
}
