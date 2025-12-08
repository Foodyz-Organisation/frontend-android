package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.foundation.background
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
// FIX: AuthApiService is a final class and cannot be extended, so we instantiate it directly.
private val PreviewAuthApiService = AuthApiService()
// ----------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
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

    // Internal state for UI only
    var showPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // --- Design Elements ---
    // 🎨 CHANGE: Defining a light gray color for the background of the text fields
    val lightGrayBackground = Color(0xFFF3F4F6) // A very light, subtle gray

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
    // --- End Design Elements ---

    // Using Box instead of Scaffold to control the full screen background
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Setting background to solid Color.White
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo/Icon Area
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            // Keeping the yellow/gold theme for the icon background
                            listOf(Color(0xFFFFECB3), Color(0xFFFFC107))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fastfood,
                    contentDescription = "App Logo",
                    tint = Color(0xFF5F370E),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Professional Signup",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB87300),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Register your restaurant business to get started.",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 👤 Full Name / Business Contact
            OutlinedTextField(
                value = viewModel.fullName.value,
                onValueChange = { viewModel.fullName.value = it },
                label = { Text("Full Name / Business Contact") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📄 Restaurant License Number (New Field based on DTO)
            OutlinedTextField(
                value = viewModel.licenseNumber.value,
                onValueChange = { viewModel.licenseNumber.value = it },
                label = { Text("Restaurant License Number (Optional)") },
                leadingIcon = { Icon(Icons.Filled.Domain, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )


            Spacer(modifier = Modifier.height(16.dp))

            // ✉️ Email
            OutlinedTextField(
                value = viewModel.email.value,
                onValueChange = { viewModel.email.value = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Password
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it },
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
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🟨 REGISTER BUTTON (Styled with Gradient)
            Button(
                onClick = viewModel::signup,
                enabled = !viewModel.isLoading.value,
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
                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(color = Color(0xFF111827))
                    } else {
                        Text(
                            text = "Register Professional Account",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }


            // Error Message
            viewModel.errorMessage.value?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

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