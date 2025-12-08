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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
import com.example.damprojectfinal.feature_auth.viewmodels.ForgotPasswordUiState
import com.example.damprojectfinal.feature_auth.viewmodels.ForgotPasswordViewModel

// --- Custom Colors ---
private val PrimaryText = Color(0xFF1F2937) // Dark Gray
private val SecondaryText = Color(0xFF6B7280) // Medium Gray
private val AccentYellow = Color(0xFFF59E0B) // Vibrant Yellow/Gold
private val CreamyWhiteLight = Color(0xFFFEFDFB) // Very light cream/off-white for background start
private val CreamyWhiteDark = Color(0xFFF9F6F0) // Slightly darker cream for gradient end

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ForgotPasswordUiState.OtpSent -> {
                Toast.makeText(context, "OTP sent to ${state.email}", Toast.LENGTH_SHORT).show()
                navController.navigate("${AuthRoutes.VERIFY_OTP}/${state.email}") {
                    // popUpTo(AuthRoutes.FORGOT_PASSWORD) { inclusive = false }
                }
                viewModel.resetState()
            }
            is ForgotPasswordUiState.Error -> {
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
        // Keep input field container pure white for contrast
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedBorderColor = AccentYellow,
        unfocusedBorderColor = SecondaryText.copy(alpha = 0.5f),
        cursorColor = AccentYellow,
        focusedLabelColor = AccentYellow,
        unfocusedLabelColor = SecondaryText,
        focusedLeadingIconColor = AccentYellow,
        unfocusedLeadingIconColor = SecondaryText
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // Apply the creamy gradient to the entire Scaffold background
            .background(creamyGradient),
        containerColor = Color.Transparent, // Make Scaffold transparent so gradient shows through
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
                // Use a subtle creamy color for the top bar container
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Light Yellow Background for the icon
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Subtle shadow
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
                text = "Trouble Logging In?",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter your email address below. We'll send you a verification code to securely reset your password.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- Email Field ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                placeholder = { Text("your.email@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is ForgotPasswordUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Send Code Button ---
            Button(
                onClick = { viewModel.sendOtp(email) },
                enabled = uiState !is ForgotPasswordUiState.Loading && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (uiState is ForgotPasswordUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Send Verification Code",
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
                Text(text = "Remember your password? ", color = SecondaryText)
                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = uiState !is ForgotPasswordUiState.Loading,
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