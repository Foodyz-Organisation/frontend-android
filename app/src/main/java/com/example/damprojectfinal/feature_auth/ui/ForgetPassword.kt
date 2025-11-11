package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = { navController.popBackStack() },
    onStartReset: (String) -> Unit = { /* Placeholder for triggering email API call */ }
) {
    // --- State for the Email Input ---
    var emailInput by remember { mutableStateOf("") }

    // --- Design Variables (Consistent with SignupScreen) ---
    val primaryLightYellow = Color(0xFFFFD60A).copy(alpha = 0.6f)
    val secondaryDarkText = Color(0xFF6B7280)
    val placeholderText = Color(0xFFAAAAAA)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = primaryLightYellow,
        unfocusedBorderColor = Color(0xFFE0E0E0),
        cursorColor = primaryLightYellow,
        focusedLabelColor = secondaryDarkText,
        unfocusedLabelColor = secondaryDarkText,
        unfocusedTextColor = secondaryDarkText,
        focusedTextColor = secondaryDarkText,
        unfocusedPlaceholderColor = placeholderText,
        focusedPlaceholderColor = placeholderText
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", color = secondaryDarkText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back", tint = secondaryDarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(48.dp))

            // --- Header ---
            Text(
                text = "You Forget Your Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryDarkText
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Enter your email address and we'll send you instructions to reset your password.",
                color = secondaryDarkText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // --- 1. EMAIL INPUT FIELD ---

            // Reusing the CategoryHeader for structure, though less necessary here
            CategoryHeader("Account Email")

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                placeholder = { Text("your.email@example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(Modifier.height(40.dp))

            // 🟨 SEND RESET LINK BUTTON
            Button(
                onClick = { onStartReset(emailInput) },
                enabled = emailInput.isNotBlank(), // Enable when email is typed
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
                    Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // You can add a link back to login if needed
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onNavigateBack) {
                Text("Back to Login", color = secondaryDarkText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Reusing helper function from SignupScreen for consistent header styling
@Composable
private fun CategoryHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFB87300),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
}