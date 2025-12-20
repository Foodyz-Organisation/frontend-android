package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.user.common._component.DynamicSearchOverlay
import com.example.damprojectfinal.user.common._component.TopAppBar
import com.example.damprojectfinal.user.common._component.SecondaryNavBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.rememberCoroutineScope
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.UserRoutes // <--- Ensure this imports the UserRoutes object correctly
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsScreen
import com.example.damprojectfinal.core.retro.RetrofitClient
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import com.example.damprojectfinal.user.common._component.UserMenuScreenContent
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.UserRepository
import com.example.damprojectfinal.user.feature_notifications.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home", // Default route is home
    onLogout: () -> Unit,
    logoutSuccess: StateFlow<Boolean>
) {

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var isSearchActive by remember { mutableStateOf(false) }

    // ✅ NOUVEAU: État pour les points de fidélité
    var loyaltyPoints by remember { mutableStateOf<Int?>(null) }

    // ✅ NOUVEAU: Charger les points de fidélité au démarrage

    // State for food types
    var foodTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingFoodTypes by remember { mutableStateOf(false) }

    // State for selected food type filter (null means "All")
    var selectedFoodType by remember { mutableStateOf<String?>(null) }

    // Notifications ViewModel (for badges)
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(context, isProfessional = false)
    )
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()


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

    // State for user profile information
    var profilePictureUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var userName by rememberSaveable { mutableStateOf<String?>(null) }
    var userEmail by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoadingProfilePicture by remember { mutableStateOf(false) }

    // Fetch user profile information - only fetch if not already loaded
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && currentUserId != "placeholder_user_id_123" && !isLoadingProfilePicture) {
            // Only fetch if we don't already have the data
            if (profilePictureUrl == null || userName == null) {
                isLoadingProfilePicture = true
                try {
                    val token = tokenManager.getAccessTokenAsync()
                    if (!token.isNullOrEmpty()) {
                        val userApiService = UserApiService(tokenManager)
                        val userRepository = UserRepository(userApiService)
                        val user = userRepository.getUserById(currentUserId, token)
                        // Update profile information
                        profilePictureUrl = user.profilePictureUrl
                        userName = user.username
                        userEmail = user.email
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error fetching user profile: ${e.message}")
                    // Keep the previous values if there's an error
                } finally {
                    isLoadingProfilePicture = false
                }
            }
        }
    }
    
    // Track current route for TopAppBar
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // Load notifications for badges
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && currentUserId != "placeholder_user_id_123") {
            notificationViewModel.loadNotifications(currentUserId)
        }
    }

    // Derive badge flags
    val hasUnreadNotifications by remember(unreadNotificationCount) {
        mutableStateOf(unreadNotificationCount > 0)
    }
    val hasUnreadMessages by remember(notifications) {
        mutableStateOf(
            notifications.any {
                !it.isRead && (it.type == "message_received" || it.type == "conversation_started")
            }
        )
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

    // --- Main Screen Content with Right-Side Drawer ---
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content with fixed TopAppBar
        Scaffold(
            topBar = {
                TopAppBar(
                    navController = navController,
                    currentRoute = currentRoute ?: "",
                    openDrawer = { 
                        isDrawerOpen = !isDrawerOpen // Toggle drawer open/close
                    },
                    onSearchClick = { isSearchActive = true },
                    currentUserId = currentUserId,
                    onProfileClick = { userId ->
                        navController.navigate("${UserRoutes.PROFILE_VIEW.substringBefore("/")}/${userId ?: currentUserId}")
                    },
                    onReelsClick = {
                        navController.navigate(UserRoutes.REELS_SCREEN)
                    },
                    onLogoutClick = onLogout,
                    profilePictureUrl = profilePictureUrl,
                    hasUnreadNotifications = hasUnreadNotifications,
                    hasUnreadMessages = hasUnreadMessages
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF8F9FA)
                            )
                        )
                    )
            ) {
                // Body scrolls; TopAppBar stays fixed via Scaffold
                PostsScreen(
                    navController = navController,
                    selectedFoodType = selectedFoodType,
                    headerContent = {
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp
                        val isTablet = screenWidth > 600
                        val isSmallScreen = screenWidth < 360
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = if (isTablet) 20.dp else if (isSmallScreen) 12.dp else 16.dp,
                                    bottom = if (isSmallScreen) 6.dp else 8.dp)
                        ) {
                            // Feature Cards Section - Enhanced Food App Style
                            FoodAppFeatureCards(navController = navController)
                            
                            Spacer(modifier = Modifier.height(if (isTablet) 28.dp else if (isSmallScreen) 18.dp else 24.dp))
                            
                            // Category Filter Chips - Enhanced with better design
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else if (isSmallScreen) 8.dp else 10.dp),
                                contentPadding = PaddingValues(horizontal = if (isTablet) 24.dp else 16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Always show "All" as the first item with icon
                                item {
                                    EnhancedFoodCategoryChip(
                                        text = "All",
                                        emoji = "🍽️",
                                        selected = selectedFoodType == null,
                                        onClick = {
                                            selectedFoodType = null
                                        }
                                    )
                                }

                                // Display fetched food types dynamically with emojis
                                if (isLoadingFoodTypes) {
                                    item {
                                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFFFFC107)
                                            )
                                        }
                                    }
                                } else {
                                    items(foodTypes) { foodType ->
                                        EnhancedFoodCategoryChip(
                                            text = foodType,
                                            emoji = getEmojiForFoodType(foodType),
                                            selected = selectedFoodType == foodType,
                                            onClick = {
                                                selectedFoodType = foodType
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(if (isTablet) 28.dp else if (isSmallScreen) 18.dp else 24.dp))
                            
                            // "Ready to be ordered" Section Header - Enhanced
                            EnhancedSectionHeader(
                                title = "Ready to be ordered",
                                onClearClick = { /* Clear filter or dismiss */ }
                            )
                            
                            Spacer(modifier = Modifier.height(if (isTablet) 20.dp else if (isSmallScreen) 12.dp else 16.dp))
                        }
                    }
                )
            }
        }

        // Full-screen drawer overlay
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isDrawerOpen = false }
            )
        }

        // Full-screen drawer - slides from right, covers everything including TopAppBar
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { fullWidth -> fullWidth }
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                color = Color.White
            ) {
                // Make the content fill the Surface and be scrollable
                UserMenuScreenContent(
                    navController = navController,
                    onLogout = {
                        isDrawerOpen = false
                        onLogout()
                    },
                    onBackClick = { isDrawerOpen = false },
                    loyaltyPoints = loyaltyPoints,
                    showTopBar = true, // Show top bar since drawer covers everything
                    paddingValues = PaddingValues(0.dp),
                    userId = currentUserId,
                    profilePictureUrl = profilePictureUrl,
                    userName = userName,
                    userEmail = userEmail
                )
            }
        }
    }

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
fun FoodAppFeatureCards(navController: NavHostController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth > 600
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 24.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 20.dp else 14.dp)
    ) {
        // Deliver Now Card - Enhanced
        EnhancedFoodFeatureCard(
            title = "Deliver Now",
            subtitle = "Fast delivery",
            icon = Icons.Filled.DeliveryDining,
            iconTint = Color(0xFF1F2937),
            gradientColors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF5F7FA)
            ),
            iconBackground = Color(0xFFF3F4F6),
            onClick = { /* Navigate to delivery */ }
        )
        
        // Daily Deals Card - More vibrant and exceptional
        EnhancedFoodFeatureCard(
            title = "Daily Deals",
            subtitle = "Up to 50% off",
            icon = Icons.Filled.Redeem,
            iconTint = Color(0xFFF59E0B),
            gradientColors = listOf(
                Color(0xFFFFFBEB),
                Color(0xFFFFF4D6)
            ),
            iconBackground = Color(0xFFFFE4B5),
            onClick = {
                navController.navigate("deals")
            }
        )
    }
}

@Composable
fun RowScope.EnhancedFoodFeatureCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    gradientColors: List<Color>,
    iconBackground: Color,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth > 600
    val isSmallScreen = screenWidth < 360
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Card(
        modifier = Modifier
            .weight(1f)
            .height(if (isTablet) 160.dp else if (isSmallScreen) 120.dp else 140.dp)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .shadow(
                elevation = if (isPressed) 8.dp else 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.15f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(if (isTablet) 24.dp else if (isSmallScreen) 16.dp else 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon with background circle
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 56.dp else if (isSmallScreen) 40.dp else 48.dp)
                        .background(
                            color = iconBackground,
                            shape = RoundedCornerShape(if (isTablet) 14.dp else 12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(if (isTablet) 32.dp else if (isSmallScreen) 24.dp else 28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isTablet) 22.sp else if (isSmallScreen) 16.sp else 20.sp,
                            letterSpacing = (-0.5).sp,
                            color = Color(0xFF111827)
                        )
                    )
                    Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isTablet) 15.sp else if (isSmallScreen) 12.sp else 14.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedFoodCategoryChip(
    text: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth > 600
    val isSmallScreen = screenWidth < 360
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF111827) else Color.White,
        animationSpec = androidx.compose.animation.core.tween(300)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF111827),
        animationSpec = androidx.compose.animation.core.tween(300)
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (selected) 6.dp else 3.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (selected) Color.Black.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.1f),
                ambientColor = if (selected) Color.Black.copy(alpha = 0.15f) else Color.Transparent
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isTablet) 20.dp else if (isSmallScreen) 14.dp else 18.dp,
                vertical = if (isTablet) 14.dp else if (isSmallScreen) 10.dp else 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 10.dp)
        ) {
            Text(
                text = emoji,
                fontSize = if (isTablet) 22.sp else if (isSmallScreen) 18.sp else 20.sp
            )
            Text(
                text = text,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = if (isTablet) 16.sp else if (isSmallScreen) 13.sp else 15.sp,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun EnhancedSectionHeader(
    title: String,
    onClearClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth > 600
    val isSmallScreen = screenWidth < 360
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 24.dp else 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isTablet) 24.sp else if (isSmallScreen) 18.sp else 22.sp,
                letterSpacing = (-0.5).sp,
                color = Color(0xFF111827)
            )
        )
        IconButton(
            onClick = onClearClick,
            modifier = Modifier
                .size(if (isTablet) 40.dp else if (isSmallScreen) 32.dp else 36.dp)
                .background(
                    color = Color(0xFFF3F4F6),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(if (isTablet) 20.dp else if (isSmallScreen) 16.dp else 18.dp)
            )
        }
    }
}

fun getEmojiForFoodType(foodType: String): String {
    return when (foodType.lowercase()) {
        "spicy", "épicé" -> "🌶️"
        "healthy", "sain" -> "🥗"
        "sweet", "sucré", "dessert" -> "🍰"
        "italian", "italien" -> "🍝"
        "asian", "asiatique" -> "🍜"
        "fast food" -> "🍔"
        "vegan", "végétarien" -> "🌱"
        "seafood", "fruits de mer" -> "🦐"
        "breakfast", "petit-déjeuner" -> "🥐"
        "drinks", "boissons" -> "🥤"
        else -> "🍽️"
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
// Keep FilterChipItem for backward compatibility if needed elsewhere
@Composable
fun FilterChipItem(text: String, selected: Boolean, onClick: () -> Unit = {}) {
    EnhancedFoodCategoryChip(
        text = text,
        emoji = "🍽️",
        selected = selected,
        onClick = onClick
    )
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
    FoodAppFeatureCards(navController = navController)
    // Fake logout state so preview doesn't crash
    val fakeLogoutState = MutableStateFlow(false)

    HomeScreen(
        navController = rememberNavController(),
        currentRoute = "home", // Ensure "home" is used for initial selection in preview
        onLogout = {},                 // do nothing in preview
        logoutSuccess = fakeLogoutState
    )
}