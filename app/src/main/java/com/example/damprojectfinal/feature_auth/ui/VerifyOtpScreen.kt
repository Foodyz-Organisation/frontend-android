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
import androidx.compose.material.icons.filled.ArrowBack // Added for navigation icon
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

// --- Custom Colors (Consistent with other screens) ---
private val PrimaryText = Color(0xFF1F2937) // Dark Gray
private val SecondaryText = Color(0xFF6B7280) // Medium Gray
private val AccentYellow = Color(0xFFF59E0B) // Vibrant Yellow/Gold
private val CreamyWhiteLight = Color(0xFFFEFDFB) // Very light cream/off-white for background start
private val CreamyWhiteDark = Color(0xFFF9F6F0) // Slightly darker cream for gradient end


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
                text = "Verify Code",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We've sent a 6-digit verification code to:",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = email,
                color = AccentYellow,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- OTP Field ---
            OutlinedTextField(
                value = otp,
                onValueChange = {
                    // Limit input to 6 digits
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
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                isError = otp.isNotEmpty() && otp.length != 6
            )

            if (otp.isNotEmpty() && otp.length != 6) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Code must be 6 digits",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow), // Solid yellow button
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (uiState is VerifyOtpUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Verify Code",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // --- Resend and Back Link ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Didn't receive the code? ", color = SecondaryText)
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = uiState !is VerifyOtpUiState.Loading,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Resend",
                        color = AccentYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}