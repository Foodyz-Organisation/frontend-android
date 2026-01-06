package com.example.damprojectfinal.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.core.utils.PasswordStrength

/**
 * Animated password strength indicator component
 * Shows visual feedback for password strength with smooth animations
 */
@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    // Colors for different strength levels
    val weakColor = Color(0xFFFF5252)      // Red
    val mediumColor = Color(0xFFFFA726)    // Orange
    val strongColor = Color(0xFF66BB6A)    // Green
    val inactiveColor = Color(0xFFE0E0E0) // Light gray

    // Determine current color based on strength
    val currentColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> weakColor
            PasswordStrength.MEDIUM -> mediumColor
            PasswordStrength.STRONG -> strongColor
        },
        animationSpec = tween(durationMillis = 300),
        label = "PasswordStrengthColor"
    )

    // Determine fill progress (0.33, 0.66, 1.0)
    val fillProgress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> 0.33f
            PasswordStrength.MEDIUM -> 0.66f
            PasswordStrength.STRONG -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "PasswordStrengthProgress"
    )

    Column(modifier = modifier) {
        // Strength bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Three segments
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                index < (fillProgress * 3).toInt() -> currentColor
                                else -> inactiveColor
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Strength label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (strength) {
                    PasswordStrength.WEAK -> "Weak"
                    PasswordStrength.MEDIUM -> "Medium"
                    PasswordStrength.STRONG -> "Strong"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = currentColor
            )

            // Hint for improvement
            if (strength != PasswordStrength.STRONG) {
                Text(
                    text = when (strength) {
                        PasswordStrength.WEAK -> "Add uppercase, numbers & symbols"
                        PasswordStrength.MEDIUM -> "Add special characters"
                        else -> ""
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
