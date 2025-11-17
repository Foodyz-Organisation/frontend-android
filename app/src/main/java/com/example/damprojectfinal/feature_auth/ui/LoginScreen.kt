package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Fastfood
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.utils.LoginViewModelFactory
import com.example.damprojectfinal.feature_auth.viewmodels.LoginViewModel
import com.example.damprojectfinal.R
import com.example.damprojectfinal.UserRoutes
import com.example.damprojectfinal.AuthRoutes
import kotlinx.coroutines.launch

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
    // ✅ Utilise le tokenManager passé en paramètre
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authApiService, tokenManager)
    )

    val uiState = viewModel.uiState
    val userRole by viewModel.userRole.collectAsState(initial = null)
    var showPassword by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Handle Login Navigation & Feedback ---
    LaunchedEffect(uiState.loginSuccess, uiState.error, userRole) {
        val role = userRole

        if (uiState.loginSuccess && !uiState.userId.isNullOrEmpty()) {

            val destinationRoute: String? = when (role) {
                "professional" -> "${UserRoutes.HOME_SCREEN_PRO}/${uiState.userId}"
                "user" -> UserRoutes.HOME_SCREEN
                else -> null
            }

            if (destinationRoute == null) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Login failed: User role '$role' is unsupported for navigation.",
                        actionLabel = "Error",
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetState()
                return@LaunchedEffect
            }

            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Login Successful! Welcome ${role?.replaceFirstChar { it.uppercase() } ?: "User"}",
                    actionLabel = "Continue",
                    duration = SnackbarDuration.Short
                )
            }

            navController.navigate(destinationRoute) {
                popUpTo(AuthRoutes.LOGIN) { inclusive = true }
            }
            viewModel.resetState()

        } else if (uiState.error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.error,
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.resetState()
        }
    }

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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // --- App Logo ---
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFECB3), Color(0xFFFFC107)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        contentDescription = "App Logo",
                        tint = Color(0xFF5F370E),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB87300),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Login to continue your food journey",
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
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    placeholder = { Text("your.email@example.com") },
                    singleLine = true,
                    isError = uiState.error != null && uiState.email.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Password Field ---
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    placeholder = { Text("Enter your password") },
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
                    onClick = viewModel::login,
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
                                text = "Login",
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social login divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    Text(
                        text = "  Or continue with  ",
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
                                painter = painterResource(id = R.drawable.google),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = onGoogleSignIn,
                        modifier = Modifier.weight(1f)
                    )

                    SocialButton(
                        text = "Facebook",
                        icon = {
                            Icon(
                                Icons.Filled.Facebook,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = onFacebookSignIn,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Signup link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Don't have an account? ", color = Color(0xFF6B7280))
                    TextButton(onClick = onNavigateToSignup) {
                        Text(
                            text = "Register Now",
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
private fun SocialButton(
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