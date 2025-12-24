package com.example.damprojectfinal.user.feature_deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.feature_deals.DealDetailUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Shared Yellow/White Palette ------------------
object ThemeColors {
    val YellowPrimary = Color(0xFFFFC107)
    val YellowContainer = Color(0xFFFFFBE0)
    val WhiteBackground = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF1F1F1F)
    val TextMedium = Color(0xFF6C6C6C)
    val DividerLight = Color(0xFFE0E0E0)
    val ErrorRed = Color(0xFFD32F2F)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    dealId: String,
    viewModel: DealsViewModel,
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dealDetailState by viewModel.dealDetailState.collectAsState()

    // Trigger data load
    LaunchedEffect(dealId) {
        viewModel.loadDealById(dealId)
    }

    Scaffold(
        containerColor = ThemeColors.WhiteBackground
    ) { padding ->
        
        when (val state = dealDetailState) {
            is DealDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ThemeColors.YellowPrimary)
                }
            }

            is DealDetailUiState.Success -> {
                DealDetailContent(
                    deal = state.deal,
                    onBackClick = onBackClick,
                    onOrderClick = onOrderClick,
                    modifier = modifier.padding(bottom = padding.calculateBottomPadding())
                )
            }

            is DealDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ThemeColors.ErrorRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDealById(dealId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThemeColors.YellowPrimary,
                                contentColor = ThemeColors.TextDark
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealDetailContent(
    deal: Deal,
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp) // Space for fixed bottom bar
        ) {
            // --- Hero Image Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AsyncImage(
                    model = deal.image,
                    contentDescription = deal.restaurantName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Discount Badge (Bottom Right of Image)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(ThemeColors.YellowPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "-${deal.discountPercentage}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemeColors.TextDark
                    )
                }
            }

            // --- Content Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp) // Overlap effect
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(ThemeColors.WhiteBackground)
                    .padding(24.dp)
            ) {
                
                // Restaurant Name & Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deal.restaurantName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.TextMedium,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Special Deal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ThemeColors.TextDark
                        )
                    }
                    
                    // Category Chip
                    Surface(
                        color = ThemeColors.YellowContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = deal.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeColors.TextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = "About this offer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deal.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ThemeColors.TextMedium,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Validity Card
                ValidityCard(deal)
            }
        }

        // --- Bottom Bar ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = ThemeColors.WhiteBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Padding(
                padding = PaddingValues(24.dp)
            ) {
                Button(
                    onClick = { onOrderClick(deal.professionalId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.YellowPrimary,
                        contentColor = ThemeColors.TextDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Order Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Padding(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(padding)) {
        content()
    }
}

@Composable
fun ValidityCard(deal: Deal) {
    val daysLeft = calculateDaysLeft(deal.endDate)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FAFB) // Light gray
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ThemeColors.YellowPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (daysLeft > 0) "$daysLeft days remaining" else "Offer Expired",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (daysLeft > 0) ThemeColors.TextDark else ThemeColors.ErrorRed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Starts",
                        style = MaterialTheme.typography.labelMedium,
                        color = ThemeColors.TextMedium
                    )
                    Text(
                        text = formatDate(deal.startDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.TextDark
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ends",
                        style = MaterialTheme.typography.labelMedium,
                        color = ThemeColors.TextMedium
                    )
                    Text(
                        text = formatDate(deal.endDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.TextDark
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.UK)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun calculateDaysLeft(endDateString: String): Int {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val endDate = dateFormat.parse(endDateString)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate = calendar.time


        if (endDate != null && endDate.after(currentDate)) {
            val diff = endDate.time - currentDate.time
            (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}