package com.example.damprojectfinal.user.feature_posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damprojectfinal.R
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme


// Assuming you have a theme file at this path


// Using a standard Column for simplicity, but LazyColumn is best for long lists
@Composable
fun PostsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // *** FIX APPLIED: Apply padding to the main screen column ***
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp) // Space between cards
    ) {
        // --- Placeholder for Header Content (Takeaway Cards, Filter Chips) ---
        // (This would come before the title)

        Spacer(Modifier.height(16.dp)) // Vertical space before the title

        // "Ready to be ordered" Title
        Text(
            text = "Ready to be ordered 🍽️",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Example Usage of the RecipeCard:
        RecipeCard(
            imageRes = R.drawable.pasta,
            prepareTimeMinutes = 15,
            rating = 4.9,
            title = "Rainbow Buddha Bowl",
            subtitle = "Green Garden",
            tags = listOf("Vegan", "Healthy"),
            price = "28 DT",
            onFavoriteClick = {},
            onCommentClick = {},
            onShareClick = {},
            onBookmarkClick = {}
        )

        RecipeCard(
            imageRes = R.drawable.rice, // Using your existing rice image
            prepareTimeMinutes = 25,
            rating = 4.7,
            title = "Creamy Pesto Pasta",
            subtitle = "Italian Delights",
            tags = listOf("Vegetarian", "Dinner"),
            price = "35 DT",
            onFavoriteClick = {},
            onCommentClick = {},
            onShareClick = {},
            onBookmarkClick = {}
        )
        Spacer(Modifier.height(16.dp)) // Bottom padding
    }
}

// ------------------------------------------------------
// 🥗 Recipe Card (Modified to fill the padded width)
// ------------------------------------------------------
@Composable
fun RecipeCard(
    imageRes: Int,
    prepareTimeMinutes: Int,
    rating: Double,
    title: String,
    subtitle: String,
    tags: List<String>,
    price: String,
    onFavoriteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // "Prepare X min" Badge (Top Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Preparation Time",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Prepare $prepareTimeMinutes min",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF111827)
                    )
                }

                // Heart Icon (Like Button - Top Right)
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(28.dp)
                        .clickable {
                            isFavorite = !isFavorite
                            onFavoriteClick()
                        }
                )

                // Rating Badge (Bottom Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFACC15))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$rating",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // 2. Content Section (below image)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(Modifier.height(8.dp))

                // Tags/Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* Handle tag click if needed */ },
                            label = {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4B5563)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE5E7EB)
                            ),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Price and Bottom Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Text(
                        text = price,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )

                    // Action Icons (Comment, Share, Star, Bookmark)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = onCommentClick)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = onShareClick)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Rate",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* handle rating action */ }
                        )
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFF4F46E5) else Color(0xFF6B7280),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    isBookmarked = !isBookmarked
                                    onBookmarkClick()
                                }
                        )
                    }
                }
            }
        }
    }
}


// ------------------------------------------------------
// 👁️ Preview Composable
// ------------------------------------------------------

@Preview(showBackground = true, name = "Food Posts Screen Preview")
@Composable
fun PostsScreenPreview() {
    // Wrap the preview in your app's theme for accurate rendering
    DamProjectFinalTheme {
        // Use Surface to simulate the background of the screen
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            PostsScreen()
        }
    }
}

@Preview(showBackground = true, name = "Single Recipe Card Preview")
@Composable
fun RecipeCardPreview() {
    DamProjectFinalTheme {
        // Preview a single card centered on the screen
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecipeCard(
                imageRes = R.drawable.pasta,
                prepareTimeMinutes = 20,
                rating = 4.6,
                title = "Spicy Wok Noodle Bowl",
                subtitle = "Street Food Style",
                tags = listOf("Spicy", "Quick", "Asian"),
                price = "25 DT",
                onFavoriteClick = {},
                onCommentClick = {},
                onShareClick = {},
                onBookmarkClick = {}
            )
        }
    }
}