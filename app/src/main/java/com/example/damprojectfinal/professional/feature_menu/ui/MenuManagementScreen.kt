package com.example.damprojectfinal.professional.feature_menu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuListUiState
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.Category
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R
import com.example.damprojectfinal.professional.common._component.CustomProTopBarWithIcons
import com.example.damprojectfinal.professional.common._component.ProfessionalBottomNavigationBar
import com.example.damprojectfinal.core.api.BaseUrlProvider

// Custom Brand Colors
private val PrimaryBrandOrange = Color(0xFFFA4A0C)
private val BrandYellow = Color(0xFFFFC107) // Yellow for the active background color
private val BackgroundLight = Color(0xFFF6F6F9)
private val CategoryBackgroundGray = Color(0xFFF0F0F0)
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = Color(0xFF9A9A9D)
private const val PROMOTIONS_CATEGORY = "PROMOTIONS"

// --------------------------------------------------
// HELPER: Category Icon Mapping
// --------------------------------------------------
@Composable
private fun getCategoryIconRes(category: String): Int {
    return when (category.uppercase()) {
        PROMOTIONS_CATEGORY -> R.drawable.gift
        Category.BURGER.name -> R.drawable.burger
        Category.PIZZA.name -> R.drawable.pizzaicon
        Category.PASTA.name -> R.drawable.pastaa
        Category.MEXICAN.name -> R.drawable.mexicanhat
        Category.SUSHI.name -> R.drawable.sushi
        Category.ASIAN.name -> R.drawable.ramen
        Category.SEAFOOD.name -> R.drawable.fish
        Category.CHICKEN.name -> R.drawable.chickenleg
        Category.SANDWICHES.name -> R.drawable.sandwich
        Category.SOUPS.name -> R.drawable.hotsoup
        Category.SALAD.name -> R.drawable.salad
        Category.VEGAN.name -> R.drawable.natural
        Category.HEALTHY.name -> R.drawable.diet
        Category.GLUTEN_FREE.name -> R.drawable.nosugar
        Category.SPICY.name -> R.drawable.chili
        Category.BREAKFAST.name -> R.drawable.breakfast
        Category.DESSERT.name -> R.drawable.pannacotta
        Category.DRINKS.name -> R.drawable.orangejuice
        Category.KIDS_MENU.name -> R.drawable.kidsmenu
        Category.FAMILY_MEAL.name -> R.drawable.sharing
        else -> R.drawable.restaurantmenu // Fallback
    }
}

// --------------------------------------------------
// SCREEN: Main Composable
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemManagementScreen(
    navController: NavHostController,
    professionalId: String,
    viewModel: MenuViewModel
) {
    val menuListState by viewModel.menuListUiState.collectAsState()
    val itemActionState by viewModel.uiState.collectAsState()

    val allCategories = Category.values().map { it.name }.toMutableList().apply {
        add(0, PROMOTIONS_CATEGORY) // Promotions first
    }
    // State to track the active filter. Default to the first (PROMOTIONS)
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(allCategories.firstOrNull()) }

    val dummyAuthToken = "YOUR_PROFESSIONAL_AUTH_TOKEN"
    
    // State for profile picture URL
    var profilePictureUrl by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(professionalId) {
        viewModel.fetchGroupedMenu(professionalId, dummyAuthToken)
    }
    
    // Fetch professional profile picture for top app bar
    LaunchedEffect(professionalId) {
        if (professionalId.isNotEmpty()) {
            try {
                val prof = com.example.damprojectfinal.core.retro.RetrofitClient.professionalApiService.getProfessionalAccount(professionalId)
                if (!prof.profilePictureUrl.isNullOrEmpty()) {
                    profilePictureUrl = prof.profilePictureUrl
                }
            } catch (e: Exception) {
                android.util.Log.e("MenuItemManagementScreen", "Error fetching pro profile picture: ${e.message}", e)
            }
        }
    }

    // Get current route for bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CustomProTopBarWithIcons(
                professionalId = professionalId,
                navController = navController,
                profilePictureUrl = profilePictureUrl,
                onLogout = { navController.navigate("login_route") },
                onMenuClick = { 
                    navController.navigate("professional_menu/$professionalId")
                }
            )
        },
        bottomBar = {
            ProfessionalBottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                professionalId = professionalId
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_menu_item/$professionalId") },
                containerColor = BrandYellow,
                contentColor = TextPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Menu Item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (menuListState) {
                MenuListUiState.Idle -> { /* Idle state */ }
                MenuListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBrandOrange)
                }
                is MenuListUiState.Error -> {
                    Text(
                        text = "Error: ${(menuListState as MenuListUiState.Error).message}",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                is MenuListUiState.Success -> {
                    val groupedMenu = (menuListState as MenuListUiState.Success).groupedMenu

                    // --- Filtering Logic ---
                    val filteredItemsForSelectedCategory = if (selectedCategory == PROMOTIONS_CATEGORY) {
                        groupedMenu.flatMap { it.value }.takeIf { it.isNotEmpty() }
                    } else {
                        groupedMenu[selectedCategory?.uppercase()]
                    }

                    val filteredMenu = if (selectedCategory == PROMOTIONS_CATEGORY) {
                        groupedMenu
                    } else {
                        groupedMenu.filterKeys { it.uppercase() == selectedCategory?.uppercase() }
                    }


                    Column(modifier = Modifier.fillMaxSize()) {

                        // Category Filter Row (Always Visible)
                        CategoryFilterRow(
                            categories = allCategories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { category ->
                                selectedCategory = category
                            }
                        )

                        // 2. Conditional Display based on filtering
                        if (groupedMenu.isEmpty()) {
                            // If the entire menu is empty (first time use)
                            EmptyMenuIllustration(navController, professionalId)
                        } else if (filteredItemsForSelectedCategory.isNullOrEmpty()) {
                            // If the menu has items, but the current filter (category) is empty
                            NoItemsForCategoryIllustration(selectedCategory ?: "this category")
                        } else {
                            // Display the filtered menu list
                            MenuSectionList(
                                navController = navController,
                                professionalId = professionalId,
                                groupedMenu = filteredMenu,
                                onDelete = { item ->
                                    viewModel.deleteMenuItem(item.id, professionalId, dummyAuthToken)
                                }
                            )
                        }
                    }
                }
            }
            SnackbarHostForActions(itemActionState)
        }
    }
}

// --------------------------------------------------
// COMPONENT: Empty States (No changes needed)
// --------------------------------------------------
@Composable
fun EmptyMenuIllustration(navController: NavController, professionalId: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.restaurante_nao_encontrado))
        val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your Chef's Table is Empty!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Time to add your first delicious dish. Let's fill up the menu!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun NoItemsForCategoryIllustration(categoryName: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ⭐ MODIFIED: Use Lottie Animation for consistency with the main Empty State ⭐
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.restaurante_nao_encontrado))
        val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
        // ⭐ END MODIFIED SECTION ⭐

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Dishes in ${categoryName.replace("_", " ")}!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            // Updated subtitle to reflect the action needed
            text = "Try adding a new item to the ${categoryName.replace("_", " ")} category or select another category.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// --------------------------------------------------
// COMPONENT: Horizontal Scrolling Category Icons (No changes needed)
// --------------------------------------------------
@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Increased vertical padding for space
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { categoryName ->
            CategoryIconChip(
                categoryName = categoryName,
                iconResId = getCategoryIconRes(categoryName),
                isSelected = categoryName == selectedCategory,
                onClick = { onCategorySelected(categoryName) }
            )
        }
    }
}

// --------------------------------------------------
// COMPONENT: The individual Category Icon Chip (No changes needed)
// --------------------------------------------------
@Composable
fun CategoryIconChip(
    categoryName: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // New structure uses a wide, rounded Surface for the pill shape
    val activeColor = BrandYellow // Use BrandYellow for the active background
    val inactiveColor = Color.White // White background for inactive chips

    val backgroundColor = if (isSelected) activeColor else inactiveColor

    val chipShape = RoundedCornerShape(32.dp) // Deep rounding for the pill shape

    Surface(
        shape = chipShape,
        modifier = Modifier
            .size(70.dp) // Square size for icon-only display
            .clickable(onClick = onClick),
        color = backgroundColor,
        shadowElevation = 4.dp // Subtle elevation
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Icon Background: Smaller, slightly elevated circle
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                color = Color.White, // Icon background is always white
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = rememberAsyncImagePainter(model = iconResId),
                        contentDescription = categoryName.replace("_", " "),
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Renders the Grouped Menu List (REVERTED TO STANDARD GRID)
// --------------------------------------------------
@Composable
fun MenuSectionList(
    navController: NavController,
    professionalId: String,
    groupedMenu: Map<String, List<MenuItemResponseDto>>,
    onDelete: (MenuItemResponseDto) -> Unit
) {
    // Flatten the filtered menu items list
    val items = groupedMenu.flatMap { it.value }

    // Chunk the list into pairs for the two-column layout
    val chunkedItems = items.chunked(2)

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        chunkedItems.forEach { rowItems ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        // Each item takes half the available width
                        Box(modifier = Modifier.weight(1f)) {
                            // Use fixed aspect ratio for consistent height across the row
                            MenuItemGridCard(
                                item = item,
                                aspectRatio = 0.6f, // Default, consistent height
                                onCardClick = {
                                    navController.navigate("edit_menu_item/${item.id}/$professionalId")
                                }
                            )
                        }
                    }
                    // If the last row has only one item, add an empty Box to fill the space
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Renders an individual Menu Item (FIXED PRICE VISIBILITY)
// --------------------------------------------------
@Composable
fun MenuItemGridCard(
    item: MenuItemResponseDto,
    aspectRatio: Float,
    onCardClick: () -> Unit
) {
    // Matches the visual style of the grid cards in the image
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio) // Use consistent aspect ratio
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // ⭐ FIX: Ensure SpaceBetween is applied to the main column
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Image (Centered and Circular)
            AsyncImage(
                model = BaseUrlProvider.getFullImageUrl(item.image),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                placeholder = rememberAsyncImagePainter(R.drawable.restaurantmenu),
                error = rememberAsyncImagePainter(R.drawable.restaurantmenu),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .padding(top = 8.dp)
            )

            // 2. Text and Price Block (Pushed to the bottom by SpaceBetween)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp)) // Space between image and text

                // Name
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Show ingredients with intensity indicator
                val intensityIngredients = item.ingredients.filter { it.supportsIntensity == true }
                if (intensityIngredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Intensity",
                            tint = BrandYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${intensityIngredients.size} adjustable",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Starting From Text
                Text(
                    text = "Starting From",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Price (Primary Brand Orange) - In TND
                Text(
                    // ⭐ CONFIRMATION: Price in TND, 3 decimal places
                    text = "${String.format("%.3f", item.price)} TND",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryBrandOrange,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Snackbar Host (No changes needed)
// --------------------------------------------------
@Composable
fun SnackbarHostForActions(state: MenuItemUiState) {
    // In a real app, this would use a Scaffold's SnackbarHostState
    when (state) {
        is MenuItemUiState.Success -> {
            // Snackbar logic
        }
        is MenuItemUiState.Error -> {
            // Snackbar logic
        }
        else -> Unit
    }
}