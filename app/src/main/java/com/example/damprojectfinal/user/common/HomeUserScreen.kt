package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.example.damprojectfinal.user.common._component.AppDrawer
import com.example.damprojectfinal.user.common._component.DynamicSearchOverlay
import com.example.damprojectfinal.user.common._component.TopAppBar

import com.example.damprojectfinal.user.common._component.AddButton
import com.example.damprojectfinal.UserRoutes // <--- Ensure this imports the UserRoutes object correctly
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsScreen

// REMOVED: import androidx.compose.foundation.rememberScrollState // This import is no longer needed here
// REMOVED: import androidx.compose.foundation.verticalScroll // This import is no longer needed here


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, currentRoute: String = "home") {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isSearchActive by remember { mutableStateOf(false) }

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
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
                currentRoute = currentRoute
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        openDrawer = { scope.launch { drawerState.open() } },
                        onSearchClick = { isSearchActive = true },
                        onProfileClick = { navController.navigate(UserRoutes.PROFILE_SCREEN) }, // Assuming you have this route
                        onReelsClick = { // <--- ADD THIS LINE
                            navController.navigate(UserRoutes.REELS_SCREEN) // <--- ADD THIS LINE
                        } // <--- ADD THIS LINE
                    )
                },
                floatingActionButton = {
                    AddButton(onClick = {
                        navController.navigate(UserRoutes.CREATE_POST) // <-- Corrected route name based on AppNavigation
                    })
                },
                floatingActionButtonPosition = FabPosition.End
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {

                    Column( // This Column now contains PostsScreen and DynamicSearchOverlay
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6))
                                )
                            )
                            .padding(bottom = 0.dp) // <--- MODIFIED: Adjust bottom padding
                    ) {
                        // --- MODIFIED: PostsScreen now receives the header content as a lambda ---
                        PostsScreen(
                            navController = navController, // <--- CRITICAL FIX: Pass navController
                            modifier = Modifier.weight(1f), // <--- MODIFIED: Use weight to fill remaining space
                            headerContent = { // <--- NEW: Pass header content here
                                HighlightCard()
                                Spacer(Modifier.height(24.dp)) // Spacing between sections

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                ) {
                                    item { FilterChipItem("All", selected = true) }
                                    item { FilterChipItem("🔥 Spicy") }
                                    item { FilterChipItem("🥗 Healthy") }
                                    item { FilterChipItem("🍰 Sweet") }
                                }
                                Spacer(Modifier.height(24.dp)) // Spacing before the "Pour vous" title

                                Text(
                                    text = "Pour vous 🍽️", // This title will now scroll
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF1F2937),
                                    modifier = Modifier.padding(horizontal = 16.dp, 8.dp)
                                )
                            }
                        )
                    }

                    // --- 2. Dynamic Search Overlay (Remains outside the main scrollable content) ---
                    if (isSearchActive) {
                        DynamicSearchOverlay(
                            onNavigateToProfile = { profileId ->
                                isSearchActive = false
                                if (profileId != null) {
                                    navController.navigate("restaurant_profile_route/${profileId}")
                                }
                            }
                        )
                    }
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
                Spacer(Modifier.height(8.dp))

                Column {
                    Text(
                        text = data.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.height(2.dp))
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

// REMOVED: FoodCard composable (its functionality will be replaced by RecipeCard directly in PostsScreen)
// REMOVED: TagItem composable (its usage was in FoodCard which is removed)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun HighlightCardPreview() {
    HighlightCard()
}
