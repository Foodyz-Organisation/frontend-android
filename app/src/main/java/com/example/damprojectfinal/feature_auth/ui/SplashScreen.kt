package com.example.damprojectfinal.feature_auth.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash screen with sequential progress dots animation.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    title: String = "Foodies",
    subtitle: String = "Discover & Order",
    logoBackgroundColor: Color = Color.White,
    logoTint: Color = Color(0xFFF59E0B),
    durationMs: Int = 1600,
    onFinished: (() -> Unit)? = null
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF176), // light yellow
            Color(0xFFFFD60A)  // deep yellow
        )
    )

    var animateDots by remember { mutableStateOf(false) }
    var currentActiveDotIndex by remember { mutableStateOf(0) } // State for cycling dot

    val dotScale by animateFloatAsState(
        targetValue = if (animateDots) 1f else 0.85f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "dotScale"
    )

    LaunchedEffect(Unit) {
        animateDots = true

        // Duration for a single dot cycle animation
        val cycleDurationMs = 600L
        val totalCycles = (durationMs.toDouble() / cycleDurationMs).toInt()

        // Cycle the active dot index concurrently with the overall duration
        repeat(totalCycles) {
            currentActiveDotIndex = (currentActiveDotIndex + 1) % 3
            delay(cycleDurationMs)
        }

        // Execute the onFinished callback after the total duration
        if (onFinished != null) {
            // Delay remaining time to ensure total duration is met
            val remainingDelay = durationMs.toLong() - (totalCycles * cycleDurationMs)
            if (remainingDelay > 0) delay(remainingDelay)
            onFinished()
        }
    }

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
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = logoTint,
                        modifier = Modifier.size(72.dp)
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

            // --- Animated Dots Row ---
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val isActive = index == currentActiveDotIndex
                    val color = if (isActive) Color(0xFFFFF1B0) else Color(0xFFFFF1B0).copy(alpha = 0.6f)

                    // Apply dotScale animation to all dots, but size the active dot slightly larger
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