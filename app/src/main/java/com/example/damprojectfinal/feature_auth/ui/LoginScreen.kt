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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    tokenManager: TokenManager,  // ✅ Reçoit TokenManager depuis AppNavigation
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
    
    // ⭐ Initialize NotificationManager for FCM token sync
    val notificationManager = remember { 
        com.example.damprojectfinal.core.utils.NotificationManager(context, tokenManager) 
    }
    
    // Instantiate ViewModel with the correct factory
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory {
            LoginViewModel(authApiService, tokenManager, notificationManager)
        }
    )

    val uiState = viewModel.uiState // Observe uiState directly
    val userRole by viewModel.userRole.collectAsState(initial = null)
    var showPassword by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Activity Result Launcher for Google Sign-In (must be after viewModel and snackbarHostState)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        legacyGoogleAuthHelper.handleSignInResult(
            data = result.data,
            onSuccess = { accountInfo ->
                // Handle successful sign-in
                viewModel.loginWithGoogle(
                    idToken = accountInfo.idToken,
                    email = accountInfo.email,
                    displayName = accountInfo.displayName,
                    profilePictureUrl = accountInfo.profilePictureUrl
                )
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



    // --- Handle Login Navigation & Feedback ---
    // Now observes uiState.loginSuccess, uiState.error, and uiState.role (which is the formatted role)

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

            // --- Navigation Logic Using UserRoutes constants (uses formatted roles) ---
                val destinationRoute: String? = when (role) {
                    "PROFESSIONAL" -> "${UserRoutes.HOME_SCREEN_PRO}/${uiState.userId}"
                    "USER" -> UserRoutes.HOME_SCREEN
                    else -> {
                    // Fallback: unsupported role -> show error and stop
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

            // Defensive token save (ViewModel already does this, but we keep a guard here)
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
            // 🔴 Login failed → show error snackbar
                snackbarHostState.showSnackbar(
                    message = uiState.error ?: "Unknown error",
                    actionLabel = "Dismiss"
                )
                viewModel.resetState()
        }
    }

    // UI Code - moved outside LaunchedEffect
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFBEA), Color(0xFFFFF8D6), Color(0xFFFFF6C1))
    )

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White.copy(alpha = 0.5f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
        disabledContainerColor = Color.White.copy(alpha = 0.3f),
        focusedIndicatorColor = Color(0xFFF59E0B),
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFFB87300),
        focusedLabelColor = Color(0xFFB87300),
        unfocusedLabelColor = Color(0xFF6B7280)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- App Logo ---
                Box(
                    modifier = Modifier
                        .size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_name),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(150.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Welcome Back",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB87300),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Login to continue your food journey",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- Email Field ---
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    placeholder = { Text("your.email@example.com") },
                    singleLine = true,
                    isError = uiState.error != null && uiState.email.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                null
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = uiState.error != null && uiState.password.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors
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
                    onClick = {
                        android.util.Log.d("LoginScreen", "Login button clicked")
                        android.util.Log.d("LoginScreen", "Email: ${uiState.email}, Password length: ${uiState.password.length}")
                        android.util.Log.d("LoginScreen", "IsLoading: ${uiState.isLoading}, Button enabled: ${!uiState.isLoading}")
                        try {
                            viewModel.login()
                            android.util.Log.d("LoginScreen", "viewModel.login() called successfully")
                        } catch (e: Exception) {
                            android.util.Log.e("LoginScreen", "Error calling login: ${e.message}", e)
                            e.printStackTrace()
                        }
                    },
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
                            CircularProgressIndicator(color = Color(0xFF111827))
                        } else {
                            Text(
                                "Login",
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social Login Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    Text(
                        "  Or continue with  ",
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodyLarge
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
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            // Launch legacy Google Sign-In (browser-based)
                            val signInIntent = legacyGoogleAuthHelper.getSignInIntent()
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SocialButton(
                        text = "Facebook",
                        icon = {
                            Icon(
                                Icons.Filled.Facebook,
                                null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = onFacebookSignIn,
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