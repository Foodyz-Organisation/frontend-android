package com.example.damprojectfinal.feature_auth.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.damprojectfinal.AuthRoutes
import com.example.damprojectfinal.feature_auth.viewmodels.VerifyOtpUiState
import com.example.damprojectfinal.feature_auth.viewmodels.VerifyOtpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOtpScreen(
    email: String,
    navController: NavController,
    viewModel: VerifyOtpViewModel
) {
    var otp by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ✅ CORRECTION : Navigation vers ResetPasswordScreen
    LaunchedEffect(uiState) {
        Log.d("VerifyOtpScreen", "Current state: $uiState")

        when (val state = uiState) {
            is VerifyOtpUiState.Verified -> {
                Log.d("VerifyOtpScreen", "✅ OTP Verified! Email: ${state.email}, Token: ${state.resetToken}")

                Toast.makeText(context, "OTP verified successfully!", Toast.LENGTH_SHORT).show()

                // ✅ Navigation vers reset_password avec email et resetToken
                navController.navigate("${AuthRoutes.RESET_PASSWORD}/${state.email}/${state.resetToken}") {
                    // Optionnel : empêcher le retour à VerifyOtp
                    // popUpTo(AuthRoutes.VERIFY_OTP) { inclusive = true }
                }

                // ✅ Reset l'état pour éviter les re-navigations
                viewModel.resetState()
            }
            is VerifyOtpUiState.Error -> {
                Log.e("VerifyOtpScreen", "❌ Error: ${state.message}")
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is VerifyOtpUiState.Loading -> {
                Log.d("VerifyOtpScreen", "⏳ Loading...")
            }
            else -> {
                Log.d("VerifyOtpScreen", "⚪ Idle state")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Enter Verification Code",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "We sent a 6-digit code to\n$email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = {
                        // ✅ N'accepter que les chiffres et max 6 caractères
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            otp = it
                        }
                    },
                    label = { Text("6-Digit Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is VerifyOtpUiState.Loading
                )

                if (otp.isNotEmpty() && otp.length != 6) {
                    Text(
                        text = "Code must be 6 digits",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        Log.d("VerifyOtpScreen", "Verifying OTP: $otp for email: $email")
                        viewModel.verifyOtp(email, otp)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = uiState !is VerifyOtpUiState.Loading && otp.length == 6
                ) {
                    if (uiState is VerifyOtpUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verify Code")
                    }
                }

                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = uiState !is VerifyOtpUiState.Loading
                ) {
                    Text("Back")
                }
            }
        }
    }
}