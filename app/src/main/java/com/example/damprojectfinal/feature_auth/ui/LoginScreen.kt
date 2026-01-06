package com.example.damprojectfinal.feature_auth.ui

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.R
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.feature_auth.viewmodels.LoginViewModel
import kotlinx.coroutines.launch
import com.example.damprojectfinal.core.utils.ViewModelFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authApiService: AuthApiService,
    tokenManager: TokenManager,
    onNavigateToForgetPassword: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onFacebookSignIn: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {}
) {
    // Get Application context needed for AndroidViewModel
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Initialize Legacy Google Auth Helper
    val legacyGoogleAuthHelper = remember { com.example.damprojectfinal.core.utils.LegacyGoogleAuthHelper(context) }
    val scope = rememberCoroutineScope()
    
    // Initialize NotificationManager for FCM token sync
    val notificationManager = remember { 
        com.example.damprojectfinal.core.utils.NotificationManager(context, tokenManager) 
    }
    
    // Instantiate ViewModel with the correct factory
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory {
            LoginViewModel(authApiService, tokenManager, notificationManager)
        }
    )

    val uiState = viewModel.uiState
    val userRole by viewModel.userRole.collectAsState(initial = null)
    var showPassword by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Activity Result Launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        legacyGoogleAuthHelper.handleSignInResult(
            data = result.data,
            onSuccess = { accountInfo ->
                viewModel.loginWithGoogle(
                    idToken = accountInfo.idToken,
                    email = accountInfo.email,
                    displayName = accountInfo.displayName,
                    profilePictureUrl = accountInfo.profilePictureUrl
                )
            },
            onError = { errorMessage ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        )
    }

    // --- Handle Login Navigation & Feedback ---
    LaunchedEffect(uiState.loginSuccess, uiState.error) {
        if (uiState.loginSuccess) {
            val role = uiState.role
            val userId = uiState.userId

            if (userId.isNullOrEmpty()) {
                snackbarHostState.showSnackbar(
                    message = "Login failed: User ID not found.",
                    duration = SnackbarDuration.Long
                )
                viewModel.resetState()
                return@LaunchedEffect
            }

            val destinationRoute: String? = when (role) {
                "PROFESSIONAL" -> "${UserRoutes.HOME_SCREEN_PRO}/${uiState.userId}"
                "USER" -> UserRoutes.HOME_SCREEN
                else -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Login failed: User role '$role' is unsupported for navigation.",
                            actionLabel = "Error",
                            duration = SnackbarDuration.Long
                        )
                    }
                    viewModel.resetState()
                    null
                }
            }

            // Defensive token save
            val access = uiState.accessToken
            val refresh = uiState.refreshToken
            if (!access.isNullOrEmpty() && !refresh.isNullOrEmpty()
                && !uiState.userId.isNullOrEmpty() && !uiState.role.isNullOrEmpty()
            ) {
                try {
                    TokenManager(context).saveTokens(
                        access,
                        refresh,
                        uiState.userId!!,
                        uiState.role!!
                    )
                } catch (e: Exception) {
                    println("Failed to save tokens: ${e.message}")
                }
            }

            destinationRoute?.let { route ->
                navController.navigate(route) {
                    popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }

            viewModel.resetState()
        } else if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Unknown error",
                actionLabel = "Dismiss"
            )
            viewModel.resetState()
        }
    }

    // --- User Not Found Popup ---
    if (uiState.userNotFound) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = {
                Text(text = "Account doesn't exist", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This Google account is not registered. Would you like to create an account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetState()
                        onNavigateToSignup()
                    }
                ) {
                    Text("Create Account", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            containerColor = Color.White,
             icon = {
                 Icon(
                     painter = painterResource(id = R.drawable.google), // Using app logo or warning icon
                     contentDescription = null,
                     modifier = Modifier.size(48.dp),
                     tint = Color.Unspecified
                 )
             }
        )
    }

    // --- Validation Error Dialog ---
    if (uiState.validationError != null) {
        com.example.damprojectfinal.core.ui.ValidationErrorDialog(
            errorMessage = uiState.validationError!!,
            onDismiss = { viewModel.resetState() }
        )
    }

    // UI Configuration
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFFAFAFA),
        unfocusedContainerColor = Color(0xFFFAFAFA),
        disabledContainerColor = Color(0xFFFAFAFA),
        focusedIndicatorColor = Color(0xFFF59E0B),
        unfocusedIndicatorColor = Color(0xFFEEEEEE), // Clear separator
        cursorColor = Color(0xFFF59E0B),
        focusedLabelColor = Color(0xFFF59E0B),
        unfocusedLabelColor = Color(0xFF9CA3AF)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFBEA) // Very light yellow tint background to match Signup
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // LAYER 1: Header Background & Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.auth_login_illustration),
                    contentDescription = "Login Illustration",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 50.dp), // Lift image slightly
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            // LAYER 2: "Sheet" Content
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 220.dp) // Overlap the illustration
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pull Handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.LightGray)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Welcome Back",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB87300)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Login to continue your food journey",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF6B7280)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- Email Field ---
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = Color(0xFFF59E0B)) },
                        placeholder = { Text("your.email@example.com") },
                        singleLine = true,
                        isError = uiState.error != null && uiState.email.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors.run { 
                            // Convert TextFieldDefaults.colors to OutlinedTextFieldDefaults.colors approximation for OutlinedTextField
                             OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFFEEEEEE),
                                cursorColor = Color(0xFFF59E0B),
                                focusedLabelColor = Color(0xFFF59E0B),
                                unfocusedLabelColor = Color(0xFF9CA3AF)
                             )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFFF59E0B)) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = uiState.error != null && uiState.password.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            cursorColor = Color(0xFFF59E0B),
                            focusedLabelColor = Color(0xFFF59E0B),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- Remember Me & Forgot Password ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMeChecked,
                                onCheckedChange = { rememberMeChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFB87300),
                                    uncheckedColor = Color(0xFF6B7280)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remember Me", color = Color(0xFF6B7280), fontSize = 14.sp)
                        }

                        TextButton(onClick = onNavigateToForgetPassword) {
                            Text("Forgot Password?", color = Color(0xFFB87300))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- Login Button ---
                    Button(
                        onClick = { viewModel.login() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFCC00),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFFFFCC00).copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Login",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Social Login Section
                    LoginSocialFooter(
                        onGoogleClick = {
                            // Sign out first to force account picker to show
                            legacyGoogleAuthHelper.signOut {
                                val signInIntent = legacyGoogleAuthHelper.getSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            }
                        },
                        onFacebookClick = onFacebookSignIn,
                        onNavigateToSignup = onNavigateToSignup
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LoginSocialFooter(
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                "  Or continue with  ",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodyMedium
            )
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialButton(
                text = "Google",
                icon = {
                    Icon(
                        painterResource(id = R.drawable.google),
                        null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                },
                onClick = onGoogleClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? ", color = Color(0xFF6B7280))
            TextButton(onClick = onNavigateToSignup) {
                Text(
                    "Register Now",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color(0xFF374151))
        }
    }
}