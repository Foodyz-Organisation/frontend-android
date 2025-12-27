package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.R
import com.example.damprojectfinal.feature_auth.viewmodels.SignupViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import com.airbnb.lottie.compose.*

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
    
    // Steps: 0 -> Personal, 1 -> Phone, 2 -> Address
    var currentStep by remember { mutableIntStateOf(0) }
    
    var showPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Activity Result Launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        legacyGoogleAuthHelper.handleSignInResult(
            data = result.data,
            onSuccess = { accountInfo ->
                accountInfo.email?.let { viewModel.updateEmail(it) }
                accountInfo.displayName?.let { viewModel.updateUsername(it) }
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Google account connected! Please complete registration.",
                        duration = SnackbarDuration.Short
                    )
                }
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

    // Success Animation
    val successComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.accountcreated))
    val successProgress by animateLottieCompositionAsState(
        composition = successComposition,
        isPlaying = uiState.signupSuccess
    )

    // Navigation Effect - Wait for Animation
    LaunchedEffect(uiState.signupSuccess, successProgress, uiState.error) {
        if (uiState.signupSuccess && successProgress == 1f) {
            snackbarHostState.showSnackbar(
                message = "Registration Successful! Welcome to Foodyz.",
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFBEA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HEADER AREA (Dynamic based on Step)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter)
            ) {
                AnimatedContent(targetState = currentStep, label = "HeaderAnimation") { step ->
                    when (step) {
                        0 -> {
                            // Step 1: Default Illustration
                            Image(
                                painter = painterResource(id = R.drawable.auth_signup_illustration),
                                contentDescription = "Signup Illustration",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 50.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        1 -> {
                            // Step 2: Phone Animation
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.phonecall))
                            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 50.dp, top = 20.dp)
                            )
                        }
                        2 -> {
                            // Step 3: Address Animation
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.locationfinding))
                            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 50.dp, top = 20.dp)
                            )
                        }
                    }
                }
            }

            // CONTENT CARD
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 220.dp)
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
                    
                    Spacer(Modifier.height(24.dp))

                    // Title Changes based on Step
                    Text(
                        text = when (currentStep) {
                            0 -> "Create Account"
                            1 -> "Add Phone Number"
                            2 -> "Add Address"
                            else -> ""
                        },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = when (currentStep) {
                            0 -> "Enter your personal details"
                            1 -> "So we can contact you"
                            2 -> "Where should we deliver?"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF6B7280)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    // FORM CONTENT WITH ANIMATION
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "FormContent"
                    ) { step ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (step) {
                                0 -> PersonalDataStep(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    confirmPassword = confirmPassword,
                                    onConfirmPasswordChange = {
                                        confirmPassword = it
                                        confirmPasswordError = if (it != uiState.password) "Passwords do not match" else null
                                    },
                                    confirmPasswordError = confirmPasswordError,
                                    showPassword = showPassword,
                                    showConfirmPassword = showConfirmPassword,
                                    onTogglePassword = { showPassword = !showPassword },
                                    onToggleConfirmPassword = { showConfirmPassword = !showConfirmPassword },
                                    onNext = {
                                        if (uiState.username.isNotBlank() && uiState.email.isNotBlank() && uiState.password.isNotBlank() && confirmPasswordError == null) {
                                            if (uiState.password == confirmPassword) {
                                                currentStep = 1
                                            } else {
                                                confirmPasswordError = "Passwords do not match"
                                            }
                                        } else {
                                             scope.launch { snackbarHostState.showSnackbar("Please fill all required fields correctly.") }
                                        }
                                    }
                                )
                                1 -> PhoneStep(
                                    phone = uiState.phone,
                                    onPhoneChange = viewModel::updatePhone,
                                    onBack = { currentStep = 0 },
                                    onNext = { currentStep = 2 }
                                )
                                2 -> AddressStep(
                                    address = uiState.address,
                                    onAddressChange = viewModel::updateAddress,
                                    isLoading = uiState.isLoading,
                                    onBack = { currentStep = 1 },
                                    onSubmit = { viewModel.signupUser() }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Social Footer (Only on Step 0)
                    if (currentStep == 0) {
                        SocialFooter(
                            onGoogleClick = {
                                val signInIntent = legacyGoogleAuthHelper.getSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            },
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }

            // SUCCESS ANIMATION OVERLAY
            if (uiState.signupSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(enabled = false) {}, // Block interactions
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LottieAnimation(
                            composition = successComposition,
                            progress = { successProgress },
                            modifier = Modifier.size(300.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Account Created!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCC00)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalDataStep(
    uiState: com.example.damprojectfinal.feature_auth.viewmodels.SignupUiState,
    viewModel: SignupViewModel,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    showPassword: Boolean,
    showConfirmPassword: Boolean,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onNext: () -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFFAFAFA),
        unfocusedContainerColor = Color(0xFFFAFAFA),
        focusedBorderColor = Color(0xFFFFCC00),
        unfocusedBorderColor = Color(0xFFEEEEEE),
        cursorColor = Color(0xFFFFCC00),
        focusedLabelColor = Color(0xFFFFCC00),
        unfocusedLabelColor = Color(0xFF9CA3AF)
    )

    // Full Name
    OutlinedTextField(
        value = uiState.username,
        onValueChange = viewModel::updateUsername,
        label = { Text("Full Name") },
        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFFFFCC00)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true
    )

    Spacer(Modifier.height(16.dp))

    // Email
    OutlinedTextField(
        value = uiState.email,
        onValueChange = viewModel::updateEmail,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFFFFCC00)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true
    )

    Spacer(Modifier.height(16.dp))

    // Password
    OutlinedTextField(
        value = uiState.password,
        onValueChange = viewModel::updatePassword,
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFFFCC00)) },
        trailingIcon = {
            IconButton(onClick = onTogglePassword) {
                Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
    )

    Spacer(Modifier.height(16.dp))

    // Confirm Password
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm Password") },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFFFCC00)) },
        trailingIcon = {
            IconButton(onClick = onToggleConfirmPassword) {
                Icon(if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true,
        isError = confirmPasswordError != null,
        supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onNext,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFCC00),
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PhoneStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFFAFAFA),
        unfocusedContainerColor = Color(0xFFFAFAFA),
        focusedBorderColor = Color(0xFFFFCC00),
        unfocusedBorderColor = Color(0xFFEEEEEE),
        cursorColor = Color(0xFFFFCC00),
        focusedLabelColor = Color(0xFFFFCC00),
        unfocusedLabelColor = Color(0xFF9CA3AF)
    )

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = { Text("Phone Number") },
        leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFFFFCC00)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true
    )

    Spacer(Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text("Back")
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFCC00),
                contentColor = Color.Black
            )
        ) {
            Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddressStep(
    address: String,
    onAddressChange: (String) -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFFAFAFA),
        unfocusedContainerColor = Color(0xFFFAFAFA),
        focusedBorderColor = Color(0xFFFFCC00),
        unfocusedBorderColor = Color(0xFFEEEEEE),
        cursorColor = Color(0xFFFFCC00),
        focusedLabelColor = Color(0xFFFFCC00),
        unfocusedLabelColor = Color(0xFF9CA3AF)
    )

    OutlinedTextField(
        value = address,
        onValueChange = onAddressChange,
        label = { Text("Address") },
        leadingIcon = { Icon(Icons.Default.Home, null, tint = Color(0xFFFFCC00)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = textFieldColors,
        singleLine = true
    )

    Spacer(Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(
            onClick = onBack,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text("Back")
        }

        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFCC00),
                contentColor = Color.Black
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SocialFooter(
    onGoogleClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            Text(
                text = "  or  ", 
                color = Color(0xFF9CA3AF), 
                style = MaterialTheme.typography.bodyMedium
            )
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F2937)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(12.dp))
            Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color(0xFF6B7280))
            TextButton(onClick = onNavigateToLogin) {
                Text("Login", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }
    }
}
