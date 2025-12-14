package com.example.damprojectfinal.user.feature_deals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.feature_deals.DealsUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel
import java.text.SimpleDateFormat
import java.util.*

// ------------------ Premium Yellow/Dark Palette ------------------
private object BrandColors {
    val YellowPrimary = Color(0xFFFFC107)
    val YellowLight = Color(0xFFFFECB3)
    val Background = Color(0xFFF8F9FA)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF121212)
    val TextSecondary = Color(0xFF757575)
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF388E3C)

    val TextDark = Color(0xFF1E1E1E)
    val YellowContainer = Color(0xFFFFF2CD)
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
                title = {
                    Text(
                        "Offres Spéciales",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadDeals() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            "Actualiser",
                            tint = BrandColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.Background,
                    scrolledContainerColor = BrandColors.Surface
                )
            )
        },
        containerColor = BrandColors.Background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = dealsState) {
                is DealsUiState.Loading -> {
                    LoadingView()
                }

                is DealsUiState.Success -> {
                    if (state.deals.isEmpty()) {
                        EmptyStateView()
                    } else {
                        LazyColumn(
                            modifier = modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    "Découvrez nos meilleures offres",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrandColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(state.deals) { deal ->
                                EnhancedDealCard(
                                    deal = deal,
                                    onClick = { onDealClick(deal._id) }
                                )
                            }
                        }
                    }
                }

                is DealsUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadDeals() }
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedDealCard(
    deal: Deal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp) // Shadow handled by Modifier.shadow
    ) {
        Column {
            // Image + Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = deal.image,
                    contentDescription = deal.restaurantName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Category Badge (Top Left)
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = BrandColors.YellowPrimary,
                    contentColor = BrandColors.TextPrimary
                ) {
                    Text(
                        text = deal.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Restaurant Name with Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BrandColors.YellowPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deal.restaurantName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = deal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.Black.copy(alpha = 0.05f))

                Spacer(modifier = Modifier.height(12.dp))

                // Footer: Date & CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = BrandColors.Error, // Red for urgency
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fin: ${formatDate(deal.endDate)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = BrandColors.Error
                        )
                    }

                    // Simple "Voir" arrow or text
                    Text(
                        text = "Voir l'offre >",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = BrandColors.YellowPrimary
                    )
                }
            }
        }
    }
}

// ------------------ Helper Views ------------------

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = BrandColors.YellowPrimary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(BrandColors.YellowLight, shape = RoundedCornerShape(60.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = BrandColors.YellowPrimary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Aucune offre pour le moment",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BrandColors.TextPrimary
        )
        Text(
            text = "Revenez plus tard pour découvrir de nouveaux deals !",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = BrandColors.Error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oups, une erreur est survenue",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BrandColors.TextPrimary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = BrandColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColors.YellowPrimary,
                contentColor = BrandColors.TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Réessayer")
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Use consistent formatting structure and locale
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM", Locale.FRANCE) // Short format: "12 Dec"
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}