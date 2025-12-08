package com.example.damprojectfinal.user.feature_deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// ------------------ Shared Yellow/White Palette (MOVE THIS TO A SEPARATE FILE) ------------------
// IMPORTANT: This block must be removed and replaced with an import if defined elsewhere.
object ThemeColors { // Renamed from BrandColors
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
    modifier: Modifier = Modifier
) {
    val dealDetailState by viewModel.dealDetailState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Deal", color = ThemeColors.TextDark) }, // Use ThemeColors
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Retour",
                            tint = ThemeColors.TextDark // Use ThemeColors
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ThemeColors.WhiteBackground // Use ThemeColors
                )
            )
        },
        containerColor = ThemeColors.WhiteBackground // Use ThemeColors
    ) { padding ->
        // Trigger data load
        LaunchedEffect(dealId) {
            viewModel.loadDealById(dealId)
        }

        when (val state = dealDetailState) {
            is DealDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ThemeColors.YellowPrimary) // Use ThemeColors
                }
            }

            is DealDetailUiState.Success -> {
                DealDetailContent(
                    deal = state.deal,
                    modifier = modifier.padding(padding)
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
                            color = ThemeColors.ErrorRed // Use ThemeColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDealById(dealId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThemeColors.YellowPrimary, // Use ThemeColors
                                contentColor = ThemeColors.TextDark // Use ThemeColors
                            )
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------

@Composable
fun DealDetailContent(
    deal: Deal,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(ThemeColors.WhiteBackground) // Use ThemeColors
    ) {
        // Image
        AsyncImage(
            model = deal.image,
            contentDescription = deal.restaurantName,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Nom du restaurant (Headline Large)
            Text(
                text = deal.restaurantName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = ThemeColors.TextDark // Use ThemeColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Catégorie (Elevated Chip - Yellow/Dark Text)
            ElevatedAssistChip(
                onClick = {},
                label = { Text(deal.category, fontWeight = FontWeight.SemiBold, color = ThemeColors.TextDark) }, // Use ThemeColors
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, Modifier.size(18.dp), tint = ThemeColors.TextDark) }, // Use ThemeColors
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ThemeColors.YellowContainer, // Use ThemeColors
                    labelColor = ThemeColors.TextDark // Use ThemeColors
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            // Using a lighter divider
            HorizontalDivider(color = ThemeColors.DividerLight, thickness = 1.dp) // Use ThemeColors
            Spacer(modifier = Modifier.height(24.dp))

            // Description Title
            Text(
                text = "Description de l'offre",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ThemeColors.TextDark // Use ThemeColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Description Content
            Text(
                text = deal.description,
                style = MaterialTheme.typography.bodyLarge,
                color = ThemeColors.TextMedium // Use ThemeColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Période de validité Card (White card, Yellow accents)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ThemeColors.WhiteBackground, // Use ThemeColors
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Card Title
                    Text(
                        text = "🗓️ Période de validité",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.YellowPrimary // Use ThemeColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date de Début
                        Column {
                            Text(
                                text = "Début",
                                style = MaterialTheme.typography.bodySmall,
                                color = ThemeColors.TextMedium // Use ThemeColors
                            )
                            Text(
                                text = formatDate(deal.startDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.TextDark // Use ThemeColors
                            )
                        }

                        // Separator line
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp)
                                .background(ThemeColors.DividerLight) // Use ThemeColors
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        // Date de Fin
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Fin",
                                style = MaterialTheme.typography.bodySmall,
                                color = ThemeColors.TextMedium // Use ThemeColors
                            )
                            Text(
                                text = formatDate(deal.endDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.TextDark // Use ThemeColors
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Temps restant (Yellow Pill indicator)
                    val daysLeft = calculateDaysLeft(deal.endDate)
                    if (daysLeft > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeColors.YellowPrimary) // Use ThemeColors
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ Il reste $daysLeft jour${if (daysLeft > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.TextDark, // Use ThemeColors
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = "❌ Offre expirée",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeColors.ErrorRed, // Use ThemeColors
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------

private fun formatDate(dateString: String): String {
    return try {
        // Updated input format to handle potential milliseconds and Z timezone correctly
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Assuming the input is UTC
        }
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun calculateDaysLeft(endDateString: String): Int {
    return try {
        // Updated date format configuration
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing is correct
        }
        val endDate = dateFormat.parse(endDateString)
        // Get current date, stripping time for accurate day calculation
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDate = calendar.time


        if (endDate != null && endDate.after(currentDate)) {
            // Use 24 hours in milliseconds for a full day difference
            val diff = endDate.time - currentDate.time
            // Calculate full days remaining
            (diff / (1000 * 60 * 60 * 24)).toInt() + 1 // Add 1 to count the current day until end date is reached
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}