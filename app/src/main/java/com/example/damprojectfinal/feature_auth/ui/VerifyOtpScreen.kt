package com.example.damprojectfinal.feature_auth.ui

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(uiState) {
        Log.d("VerifyOtpScreen", "Current state: $uiState")

        when (val state = uiState) {
            is VerifyOtpUiState.Verified -> {
                Log.d("VerifyOtpScreen", "✅ OTP Verified! Email: ${state.email}, Token: ${state.resetToken}")
                Toast.makeText(context, "OTP verified successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("${AuthRoutes.RESET_PASSWORD}/${state.email}/${state.resetToken}") {
                    // popUpTo(AuthRoutes.VERIFY_OTP) { inclusive = true }
                }
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
                text = "Verify Code",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB87300),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We sent a 6-digit verification code to",
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                color = Color(0xFFB87300),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- OTP Field ---
            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        otp = it
                    }
                },
                label = { Text("6-Digit Code") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                placeholder = { Text("000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is VerifyOtpUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                isError = otp.isNotEmpty() && otp.length != 6
            )

            if (otp.isNotEmpty() && otp.length != 6) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Code must be 6 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Verify Button ---
            Button(
                onClick = {
                    Log.d("VerifyOtpScreen", "Verifying OTP: $otp for email: $email")
                    viewModel.verifyOtp(email, otp)
                },
                enabled = uiState !is VerifyOtpUiState.Loading && otp.length == 6,
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
                    if (uiState is VerifyOtpUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color(0xFF111827),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Verify Code",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Back Link ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Didn't receive the code? ", color = Color(0xFF6B7280))
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = uiState !is VerifyOtpUiState.Loading
                ) {
                    Text(
                        text = "Resend",
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}