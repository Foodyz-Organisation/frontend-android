package com.example.damprojectfinal.user.feature_deals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.dto.deals.Deal
import com.example.damprojectfinal.feature_deals.DealsUiState
import com.example.damprojectfinal.feature_deals.DealsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// ------------------ Premium Yellow/Dark Palette ------------------
private object BrandColors {
    val YellowPrimary = Color(0xFFFFC107)
    val YellowLight = Color(0xFFFFECB3)
    val YellowCream = Color(0xFFFFF9E6)
    val Background = Color(0xFFF8F9FA)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF121212)
    val TextSecondary = Color(0xFF757575)
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF388E3C)
    val Red = Color(0xFFE53935)
    val Orange = Color(0xFFFF9800)
    val TextDark = Color(0xFF1E1E1E)
    val YellowContainer = Color(0xFFFFF2CD)
    val GrayLight = Color(0xFFF5F5F5)
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Responsive: 2 columns for phones, 3 for tablets
    val columns = remember(configuration.screenWidthDp) {
        when {
            configuration.screenWidthDp > 600 -> 3 // Tablet
            else -> 2 // Phone
        }
    }

    Scaffold(
        topBar = {
            // Custom header matching the design - pushed from top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandColors.YellowCream)
                    .statusBarsPadding()
            ) {
                // Top bar with back button, title, and "Up to 50% OFF" button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(BrandColors.YellowLight, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = BrandColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Title with gift icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = BrandColors.Orange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Daily Deals",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.TextPrimary
                            )
                        )
                    }

                    // "Up to 50% OFF" button - responsive text size
                    Surface(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = BrandColors.YellowLight
                    ) {
                        Text(
                            "Up to 50% OFF",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (screenWidth > 360.dp) 12.sp else 10.sp
                            ),
                            color = BrandColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Subtitle
                Text(
                    "Limited time offers",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (screenWidth > 360.dp) 14.sp else 12.sp
                    ),
                    color = BrandColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
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
                        Column(modifier = modifier.fillMaxSize()) {
                            // Section header: "Today's Special Offers"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 16.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BrandColors.YellowPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Today's Special Offers",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BrandColors.TextPrimary,
                                            fontSize = if (screenWidth > 360.dp) 18.sp else 16.sp
                                        )
                                    )
                                }
                                Text(
                                    "${state.deals.size} deals available",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = if (screenWidth > 360.dp) 12.sp else 10.sp
                                    ),
                                    color = BrandColors.TextSecondary
                                )
                            }

                            // Responsive grid of deal cards
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(
                                    if (screenWidth > 600.dp) 16.dp else 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(
                                    if (screenWidth > 600.dp) 20.dp else 16.dp
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.deals) { deal ->
                                    DealCard(
                                        deal = deal,
                                        onClick = { onDealClick(deal._id) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
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
fun DealCard(
    deal: Deal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Responsive image height
    val imageHeight = remember(screenWidth) {
        when {
            screenWidth > 600.dp -> 180.dp // Tablet
            screenWidth > 360.dp -> 140.dp // Normal phone
            else -> 120.dp // Small phone
        }
    }
    
    // Mock data for missing fields (can be replaced with actual data when available)
    val discountPercent = remember(deal._id) { Random.nextInt(30, 51) } // 30 to 50 inclusive
    val rating = remember(deal._id) { 
        4.5 + (Random.nextDouble() * 0.5) // Random between 4.5 and 5.0
    }
    val originalPrice = remember(deal._id) { Random.nextInt(40, 101) } // 40 to 100 inclusive
    val discountedPrice = remember(deal._id, originalPrice, discountPercent) { 
        (originalPrice * (1 - discountPercent / 100.0)).toInt() 
    }
    val tags = remember(deal._id) { 
        listOf(deal.category, "Special").take(2)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Image section - responsive height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                val imageModel = when {
                    deal.image.startsWith("http", ignoreCase = true) -> deal.image
                    deal.image.startsWith("content:", ignoreCase = true) -> deal.image
                    deal.image.isNotBlank() -> BaseUrlProvider.getFullImageUrl(deal.image)
                    else -> null
                }

                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = deal.restaurantName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrandColors.GrayLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = BrandColors.TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Discount badge (top-left)
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = BrandColors.Red
                ) {
                    Text(
                        text = "-$discountPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Favorite icon (top-right)
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.9f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) BrandColors.Red else BrandColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Rating (bottom-left overlay)
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = BrandColors.YellowPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", rating),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Content section - responsive padding
            val contentPadding = remember(screenWidth) {
                if (screenWidth > 600.dp) 16.dp else 12.dp
            }
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                // Title - responsive font size
                Text(
                    text = deal.description.take(if (screenWidth > 360.dp) 30 else 20).ifEmpty { deal.restaurantName },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.TextPrimary,
                        fontSize = if (screenWidth > 600.dp) 16.sp else if (screenWidth > 360.dp) 14.sp else 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Restaurant name - responsive font size
                Text(
                    text = deal.restaurantName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = if (screenWidth > 360.dp) 12.sp else 10.sp
                    ),
                    color = BrandColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Offer dates in yellow rounded rectangle - responsive
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = BrandColors.YellowContainer
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (screenWidth > 360.dp) 8.dp else 6.dp,
                            vertical = if (screenWidth > 360.dp) 6.dp else 4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = BrandColors.TextPrimary,
                            modifier = Modifier.size(if (screenWidth > 360.dp) 14.dp else 12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Offer available from ${formatDateRange(deal.startDate)} until ${formatDateRange(deal.endDate)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = if (screenWidth > 360.dp) 10.sp else 9.sp
                            ),
                            color = BrandColors.TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tags - responsive
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandColors.YellowLight
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (screenWidth > 360.dp) 10.sp else 9.sp
                                ),
                                color = BrandColors.TextPrimary,
                                modifier = Modifier.padding(
                                    horizontal = if (screenWidth > 360.dp) 8.dp else 6.dp,
                                    vertical = if (screenWidth > 360.dp) 4.dp else 3.dp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price row - responsive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$originalPrice DT",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = if (screenWidth > 360.dp) 12.sp else 10.sp
                            ),
                            color = BrandColors.TextSecondary
                        )
                        Text(
                            text = "$discountedPrice DT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (screenWidth > 600.dp) 18.sp else if (screenWidth > 360.dp) 16.sp else 14.sp
                            ),
                            color = BrandColors.TextPrimary
                        )
                    }

                    // "Grab Deal" button - responsive
                    Button(
                        onClick = onClick,
                        modifier = Modifier.height(if (screenWidth > 360.dp) 32.dp else 28.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.YellowPrimary
                        ),
                        contentPadding = PaddingValues(
                            horizontal = if (screenWidth > 360.dp) 12.dp else 8.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = BrandColors.TextPrimary,
                            modifier = Modifier.size(if (screenWidth > 360.dp) 16.dp else 14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Grab Deal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (screenWidth > 360.dp) 12.sp else 10.sp
                            ),
                            color = BrandColors.TextPrimary
                        )
                    }
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

private fun formatDateRange(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd-MM", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
