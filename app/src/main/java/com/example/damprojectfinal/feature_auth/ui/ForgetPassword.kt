package com.example.damprojectfinal.feature_auth.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.feature_auth.viewmodels.ForgotPasswordUiState
import com.example.damprojectfinal.feature_auth.viewmodels.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ✅ CORRECTION : Navigation vers VerifyOtpScreen
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ForgotPasswordUiState.OtpSent -> {
                Toast.makeText(context, "OTP sent to ${state.email}", Toast.LENGTH_SHORT).show()

                // ✅ Navigation vers verify_otp avec l'email
                navController.navigate("${AuthRoutes.VERIFY_OTP}/${state.email}") {
                    // Optionnel : empêcher le retour à ForgotPassword
                    // popUpTo(AuthRoutes.FORGOT_PASSWORD) { inclusive = false }
                }

                // ✅ Reset l'état pour éviter les re-navigations
                viewModel.resetState()
            }
            is ForgotPasswordUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Enter your email address and we'll send you a verification code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is ForgotPasswordUiState.Loading
                )

                Button(
                    onClick = { viewModel.sendOtp(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is ForgotPasswordUiState.Loading && email.isNotBlank()
                ) {
                    if (uiState is ForgotPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send Code")
                    }
                }

                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = uiState !is ForgotPasswordUiState.Loading
                ) {
                    Text("Back to Login")
                }
            }
        }
    }
}