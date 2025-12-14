package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.damprojectfinal.ProfileRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.UserRoutes // <--- Ensure this imports the UserRoutes object correctly
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsScreen
import com.example.damprojectfinal.core.retro.RetrofitClient
import android.util.Log


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

    // ✅ NOUVEAU: État pour les points de fidélité
    var loyaltyPoints by remember { mutableStateOf<Int?>(null) }

    // ✅ NOUVEAU: Charger les points de fidélité au démarrage

    // State for food types
    var foodTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingFoodTypes by remember { mutableStateOf(false) }

    // State for selected food type filter (null means "All")
    var selectedFoodType by remember { mutableStateOf<String?>(null) }



    LaunchedEffect(Unit) {
                isLoadingFoodTypes = true
                try {
                    Log.d("HomeScreen", "🔄 Début chargement points...")
                    val token = tokenManager.getAccessTokenAsync()
                    Log.d("HomeScreen", "🔑 Token: ${token?.take(20)}...")

                    if (!token.isNullOrEmpty()) {
                        val api = ReclamationRetrofitClient.createClient(token)
                        val balance = api.getUserLoyalty()
                        loyaltyPoints = balance?.loyaltyPoints
                        Log.d("HomeScreen", "✅ Points chargés: $loyaltyPoints")
                        Log.d("HomeScreen", "📊 Balance: $balance")
                    } else {
                        Log.e("HomeScreen", "❌ Token vide ou null")
                    }
                    foodTypes = RetrofitClient.postsApiService.getFoodTypes()
                } catch (e: Exception) {
                    // Log the error here, where 'e' is correctly in scope
                    Log.e("HomeScreen", "❌ Erreur chargement points: ${e.message}")
                    e.printStackTrace() // Don't forget the parentheses for the function call
                } finally {
                    // Use finally for cleanup, not for re-logging the error
                    isLoadingFoodTypes = false
                }
            }


    val currentUserId: String by remember {
        mutableStateOf(tokenManager.getUserIdBlocking() ?: "placeholder_user_id_123")
    }

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

    // --- Main Screen Content (Including Scaffold and Drawer) ---
    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable gestures when the search overlay is open
        gesturesEnabled = !isSearchActive,
        drawerContent = {
            AppDrawer(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                navigateTo = navigateTo,
                currentRoute = currentRoute,
                onLogoutClick = onLogout,
                loyaltyPoints = loyaltyPoints // ✅ Passer les points au drawer
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    openDrawer = { scope.launch { drawerState.open() } },
                    onSearchClick = { isSearchActive = true },
                    currentUserId = currentUserId,
                    onProfileClick = { userId ->
                        navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/$userId")
                    },
                    onReelsClick = {
                        navController.navigate(UserRoutes.REELS_SCREEN)
                    },
                    onLogoutClick = onLogout
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6)))
                    )
            ) {
                // Body scrolls; top bar stays fixed via Scaffold
                Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                    PostsScreen(
                        navController = navController,
                        selectedFoodType = selectedFoodType,
                        headerContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                HighlightCard(navController = navController)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 16.dp)
                                ) {
                                    // Always show "All" as the first item
                                    item {
                                        FilterChipItem(
                                            text = "All",
                                            selected = selectedFoodType == null,
                                            onClick = {
                                                selectedFoodType = null
                                            }
                                        )
                                    }

                                    // Display fetched food types dynamically
                                    if (isLoadingFoodTypes) {
                                        // Show loading indicator or nothing while loading
                                        item {
                                            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    } else {
                                        // Display all fetched food types
                                        items(foodTypes) { foodType ->
                                            FilterChipItem(
                                                text = foodType,
                                                selected = selectedFoodType == foodType,
                                                onClick = {
                                                    selectedFoodType = foodType
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    )
                }

                // 🌟 REMOVED: DynamicSearchOverlay should NOT be inside this padded Box.
            }
        }
    } // End ModalNavigationDrawer

    // --- Dynamic Search Overlay (Placed OUTSIDE Scaffold/Drawer to cover the whole screen) ---
    if (isSearchActive) {
        // FIX: Passing Modifier.fillMaxSize() and relying on the implementation of
        // DynamicSearchOverlay (from the previous step) to accept this modifier.
        DynamicSearchOverlay(
            onDismiss = { isSearchActive = false },
            onNavigateToProfile = { professionalId ->
                isSearchActive = false
                if (professionalId.isNotEmpty()) {
                    // Navigate to restaurant profile view (RestaurantProfileView)
                    navController.navigate("restaurant_profile_view/$professionalId")
                }
            },
            modifier = Modifier.fillMaxSize() // This ensures it covers the entire view
        )
    }

}

@Composable
fun HighlightCard(navController: NavHostController) {
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

    val takeawayCardData = HighlightCardData(
        startColor = Color(0xFFE0E7FF),
        endColor = Color(0xFFC7D2FE),
        iconTint = Color(0xFF4F46E5),
        title = "Takeaway",
        subtitle = "Grab & Go",
        iconPainter = rememberVectorPainter(Icons.Filled.LocalMall),
        contentDescription = "Takeaway"
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
                    .clickable {
                        if (data.title == "Daily Deals") {
                            navController.navigate("deals")
                        }
                    },
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
fun FilterChipItem(text: String, selected: Boolean, onClick: () -> Unit = {}) {
    val backgroundColor by animateColorAsState(if (selected) Color(0xFF111827) else Color(0xFFF3F4F6))
    val textColor by animateColorAsState(if (selected) Color.White else Color(0xFF111827))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
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
    val navController = rememberNavController()
    val fakeLogoutState = MutableStateFlow(false)
    HomeScreen(
        navController = navController,
        onLogout = {},
        logoutSuccess = fakeLogoutState
    )
}

@Preview(showBackground = true)
@Composable
fun HighlightCardPreview() {
    val navController = rememberNavController() // Dummy NavController pour la preview
    HighlightCard(navController = navController)
    // Fake logout state so preview doesn't crash
    val fakeLogoutState = MutableStateFlow(false)

    HomeScreen(
        navController = rememberNavController(),
        currentRoute = "home", // Ensure "home" is used for initial selection in preview
        onLogout = {},                 // do nothing in preview
        logoutSuccess = fakeLogoutState
    )
}