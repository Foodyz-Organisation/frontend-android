package com.example.damprojectfinal.feature_auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Fastfood
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.damprojectfinal.feature_auth.viewmodels.ResetPasswordUiState
import com.example.damprojectfinal.feature_auth.viewmodels.ResetPasswordViewModel
import com.example.damprojectfinal.AuthRoutes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    resetToken: String,
    navController: NavController,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ResetPasswordUiState.Success -> {
                Toast.makeText(context, "Password reset successfully! Please login.", Toast.LENGTH_LONG).show()
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
                viewModel.resetState()
            }
            is ResetPasswordUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
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
        unfocusedLabelColor = Color(0xFF6B7280),
        errorIndicatorColor = Color(0xFFDC2626),
        errorLabelColor = Color(0xFFDC2626)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
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
                text = "Reset Password",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB87300),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a strong new password for your account",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- New Password Field ---
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                placeholder = { Text("Enter new password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is ResetPasswordUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                isError = newPassword.isNotEmpty() && newPassword.length < 8
            )

            if (newPassword.isNotEmpty() && newPassword.length < 8) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password must be at least 8 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Confirm Password Field ---
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                placeholder = { Text("Confirm new password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is ResetPasswordUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
            )

            if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Reset Password Button ---
            Button(
                onClick = { viewModel.resetPassword(email, resetToken, newPassword) },
                enabled = uiState !is ResetPasswordUiState.Loading &&
                        newPassword.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        newPassword == confirmPassword &&
                        newPassword.length >= 8,
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
                    if (uiState is ResetPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Reset Password",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Back to Login ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Password reset successful? ", color = Color(0xFF6B7280))
                TextButton(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Back to Login",
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}