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
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.user.common._component.AppDrawer
import com.example.damprojectfinal.user.common._component.DynamicSearchOverlay
import com.example.damprojectfinal.user.feature_posts.ui.PostsScreen
import com.example.damprojectfinal.user.common._component.TopAppBar
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.damprojectfinal.core.api.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.damprojectfinal.UserRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home",
    onLogout: () -> Unit,                     // from parent
    logoutSuccess: StateFlow<Boolean>         // from parent
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }


    var isSearchActive by remember { mutableStateOf(false) }

    // ⭐ FIX: Using getStoredUserId() as the assumed method.
    // If your TokenManager only has getToken(), you must create an extension/helper
    // to decode the token to get the ID, or implement a separate storage for the ID.
    // Ensure you replace getStoredUserId() with the actual method name available in your TokenManager.
    val currentUserId: String by remember {
        mutableStateOf(tokenManager.getUserIdBlocking() ?: "placeholder_user_id_123")
    }


    // 🔥 Observe logout result ONLY once
    LaunchedEffect(logoutSuccess) {
        logoutSuccess.collect { success ->
            if (success) {
                // Navigate to Login after successful logout
                navController.navigate("login_route") {
                    popUpTo(0)
                }
            }
        }
    }

    // Navigation including logout
    val navigateTo: (String) -> Unit = { route ->
        if (route == "logout_trigger") {
            onLogout()   // DO NOT collect flow here
        } else {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
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
                currentRoute = currentRoute
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF9FAFB), Color(0xFFF3F4F6))
                        )
                    )
                    .verticalScroll(rememberScrollState(), enabled = !isSearchActive)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TopAppBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    openDrawer = { scope.launch { drawerState.open() } },
                    onSearchClick = { isSearchActive = true },
                    // ✅ Passed the currentUserId
                    currentUserId = currentUserId,
                    // ✅ Correctly navigates using the dynamic route
                    onProfileClick = { userId ->
                        navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/$userId")
                    }
                )

                HighlightCard()

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    item { FilterChipItem("All", selected = true) }
                    item { FilterChipItem("🔥 Spicy", selected = false) }
                    item { FilterChipItem("🥗 Healthy", selected = false) }
                    item { FilterChipItem("🍰 Sweet", selected = false) }
                }

                Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                    PostsScreen()
                }
            }

            if (isSearchActive) {
                DynamicSearchOverlay(
                    onNavigateToProfile = { profileId ->
                        isSearchActive = false
                        if (profileId != null) {
                            navController.navigate("restaurant_profile_route/$profileId")
                        }
                    }
                )
            }
        }
    }
}

// -------------------------- Helpers (unchanged) --------------------------

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

        HighlightCardItem(modifier = Modifier.weight(1f), data = takeawayCardData, onClick = {})
        HighlightCardItem(modifier = Modifier.weight(1f), data = dealsCardData, onClick = {})
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
        currentRoute = "home_screen",
        onLogout = {},                 // do nothing in preview
        logoutSuccess = fakeLogoutState
    )
}