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
import androidx.compose.material.icons.filled.ArrowBack
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

// --- Custom Colors (Consistent) ---
private val PrimaryText = Color(0xFF1F2937) // Dark Gray
private val SecondaryText = Color(0xFF6B7280) // Medium Gray
private val AccentYellow = Color(0xFFF59E0B) // Vibrant Yellow/Gold
private val CreamyWhiteLight = Color(0xFFFEFDFB) // Very light cream/off-white for background start
private val CreamyWhiteDark = Color(0xFFF9F6F0) // Slightly darker cream for gradient end

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

    val creamyGradient = Brush.verticalGradient(
        colors = listOf(CreamyWhiteLight, CreamyWhiteDark)
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedBorderColor = AccentYellow,
        unfocusedBorderColor = SecondaryText.copy(alpha = 0.5f),
        cursorColor = AccentYellow,
        focusedLabelColor = AccentYellow,
        unfocusedLabelColor = SecondaryText,
        focusedLeadingIconColor = AccentYellow,
        unfocusedLeadingIconColor = SecondaryText,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
    )

    val isNewPasswordValid = newPassword.length >= 8
    val passwordsMatch = newPassword == confirmPassword
    val canSubmit = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && isNewPasswordValid && passwordsMatch

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(creamyGradient),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { /* Empty title for clean look */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamyWhiteLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- App Logo ---
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fastfood,
                        contentDescription = "App Logo",
                        tint = AccentYellow,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Set New Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your new password must be different from previous passwords.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- New Password Field ---
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password (Min 8 characters)") },
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
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                isError = newPassword.isNotEmpty() && !isNewPasswordValid
            )

            if (newPassword.isNotEmpty() && !isNewPasswordValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password must be at least 8 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch
            )

            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Reset Password Button ---
            Button(
                onClick = { viewModel.resetPassword(email, resetToken, newPassword) },
                enabled = uiState !is ResetPasswordUiState.Loading && canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (uiState is ResetPasswordUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Reset Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // --- Back to Login ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Finished resetting your password? ", color = SecondaryText)
                TextButton(
                    onClick = {
                        // Use AuthRoutes.LOGIN for consistency
                        navController.navigate(AuthRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Log In",
                        color = AccentYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}