package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.utils.ViewModelFactory
import com.example.damprojectfinal.feature_auth.viewmodels.ProSignupViewModel


// --- Dummy implementation for Preview ---
private val PreviewAuthApiService = AuthApiService()
// ----------------------------------------


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProSignupScreen(
    navController: NavHostController,
    authApiService: AuthApiService
) {
    val viewModel: ProSignupViewModel = viewModel(
        factory = ViewModelFactory {
            ProSignupViewModel(authApiService, navController)
        }
    )

    var showPassword by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Design colors
    val lightGrayBackground = Color(0xFFF3F4F6)
    val yellowPrimary = Color(0xFFFFC107)
    val yellowDark = Color(0xFFB87300)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            StepProgressIndicator(
                currentStep = viewModel.currentStep.value,
                totalSteps = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated content based on current step
            AnimatedContent(
                targetState = viewModel.currentStep.value,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> Step1ProfessionalDetails(
                        viewModel = viewModel,
                        showPassword = showPassword,
                        onShowPasswordChange = { showPassword = it },
                        lightGrayBackground = lightGrayBackground
                    )
                    2 -> Step2LicenseInfo(
                        viewModel = viewModel,
                        lightGrayBackground = lightGrayBackground
                    )
                    3 -> Step3Location(
                        viewModel = viewModel,
                        lightGrayBackground = lightGrayBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = if (viewModel.currentStep.value > 1) 
                    Arrangement.spacedBy(12.dp) else Arrangement.End
            ) {
                // Back Button
                if (viewModel.currentStep.value > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = yellowDark
                        ),
                        border = BorderStroke(1.5.dp, yellowDark)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Back",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Next/Complete Button
                Button(
                    onClick = { viewModel.nextStep() },
                    enabled = !viewModel.isLoading.value,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFE15A), yellowPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                color = Color(0xFF111827),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (viewModel.currentStep.value == 3) "Complete Registration" else "Continue",
                                    color = Color(0xFF111827),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (viewModel.currentStep.value == 3) 
                                        Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF111827),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Error Message
            viewModel.errorMessage.value?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Success Dialog
    if (viewModel.showSuccessDialog.value) {
        SuccessCelebrationDialog()
    }

    // OSM Location Picker
    if (viewModel.showLocationPicker.value) {
        OSMLocationPicker(
            initialLocation = viewModel.selectedLocation.value,
            onLocationSelected = { location ->
                viewModel.selectedLocation.value = location
                viewModel.showLocationPicker.value = false
            },
            onDismiss = {
                viewModel.showLocationPicker.value = false
            }
        )
    }
}

@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step == currentStep
            val isCompleted = step < currentStep

            Box(
                modifier = Modifier
                    .size(if (isActive) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF10B981)
                            isActive -> Color(0xFFFFC107)
                            else -> Color(0xFFE5E7EB)
                        }
                    )
                    .animateContentSize()
            )

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(
                            if (isCompleted) Color(0xFF10B981) else Color(0xFFE5E7EB)
                        )
                )
            }
        }
    }
}

@Composable
fun Step1ProfessionalDetails(
    viewModel: ProSignupViewModel,
    showPassword: Boolean,
    onShowPasswordChange: (Boolean) -> Unit,
    lightGrayBackground: Color
) {
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = lightGrayBackground,
        unfocusedContainerColor = lightGrayBackground,
        disabledContainerColor = lightGrayBackground.copy(alpha = 0.5f),
        focusedIndicatorColor = Color(0xFFF59E0B),
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFFB87300),
        focusedLabelColor = Color(0xFFB87300),
        unfocusedLabelColor = Color(0xFF6B7280)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon with enhanced animation
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFECB3), Color(0xFFFFC107))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = Color(0xFF5F370E),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic title
        Text(
            text = "Welcome to foodyz!",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB87300),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create your professional account",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Full Name
        OutlinedTextField(
            value = viewModel.fullName.value,
            onValueChange = { viewModel.fullName.value = it },
            label = { Text("Full Name / Business Contact") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Person, 
                    contentDescription = null,
                    tint = Color(0xFFB87300)
                ) 
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Email
        OutlinedTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Email Address") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Email, 
                    contentDescription = null,
                    tint = Color(0xFFB87300)
                ) 
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password
        OutlinedTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Password") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Lock, 
                    contentDescription = null,
                    tint = Color(0xFFB87300)
                ) 
            },
            trailingIcon = {
                IconButton(onClick = { onShowPasswordChange(!showPassword) }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            supportingText = {
                Text(
                    "Minimum 6 characters",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Confirm Password
        OutlinedTextField(
            value = viewModel.confirmPassword.value,
            onValueChange = { viewModel.confirmPassword.value = it },
            label = { Text("Confirm Password") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Lock, 
                    contentDescription = null,
                    tint = Color(0xFFB87300)
                ) 
            },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            isError = viewModel.confirmPassword.value.isNotEmpty() && 
                     viewModel.confirmPassword.value != viewModel.password.value,
            supportingText = {
                if (viewModel.confirmPassword.value.isNotEmpty() && 
                    viewModel.confirmPassword.value != viewModel.password.value) {
                    Text(
                        "Passwords do not match",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }
}

@Composable
fun Step2LicenseInfo(
    viewModel: ProSignupViewModel,
    lightGrayBackground: Color
) {
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = lightGrayBackground,
        unfocusedContainerColor = lightGrayBackground,
        disabledContainerColor = lightGrayBackground.copy(alpha = 0.5f),
        focusedIndicatorColor = Color(0xFFF59E0B),
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFFB87300),
        focusedLabelColor = Color(0xFFB87300),
        unfocusedLabelColor = Color(0xFF6B7280)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFDCFCE7), Color(0xFF10B981))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Business Verification",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB87300),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Build trust with your customers",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        // License Number
        OutlinedTextField(
            value = viewModel.licenseNumber.value,
            onValueChange = { viewModel.licenseNumber.value = it },
            label = { Text("Restaurant License Number") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Badge, 
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                ) 
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            supportingText = {
                Text(
                    "Optional - You can add this later",
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFB87300),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Your license number helps customers trust your business. You can skip this step and add it later from your profile.",
                    color = Color(0xFF78350F),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun Step3Location(
    viewModel: ProSignupViewModel,
    lightGrayBackground: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFDCFCE7), Color(0xFF10B981))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Almost there!",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB87300),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Help customers find your restaurant",
            color = Color(0xFF6B7280),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Location Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.selectedLocation.value != null) {
                    Color(0xFFDCFCE7)
                } else {
                    lightGrayBackground
                }
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restaurant Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewModel.selectedLocation.value != null) {
                                viewModel.selectedLocation.value!!.name.ifEmpty {
                                    "Location selected ✓"
                                }
                            } else {
                                "Tap to add your location"
                            },
                            fontSize = 14.sp,
                            color = if (viewModel.selectedLocation.value != null) {
                                Color(0xFF10B981)
                            } else {
                                Color(0xFF6B7280)
                            },
                            fontWeight = if (viewModel.selectedLocation.value != null) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.showLocationPicker.value = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.selectedLocation.value != null) {
                                Color(0xFF10B981)
                            } else {
                                Color(0xFFFFC107)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.selectedLocation.value != null)
                                Icons.Filled.Edit
                            else
                                Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.selectedLocation.value != null) "Change" else "Add",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFB87300),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Adding your location is optional but recommended. It helps customers discover your restaurant and improves your visibility.",
                    color = Color(0xFF78350F),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SuccessCelebrationDialog() {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFDCFCE7), Color(0xFF10B981))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to foodyz!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB87300),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your professional account has been created successfully. You can now start managing your restaurant!",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    color = Color(0xFFFFC107),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Redirecting to login...",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Pro Signup Screen Preview")
@Composable
fun ProSignupScreenPreview() {
    MaterialTheme {
        ProSignupScreen(
            navController = rememberNavController(),
            authApiService = PreviewAuthApiService
        )
    }
}