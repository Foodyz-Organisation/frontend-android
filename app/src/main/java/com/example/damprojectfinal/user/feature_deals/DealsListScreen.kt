package com.example.damprojectfinal.user.feature_deals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.feature_deals.DealsUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Custom Yellow/White Palette ------------------
private object BrandColors {
    val YellowPrimary = Color(0xFFFFC107) // Primary accent
    val YellowContainer = Color(0xFFFFFBE0) // Light yellow for backgrounds/chips
    val WhiteBackground = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF1F1F1F) // Primary text color
    val TextMedium = Color(0xFF6C6C6C) // Secondary text color
    val ErrorRed = Color(0xFFD32F2F)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsListScreen(
    viewModel: DealsViewModel,
    onDealClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dealsState by viewModel.dealsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deals Disponibles", color = BrandColors.TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrandColors.TextDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDeals() }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Actualiser",
                            tint = BrandColors.YellowPrimary // Yellow refresh icon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.WhiteBackground
                )
            )
        },
        containerColor = Color(0xFFF7F7F7) // Off-white background for list separation
    ) { padding ->
        when (val state = dealsState) {
            is DealsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandColors.YellowPrimary) // Yellow loader
                }
            }

            is DealsUiState.Success -> {
                if (state.deals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun deal disponible pour le moment",
                            style = MaterialTheme.typography.bodyLarge,
                            color = BrandColors.TextMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.deals) { deal ->
                            DealCard(
                                deal = deal,
                                onClick = { onDealClick(deal._id) }
                            )
                        }
                    }
                }
            }

            is DealsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = BrandColors.ErrorRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDeals() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.YellowPrimary,
                                contentColor = BrandColors.TextDark
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

@Composable
fun DealCard(
    deal: Deal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), // Slightly smaller radius for modern look
        colors = CardDefaults.cardColors(containerColor = BrandColors.WhiteBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Softer shadow
    ) {
        Column {
            // Image Section
            AsyncImage(
                model = deal.image,
                contentDescription = deal.restaurantName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Restaurant Name
                Text(
                    text = deal.restaurantName,
                    style = MaterialTheme.typography.titleLarge, // Slightly larger title
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandColors.TextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chip (Yellow Branded)
                    AssistChip(
                        onClick = {},
                        label = { Text(deal.category, color = BrandColors.TextDark) },
                        shape = RoundedCornerShape(8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = BrandColors.YellowContainer,
                            labelColor = BrandColors.TextDark
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Expiration Date
                    Text(
                        text = "Expire le: ${formatDate(deal.endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandColors.TextMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = deal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = BrandColors.TextMedium
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Use consistent formatting structure and locale
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}