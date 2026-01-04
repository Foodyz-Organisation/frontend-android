package com.example.damprojectfinal.user.feature_menu.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.damprojectfinal.core.dto.menu.MenuSuggestionsDto
import com.example.damprojectfinal.core.dto.menu.SuggestionCombination

/**
 * AI Suggestions Dialog
 * Displays AI-generated menu item suggestions in a beautiful modal
 */
@Composable
fun AISuggestionsDialog(
    suggestions: MenuSuggestionsDto?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI Suggestions",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3142)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Content based on state
                when {
                    isLoading -> {
                        // Loading state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF6B35),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "AI is analyzing ingredients...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    error != null -> {
                        // Error state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 48.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Could not load suggestions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2D3142)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    suggestions != null -> {
                        // Success state - show suggestions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Best Combination Card
                            SuggestionCard(
                                title = "Best Combination",
                                icon = Icons.Default.Star,
                                suggestion = suggestions.bestCombination,
                                gradientColors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFFF8C42)
                                )
                            )
                            
                            // Popular Choice Card
                            SuggestionCard(
                                title = "Popular Choice",
                                icon = Icons.Default.TrendingUp,
                                suggestion = suggestions.popularChoice,
                                gradientColors = listOf(
                                    Color(0xFF4ECDC4),
                                    Color(0xFF44A08D)
                                )
                            )
                            
                            // Reasoning Section
                            if (suggestions.reasoning.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8F9FA))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Why these suggestions?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2D3142)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = suggestions.reasoning,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual suggestion card with gradient background
 */
@Composable
private fun SuggestionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    suggestion: SuggestionCombination,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Description
                Text(
                    text = suggestion.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Ingredients (if any)
                if (suggestion.ingredients.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ingredients:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = suggestion.ingredients.joinToString(", "),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
                
                // Options (if any)
                if (suggestion.options.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Add-ons:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = suggestion.options.joinToString(", "),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
