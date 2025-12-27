package com.example.damprojectfinal.feature_auth.ui

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.R
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Splash screen that checks authentication and reports the user's role.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    tokenManager: TokenManager,
    // Callback to inform the navigator about the authentication result.
    onAuthCheckComplete: (userId: String?, userRole: String?) -> Unit,
    title: String = "Foodyz",
    subtitle: String = "Discover & Order",
    logoBackgroundColor: Color = Color.White,
    logoTint: Color = Color(0xFFF59E0B),
    durationMs: Int = 1600,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF176),
            Color(0xFFFFD60A)
        )
    )

    var currentActiveDotIndex by remember { mutableStateOf(0) }
    val dotScale by animateFloatAsState(
        targetValue = 1f, // Simplified animation trigger
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "dotScale"
    )

    LaunchedEffect(Unit) {
        // 1. CHECK ONBOARDING FIRST (FAST TRACK)
        // If onboarding is NOT incomplete, we want to show it immediately without splash delay
        val isOnboardingCompleted = tokenManager.isOnboardingCompleted().first()
        if (!isOnboardingCompleted) {
            onAuthCheckComplete(null, "onboarding")
            return@LaunchedEffect
        }

        // 2. Normal Splash Animation & Auth Check
        val cycleDuration = 600L
        val totalCycles = (durationMs.toDouble() / cycleDuration).toInt()

        repeat(totalCycles) {
            delay(cycleDuration)
            currentActiveDotIndex = (currentActiveDotIndex + 1) % 3
        }

        // Authentication check
        val accessToken = tokenManager.getAccessTokenAsync()

        if (!accessToken.isNullOrEmpty()) {
            // Check if token is expired
            val isExpired = com.example.damprojectfinal.core.utils.JwtUtils.isTokenExpired(accessToken)
            
            if (isExpired) {
                // Token is expired - clear all auth data and go to login
                Log.w("SplashScreen", "🔓 Token expired - clearing auth data")
                tokenManager.clearTokens()
                onAuthCheckComplete(null, null)
            } else {
                // Token is valid - verify we have complete data
                val userId = tokenManager.getUserIdFlow().first()
                val userRole = tokenManager.getUserRole().first()
                
                // Check if we have all required data
                if (userId.isNullOrEmpty() || userRole.isNullOrEmpty()) {
                    // Incomplete auth data - clear and go to login
                    tokenManager.clearTokens()
                    onAuthCheckComplete(null, null)
                } else {
                    // Token is valid and data is complete - proceed to home
                    onAuthCheckComplete(userId, userRole)
                }
            }
        } else {
            // No token: user is logged out -> Go to Login
            onAuthCheckComplete(null, null)
        }
    }

    // --- UI remains the same ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(24.dp, RoundedCornerShape(48.dp), clip = false)
                    .clip(RoundedCornerShape(48.dp)),
                colors = CardDefaults.cardColors(containerColor = logoBackgroundColor)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.logo_name),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111111)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2C2C2C),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = index == currentActiveDotIndex
                    val color = if (isActive) Color(0xFFFFF1B0) else Color(0xFFFFF1B0).copy(alpha = 0.6f)
                    val size = if (isActive) 14.dp * dotScale else 12.dp

                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                }
            }
        }
    }
}
