package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.user.common._component.AppDrawer
import com.example.damprojectfinal.user.common._component.DynamicSearchOverlay
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.user.feature_posts.ui.PostsScreen
import com.example.damprojectfinal.UserRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Placeholder Composable for HighlightCard/FilterChipItem (ensure they exist in your project)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home", // Default route is home
    onLogout: () -> Unit,
    logoutSuccess: StateFlow<Boolean>
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var isSearchActive by remember { mutableStateOf(false) }

    val currentUserId: String by remember {
        mutableStateOf(tokenManager.getUserIdBlocking() ?: "placeholder_user_id_123")
    }

    // Automatically navigate to login after logout
    LaunchedEffect(logoutSuccess) {
        logoutSuccess.collect { success ->
            if (success) {
                navController.navigate("login_route") { popUpTo(0) }
            }
        }
    }

    // General navigation helper
    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    // ------------------------- UI -------------------------
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isSearchActive,
        drawerContent = {
            AppDrawer(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                navigateTo = navigateTo,
                currentRoute = currentRoute,
                onLogoutClick = onLogout
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6)))
                    )
                    .verticalScroll(rememberScrollState(), enabled = !isSearchActive)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ----------------- TOP APP BAR -----------------
                TopAppBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    openDrawer = { scope.launch { drawerState.open() } },
                    onSearchClick = { isSearchActive = true },
                    currentUserId = currentUserId,
                    onProfileClick = { userId ->
                        navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/$userId")
                    },
                    onLogoutClick = onLogout
                )

                // ----------------- HIGHLIGHT CARD -----------------
                HighlightCard()

                // ----------------- FILTER CHIPS -----------------
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    item { FilterChipItem("All", selected = currentRoute == "home") }
                    item { FilterChipItem("🔥 Spicy", selected = false) }
                    item { FilterChipItem("🥗 Healthy", selected = false) }
                    item { FilterChipItem("🍰 Sweet", selected = false) }
                }

                // ----------------- POSTS -----------------
                Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                    PostsScreen()
                }
            }

            // ----------------- DYNAMIC SEARCH OVERLAY -----------------
            if (isSearchActive) {
                DynamicSearchOverlay(
                    navController = navController,
                    onDismiss = { isSearchActive = false }
                )
            }
        }
    }
}

// -------------------------- Helpers (unchanged) --------------------------

@Composable
fun HighlightCard() {
    val dynamicHighlights = listOf(
        HighlightCardData(
            startColor = Color(0xFFE0E7FF),
            endColor = Color(0xFFC7D2FE),
            iconTint = Color(0xFF4F46E5),
            title = "Takeaway",
            subtitle = "Pick up your food",
            iconPainter = rememberVectorPainter(Icons.Filled.LocalMall),
            contentDescription = "Takeaway"
        ),
        HighlightCardData(
            startColor = Color(0xFFE6FFFA),
            endColor = Color(0xFFB2F5EA),
            iconTint = Color(0xFF059669),
            title = "Delivery",
            subtitle = "Delivered to your door",
            iconPainter = rememberVectorPainter(Icons.Filled.DeliveryDining),
            contentDescription = "Delivery"
        ),
        HighlightCardData(
            startColor = Color(0xFFFFF0F6),
            endColor = Color(0xFFFBCFE8),
            iconTint = Color(0xFFDB2777),
            title = "Eat-in",
            subtitle = "Dine with us",
            iconPainter = rememberVectorPainter(Icons.Filled.Restaurant),
            contentDescription = "Eat-in"
        )
    )

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % dynamicHighlights.size
        }
    }

    val currentHighlight = dynamicHighlights[currentIndex]

    val dealsCardData = HighlightCardData(
        startColor = Color(0xFFFFFBEB),
        endColor = Color(0xFFFEF3C7),
        iconTint = Color(0xFFF59E0B),
        title = "Daily Deals",
        subtitle = "Up to 50% off",
        iconPainter = rememberVectorPainter(Icons.Filled.Redeem),
        contentDescription = "Daily Deals"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(currentHighlight, dealsCardData).forEach { data ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clickable { /* handle click */ },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(data.startColor, data.endColor)
                            )
                        )
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            painter = data.iconPainter,
                            contentDescription = data.contentDescription,
                            tint = data.iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black
                            )
                            Text(
                                text = data.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(Brush.linearGradient(listOf(data.startColor, data.endColor)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                Icon(
                    painter = data.iconPainter,
                    contentDescription = data.contentDescription,
                    tint = data.iconTint,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Column {
                    Text(data.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2937))
                    Text(data.subtitle, fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, selected: Boolean) {
    val backgroundColor by animateColorAsState(if (selected) Color(0xFF111827) else Color(0xFFF3F4F6))
    val textColor by animateColorAsState(if (selected) Color.White else Color(0xFF111827))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {

    // Fake logout state so preview doesn't crash
    val fakeLogoutState = MutableStateFlow(false)

    HomeScreen(
        navController = rememberNavController(),
        currentRoute = "home", // Ensure "home" is used for initial selection in preview
        onLogout = {},                 // do nothing in preview
        logoutSuccess = fakeLogoutState
    )
}