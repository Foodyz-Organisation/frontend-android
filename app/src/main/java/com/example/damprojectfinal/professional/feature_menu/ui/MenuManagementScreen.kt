package com.example.damprojectfinal.professional.feature_menu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuListUiState
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.Category
import com.airbnb.lottie.compose.*
import com.example.damprojectfinal.R

// Custom Brand Colors
private val PrimaryBrandOrange = Color(0xFFFA4A0C)
private val BrandYellow = Color(0xFFFFC107)
private val BackgroundLight = Color(0xFFF6F6F9)
private val CategoryBackgroundGray = Color(0xFFF0F0F0)
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = Color(0xFF9A9A9D)

private const val BASE_URL = "http://10.0.2.2:3000/"
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
    navController: NavController,
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

    LaunchedEffect(professionalId) {
        viewModel.fetchGroupedMenu(professionalId, dummyAuthToken)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Menu Items", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
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
// COMPONENT: Empty States
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
// COMPONENT: Horizontal Scrolling Category Icons
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
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
// COMPONENT: The individual Category Icon Chip
// --------------------------------------------------
@Composable
fun CategoryIconChip(
    categoryName: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(64.dp),
            color = if (isSelected) PrimaryBrandOrange.copy(alpha = 0.2f) else CategoryBackgroundGray
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = rememberAsyncImagePainter(model = iconResId),
                    contentDescription = categoryName.replace("_", " "),
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = categoryName.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryBrandOrange else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --------------------------------------------------
// COMPONENT: Renders the Grouped Menu List
// --------------------------------------------------
@Composable
fun MenuSectionList(
    navController: NavController,
    professionalId: String,
    groupedMenu: Map<String, List<MenuItemResponseDto>>,
    onDelete: (MenuItemResponseDto) -> Unit
) {
    // Adjusted vertical padding to reduce space between categories
    LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        groupedMenu.forEach { (category, items) ->
            item {
                Text(
                    text = category.uppercase().replace("_", " "),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                // Divider after category title, before items
                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            }

            // Using items() directly inside LazyColumn, keeping the vertical list layout
            items(items) { item ->
                MenuItemCard(
                    item = item,
                    onEditClick = {
                        navController.navigate("edit_menu_item/${item.id}/$professionalId")
                    },
                    onDeleteClick = { onDelete(item) }
                )
                // ADDED THINNER DIVIDER AFTER EACH ITEM FOR FLAT LIST LOOK
                Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 0.dp))
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Renders an individual Menu Item (Seamless Look)
// --------------------------------------------------
@Composable
fun MenuItemCard(
    item: MenuItemResponseDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight) // Match Scaffold background for seamless look
            .padding(horizontal = 0.dp, vertical = 12.dp), // Overall item padding
        verticalAlignment = Alignment.CenterVertically // Vertically align content within the row
    ) {
        // 1. Prominent Image on the left
        AsyncImage(
            model = BASE_URL + item.image,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp) // Fixed size for image
                .clip(RoundedCornerShape(8.dp)) // Kept image corner rounding
                .aspectRatio(1f) // Ensure image is square
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            // 2. Name and Price (Top Section)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Align text and price vertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false) // Don't let name push price too far
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBrandOrange,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 3. Description
            Text(
                text = item.description ?: "No description provided.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp) // Adjusted padding
            )

            // 4. Action Buttons (Aligned to end, no extra padding around them)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp), // Remove internal padding
                    modifier = Modifier.height(26.dp).defaultMinSize(minWidth = 1.dp) // Smaller min width
                ) {
                    Text("EDIT", color = PrimaryBrandOrange, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp)) // Space between buttons
                TextButton(
                    onClick = onDeleteClick,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp), // Remove internal padding
                    modifier = Modifier.height(26.dp).defaultMinSize(minWidth = 1.dp), // Smaller min width
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Snackbar Host
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