package com.example.damprojectfinal.user.common

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.scale
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
import com.example.damprojectfinal.user.common._component.SecondaryNavBar
import com.example.damprojectfinal.UserRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsScreen
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.ReelsViewModel
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import com.example.damprojectfinal.user.common._component.UserMenuScreenContent
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.UserRepository
import com.example.damprojectfinal.user.feature_notifications.viewmodel.NotificationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.damprojectfinal.user.common._component.AnimatedFlipHeader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    currentRoute: String = "home",
    onLogout: () -> Unit,
    logoutSuccess: StateFlow<Boolean>
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    var loyaltyPoints by remember { mutableStateOf<Int?>(null) }
    var foodTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingFoodTypes by remember { mutableStateOf(false) }
    var selectedFoodType by remember { mutableStateOf<String?>(null) }

    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory(context, isProfessional = false)
    )
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        isLoadingFoodTypes = true
        try {
            val token = tokenManager.getAccessTokenAsync()
            if (!token.isNullOrEmpty()) {
                val api = ReclamationRetrofitClient.createClient(token)
                val balance = api.getUserLoyalty()
                loyaltyPoints = balance?.loyaltyPoints
            }
            foodTypes = RetrofitClient.postsApiService.getFoodTypes()
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error loading data: ${e.message}")
        } finally {
            isLoadingFoodTypes = false
        }
    }

    val currentUserId: String by remember {
        mutableStateOf(tokenManager.getUserIdBlocking() ?: "placeholder_user_id_123")
    }

    var profilePictureUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var userName by rememberSaveable { mutableStateOf<String?>(null) }
    var userEmail by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoadingProfilePicture by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && currentUserId != "placeholder_user_id_123" && !isLoadingProfilePicture) {
            if (profilePictureUrl == null || userName == null) {
                isLoadingProfilePicture = true
                try {
                    val token = tokenManager.getAccessTokenAsync()
                    if (!token.isNullOrEmpty()) {
                        val userApiService = UserApiService(tokenManager)
                        val userRepository = UserRepository(userApiService)
                        val user = userRepository.getUserById(currentUserId, token)
                        profilePictureUrl = user.profilePictureUrl
                        userName = user.username
                        userEmail = user.email
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error fetching user profile: ${e.message}")
                } finally {
                    isLoadingProfilePicture = false
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && currentUserId != "placeholder_user_id_123") {
            notificationViewModel.loadNotifications(currentUserId)
        }
    }

    val hasUnreadNotifications by remember(unreadNotificationCount) {
        mutableStateOf(unreadNotificationCount > 0)
    }

    LaunchedEffect(logoutSuccess) {
        logoutSuccess.collect { success ->
            if (success) {
                navController.navigate("login_route") { popUpTo(0) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        color = Color.White,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 4.dp
                    ) {
                        SecondaryNavBar(
                            navController = navController,
                            currentRoute = navController.currentBackStackEntry?.destination?.route ?: UserRoutes.HOME_SCREEN,
                            onReelsClick = {
                                navController.navigate(UserRoutes.REELS_SCREEN)
                            },
                            hasUnreadMessages = notifications.any {
                                !it.isRead && (it.type == "message_received" || it.type == "conversation_started")
                            },
                            hasUnreadNotifications = hasUnreadNotifications,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // Top Delivery Location Bar
                DeliveryLocationBar(
                    onMenuClick = { isDrawerOpen = true },
                    navController = navController
                )
                
                // Search Bar
                SearchBar(
                    onSearchClick = { isSearchActive = true },
                    onAddClick = { navController.navigate(UserRoutes.CREATE_POST) }
                )
                
                // Restaurant Cards with Header Content (No nested scroll)
                val reelsViewModel: ReelsViewModel = viewModel()
                PostsScreen(
                    navController = navController,
                    selectedFoodType = selectedFoodType,
                    reelsViewModel = reelsViewModel,
                    headerContent = {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Promotional Banner
                            // Deals Carousel
                            DealsCarousel(
                                onDealClick = {
                                    navController.navigate("deals")
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // Kitchen Near You Section
                            Text(
                                text = "Kitchen Near You",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Category Icons Row with filtering
                            CategoryIconsRow(
                                selectedFoodType = selectedFoodType,
                                onFoodTypeSelected = { foodType ->
                                    selectedFoodType = foodType
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                )
            }
        }

        // Drawer overlay
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isDrawerOpen = false }
            )
        }

        // Full-screen drawer
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { it }
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { it }
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopEnd),
                color = Color.White
            ) {
                UserMenuScreenContent(
                    navController = navController,
                    onLogout = {
                        isDrawerOpen = false
                        onLogout()
                    },
                    onBackClick = { isDrawerOpen = false },
                    loyaltyPoints = loyaltyPoints,
                    showTopBar = true,
                    paddingValues = PaddingValues(0.dp),
                    userId = currentUserId,
                    profilePictureUrl = profilePictureUrl,
                    userName = userName,
                    userEmail = userEmail
                )
            }
        }
    }

    if (isSearchActive) {
        DynamicSearchOverlay(
            onDismiss = { isSearchActive = false },
            onNavigateToProfile = { professionalId ->
                isSearchActive = false
                if (professionalId.isNotEmpty()) {
                    navController.navigate("restaurant_profile_view/$professionalId")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// New Components for Food Delivery Design

@Composable
fun DeliveryLocationBar(
    onMenuClick: () -> Unit,
    navController: NavHostController
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth > 600
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                horizontal = when {
                    isTablet -> 24.dp
                    isSmallScreen -> 12.dp
                    else -> 16.dp
                },
                vertical = when {
                    isTablet -> 16.dp
                    isSmallScreen -> 8.dp
                    else -> 12.dp
                }
            )
    ) {
        // App Signature with Animated Flip Header
        AnimatedFlipHeader(
            onEventsClick = {
                navController.navigate(UserRoutes.EVENT_LIST_REMOTE)
            },
            modifier = Modifier.align(Alignment.Center)
        )

        // Menu Button (Right Side)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(
                    when {
                        isTablet -> 48.dp
                        isSmallScreen -> 36.dp
                        else -> 40.dp
                    }
                )
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF1F2937),
                modifier = Modifier.size(
                    when {
                        isTablet -> 28.dp
                        isSmallScreen -> 20.dp
                        else -> 24.dp
                    }
                )
            )
        }
    }
}

@Composable
fun SearchBar(
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 360
    val isTablet = screenWidth > 600
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = when {
                    isTablet -> 24.dp
                    isSmallScreen -> 12.dp
                    else -> 16.dp
                },
                vertical = when {
                    isTablet -> 12.dp
                    isSmallScreen -> 6.dp
                    else -> 8.dp
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(
            when {
                isTablet -> 16.dp
                isSmallScreen -> 8.dp
                else -> 12.dp
            }
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fake Search Bar (Clickable)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(
                    when {
                        isTablet -> 60.dp
                        isSmallScreen -> 48.dp
                        else -> 52.dp
                    }
                )
                .clickable(onClick = onSearchClick),
            shape = RoundedCornerShape(
                when {
                    isTablet -> 16.dp
                    else -> 12.dp
                }
            ),
            color = Color(0xFFF9FAFB),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(
                        when {
                            isTablet -> 26.dp
                            isSmallScreen -> 20.dp
                            else -> 24.dp
                        }
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search foods and Kitchen",
                    color = Color(0xFF9CA3AF),
                    fontSize = when {
                        isTablet -> 16.sp
                        isSmallScreen -> 12.sp
                        else -> 14.sp
                    }
                )
            }
        }
        
        // Add Button
        Box(
            modifier = Modifier
                .size(
                    when {
                        isTablet -> 60.dp
                        isSmallScreen -> 48.dp
                        else -> 52.dp
                    }
                )
                .background(
                    Color(0xFFFFC107),
                    RoundedCornerShape(
                        when {
                            isTablet -> 16.dp
                            else -> 12.dp
                        }
                    )
                )
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Post",
                tint = Color.White,
                modifier = Modifier.size(
                    when {
                        isTablet -> 28.dp
                        isSmallScreen -> 20.dp
                        else -> 24.dp
                    }
                )
            )
        }
    }
}

@Composable
fun CategoryTabsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = category == selectedCategory
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) Color(0xFFFFC107) else Color.White)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color.White else Color(0xFF6B7280),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DealsCarousel(
    dealsViewModel: com.example.damprojectfinal.feature_deals.DealsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onDealClick: () -> Unit = {}
) {
    val dealsState by dealsViewModel.dealsState.collectAsState()

    when (val state = dealsState) {
        is com.example.damprojectfinal.feature_deals.DealsUiState.Loading -> {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC107))
            }
        }
        is com.example.damprojectfinal.feature_deals.DealsUiState.Success -> {
            val activeDeals = state.deals.filter { it.isActive }.reversed()
            
            if (activeDeals.isEmpty()) {
                // Coming Soon Animation - Clickable to navigate to deals screen
                ComingSoonCard(onClick = onDealClick)
            } else {
                // Show real deals from API
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeDeals) { deal ->
                        RealDealCard(
                            deal = deal,
                            onClick = onDealClick
                        )
                    }
                }
            }
        }
        is com.example.damprojectfinal.feature_deals.DealsUiState.Error -> {
            // Error state - show coming soon, clickable to navigate to deals screen
            ComingSoonCard(onClick = onDealClick)
        }
    }
}

@Composable
fun ComingSoonCard(onClick: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎉",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Exciting Deals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Coming Soon!",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RealDealCard(
    deal: com.example.damprojectfinal.core.dto.deals.Deal,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SPECIAL OFFER",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${deal.discountPercentage}% OFF",
                    color = Color(0xFFFBBF24),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deal.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deal.restaurantName,
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Deal Image or Emoji
            if (deal.image.isNotEmpty()) {
                AsyncImage(
                    model = deal.image,
                    contentDescription = "Deal image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback emoji based on category
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF374151)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(deal.category),
                        fontSize = 48.sp
                    )
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.uppercase()) {
        "PIZZA" -> "🍕"
        "BURGER" -> "🍔"
        "PASTA" -> "🍝"
        "SUSHI" -> "🍣"
        "DESSERT" -> "🍰"
        "DRINK" -> "🥤"
        "SALAD" -> "🥗"
        else -> "🍽️"
    }
}

@Composable
fun CategoryIconsRow(
    selectedFoodType: String? = null,
    onFoodTypeSelected: (String?) -> Unit = {}
) {
    val popularFoodTypes = com.example.damprojectfinal.core.dto.posts.FoodType.getPopularFoodTypes()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(popularFoodTypes.size) { index ->
            val foodType = popularFoodTypes[index]
            val isSelected = selectedFoodType == foodType.value
            CategoryIconItem(
                emoji = foodType.emoji,
                label = foodType.displayName,
                isSelected = isSelected,
                onClick = {
                    // Toggle: if already selected, deselect (show all posts)
                    if (isSelected) {
                        onFoodTypeSelected(null)
                    } else {
                        onFoodTypeSelected(foodType.value)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryIconItem(
    emoji: String,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    color = if (isSelected) Color(0xFFFFC107) else Color(0xFFFFECB3),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFFFFC107) else Color(0xFF1F2937),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
            onClick = { }
        )
        
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