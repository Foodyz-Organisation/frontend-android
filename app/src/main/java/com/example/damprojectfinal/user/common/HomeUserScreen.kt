package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.example.damprojectfinal.user.common._component.AppDrawer
import com.example.damprojectfinal.user.common._component.DynamicSearchOverlay
import com.example.damprojectfinal.user.feature_posts.ui.PostsScreen
// 🔑 Import the TopAppBar component from the common components package
import com.example.damprojectfinal.user.common._component.TopAppBar
import androidx.compose.ui.platform.LocalContext
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.AuthRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, currentRoute: String = "home") {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var isSearchActive by remember { mutableStateOf(false) }

    // Define navigation action for the drawer
    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            // Optional: Pop up to the start destination to avoid building up a large stack
            // popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            // Launch the new item as a single copy to avoid duplicate destinations
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isSearchActive,
        drawerContent = {
            AppDrawer(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                navigateTo = navigateTo,
                currentRoute = currentRoute,
                onLogout = {
                    scope.launch {
                        tokenManager.clearTokens()
                        navController.navigate(AuthRoutes.LOGIN) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                // --- 1. Main Scrollable Content ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6))
                            )
                        )
                        .verticalScroll(rememberScrollState(), enabled = !isSearchActive)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 🎯 Cleaned up call: Using the imported name 'TopAppBar'
                    TopAppBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        openDrawer = {
                            // The click on the drawer icon opens the drawer
                            scope.launch { drawerState.open() }
                        },
                        onSearchClick = {
                            isSearchActive = true
                        },
                        onProfileClick = { navController.navigate("user_profile_route") }
                    )

                    HighlightCard()

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        item { FilterChipItem("All", selected = true) }
                        item { FilterChipItem("🔥 Spicy") }
                        item { FilterChipItem("🥗 Healthy") }
                        item { FilterChipItem("🍰 Sweet") }
                    }

                    Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                        PostsScreen()
                    }
                }

                // --- 2. Dynamic Search Overlay (Placed on top of content) ---
                if (isSearchActive) {
                    DynamicSearchOverlay(
                        onNavigateToProfile = { profileId ->
                            isSearchActive = false
                            // Assuming 'restaurant_profile_route/{profileId}' needs a concrete ID for navigation
                            if (profileId != null) {
                                // Simple string check to prevent navigation error if 'null' is passed from HistoryItem closure
                                navController.navigate("restaurant_profile_route/${profileId}")
                            }
                        }
                        // ❌ Removed the erroneous onCloseSearch parameter here
                    )
                }
            }
        }
    )
}
@Composable
fun HighlightCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val takeawayCardData = HighlightCardData(
            startColor = Color(0xFFE0E7FF),
            endColor = Color(0xFFC7D2FE),
            iconTint = Color(0xFF4F46E5),
            title = "Takeaway",
            subtitle = "Pick up your food",
            iconPainter = rememberVectorPainter(Icons.Filled.LocalMall),
            contentDescription = "Takeaway"
        )

        val dealsCardData = HighlightCardData(
            startColor = Color(0xFFFFFBEB),
            endColor = Color(0xFFFEF3C7),
            iconTint = Color(0xFFF59E0B),
            title = "Daily Deals",
            subtitle = "Up to 50% off",
            iconPainter = rememberVectorPainter(Icons.Filled.Redeem),
            contentDescription = "Daily Deals"
        )

        HighlightCardItem(
            modifier = Modifier.weight(1f),
            data = takeawayCardData,
            onClick = { /* Handle Takeaway click */ }
        )

        HighlightCardItem(
            modifier = Modifier.weight(1f),
            data = dealsCardData,
            onClick = { /* Handle Daily Deals click */ }
        )
    }
}

// --- Helper Data Class and Composable for Reusability ---

private data class HighlightCardData(
    val startColor: Color,
    val endColor: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val iconPainter: androidx.compose.ui.graphics.painter.Painter,
    val contentDescription: String
)

@Composable
private fun HighlightCardItem(
    modifier: Modifier = Modifier,
    data: HighlightCardData,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.linearGradient(
                    listOf(data.startColor, data.endColor)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    painter = data.iconPainter,
                    contentDescription = data.contentDescription,
                    tint = data.iconTint,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = data.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
@Composable
fun FilterChipItem(text: String, selected: Boolean = false) {
    val backgroundColor by animateColorAsState(
        if (selected) Color(0xFF111827) else Color(0xFFF3F4F6)
    )
    val textColor by animateColorAsState(
        if (selected) Color.White else Color(0xFF111827)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .shadow(if (selected) 4.dp else 0.dp, RoundedCornerShape(50))
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FoodCard(name: String, place: String, tags: List<String>, price: String, image: Int) {
    Column(
        modifier = Modifier
            .width(270.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .shadow(10.dp, RoundedCornerShape(24.dp))
    ) {
        Box {
            // NOTE: painterResource(id = image) requires an actual resource ID (R.drawable.xxx)
            // which is not defined here, but the composable structure is correct.
            Image(
                painter = painterResource(id = image),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )

            // Prep time chip
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Time",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "  Prepare 8 min",
                        fontSize = 12.sp,
                        color = Color(0xFF1F2937)
                    )
                }
            }

            // Favorite button
            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF111827),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = place,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tags.forEach { TagItem(it) }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = price,
                color = Color(0xFFF97316),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "🛍️ Order", color = Color(0xFF1F2937), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TagItem(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF7ED))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFFF97316),
            fontWeight = FontWeight.Medium
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
// You need a dummy NavController for preview
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun HighlightCardPreview() {
    HighlightCard()
}
