package com.example.damprojectfinal.user.feature_menu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.R
import com.airbnb.lottie.compose.*
// DTOs for Menu Item structure (source)
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
import com.example.damprojectfinal.core.dto.menu.Category
// DTOs for Cart structure (target)
import com.example.damprojectfinal.core.dto.cart.AddToCartRequest
import com.example.damprojectfinal.core.dto.cart.IngredientDto as CartIngredientDto
import com.example.damprojectfinal.core.dto.cart.OptionDto as CartOptionDto
// Repositories and Networking
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.ui.theme.*
// ViewModels and State
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModel
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartUiState
import com.example.damprojectfinal.user.feature_menu.viewmodel.DynamicMenuViewModel
import com.example.damprojectfinal.user.feature_menu.viewmodel.DynamicMenuViewModelFactory
import com.google.gson.Gson

// -----------------------------------------------------------------------------
// --- DATA MODELS ---
// -----------------------------------------------------------------------------
data class Option(
    val name: String,
    val price: Float
)

data class MenuItem(
    val id: String,
    val name: String,
    val priceDT: Float,
    val imageUrl: String?,
    val category: Category,
    val defaultIngredients: List<String> = emptyList(),
    val extraOptions: List<Option> = emptyList()
)

// Category UI Model
data class CategoryItem(
    val category: Category,
    val displayName: String,
    val icon: String
)

// Category configuration
private fun getCategoryItems(): List<CategoryItem> {
    return listOf(
        CategoryItem(Category.BURGER, "BURGER", "🍔"),
        CategoryItem(Category.PIZZA, "PIZZA", "🍕"),
        CategoryItem(Category.PASTA, "PASTA", "🍝"),
        CategoryItem(Category.MEXICAN, "MEXICAN", "🌮"),
        CategoryItem(Category.SUSHI, "SUSHI", "🍣"),
        CategoryItem(Category.ASIAN, "ASIAN", "🥡"),
        CategoryItem(Category.SEAFOOD, "SEAFOOD", "🦞"),
        CategoryItem(Category.CHICKEN, "CHICKEN", "🍗"),
        CategoryItem(Category.SANDWICHES, "SANDWICHES", "🥪"),
        CategoryItem(Category.SALAD, "SALAD", "🥗"),
        CategoryItem(Category.DESSERT, "DESSERT", "🍰"),
        CategoryItem(Category.DRINKS, "DRINKS", "🥤"),
    )
}

// DTO to UI Model Mappers (Necessary to convert backend Double to UI Float)
private fun List<IngredientDto>?.toIngredientNames(): List<String> {
    return this?.mapNotNull { it.name } ?: emptyList()
}
private fun List<com.example.damprojectfinal.core.dto.menu.OptionDto>?.toOptionModels(): List<Option> {
    return this?.map { Option(name = it.name, price = it.price.toFloat()) } ?: emptyList()
}
private fun MenuItemResponseDto.toUiModel(): MenuItem {
    return MenuItem(
        id = this.id,
        name = this.name,
        priceDT = this.price.toFloat(),
        imageUrl = this.image,
        category = this.category,
        defaultIngredients = this.ingredients.toIngredientNames(),
        extraOptions = this.options.toOptionModels()
    )
}

// -----------------------------------------------------------------------------
// MAIN COMPOSABLE: Restaurant Menu Screen
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(
    restaurantId: String,
    onBackClick: () -> Unit,
    onViewCartClick: () -> Unit,
    onConfirmOrderClick: () -> Unit,
    // REQUIRED DEPENDENCIES
    cartViewModel: CartViewModel,
    userId: String,
) {
    val gson = remember { Gson() }
    val menuRepository = remember {
        MenuItemRepository(RetrofitClient.menuItemApi, gson)
    }
    // TODO: Replace with real auth token retrieval mechanism
    val authToken = remember { "YOUR_REAL_TOKEN" }

    // --- Menu ViewModel Setup ---
    val menuViewModel: DynamicMenuViewModel = viewModel(
        factory = DynamicMenuViewModelFactory(
            repository = menuRepository,
            professionalId = restaurantId,
            authToken = authToken
        )
    )

    // --- Cart State Observation ---
    val cartUiState by cartViewModel.uiState.collectAsState()

    // ⭐ FIX 1: Safely cast the state to Success to access the cart data
    val currentCart = (cartUiState as? CartUiState.Success)?.cart

    // UI Data conversion
    val rawMenuItems by menuViewModel.menuItems.collectAsState()

    // ⭐ FIX: Enforce unique IDs when mapping to UI models to prevent LazyColumn crash
    val uiMenuItems: List<MenuItem> = remember(rawMenuItems) {
        rawMenuItems
            .distinctBy { it.id } // Filters out any duplicate IDs
            .map { it.toUiModel() }
    }

    // UI state for loading and error
    val isLoading by menuViewModel.isLoading.collectAsState()
    val errorMessage by menuViewModel.errorMessage.collectAsState()

    var selectedItemForCustomization by remember { mutableStateOf<MenuItem?>(null) }
    
    // Category selection state
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    // Filter items by category
    val filteredItems = remember(uiMenuItems, selectedCategory) {
        if (selectedCategory == null) {
            uiMenuItems
        } else {
            uiMenuItems.filter { it.category == selectedCategory }
        }
    }
    
    // Get available categories from menu items
    val availableCategories = remember(uiMenuItems) {
        val categories = uiMenuItems.map { it.category }.distinct()
        getCategoryItems().filter { it.category in categories }
    }

    // Derived state for UI display (Reads directly from the updated cart)
    // ⭐ FIX 2: Safely access cart properties after checking type
    val cartItemCount = currentCart?.items?.size ?: 0
    val totalOrderPrice = currentCart?.items?.sumOf { it.calculatedPrice * it.quantity }?.toFloat() ?: 0.0f

    // Load cart on start
    LaunchedEffect(userId) {
        // ⭐ FIX 3: Removed userId argument based on the ViewModel provided in the last step
        cartViewModel.loadCart()
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                restaurantName = "Chili's", // Consider passing the real restaurant name here
                onBackClick = onBackClick,
                // FIX: Change 'onViewCartClick' to 'onCartClick' to match the function definition
                onCartClick = onViewCartClick,
                cartItemCount = cartItemCount
            )
        },
        bottomBar = {
            MenuBottomBar(
                totalOrderPrice = totalOrderPrice,
                onConfirmClick = onConfirmOrderClick
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Selector
            if (availableCategories.isNotEmpty()) {
                CategorySelector(
                    categories = availableCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Content based on state
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading menu: $errorMessage",
                            color = Color.Red
                        )
                    }
                }
                filteredItems.isEmpty() -> {
                    // Empty state with animation
                    EmptyCategoryState(
                        categoryName = selectedCategory?.name ?: "this category"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(Modifier.height(8.dp)) }

                        items(
                            items = filteredItems,
                            key = { item -> item.id }
                        ) { item ->
                            MenuItemCard(
                                item = item,
                                onAddClick = { selectedItemForCustomization = item }
                            )
                        }
                    }
                }
            }
        }
    }


// --- Customization Overlay Logic ---
    selectedItemForCustomization?.let { item -> // 'item' is the MenuItem UI model
        ItemCustomizationOverlay(
            item = item,
            onDismiss = { selectedItemForCustomization = null },
            onConfirmAddToCart = { confirmedItem, quantity, ingredientsToRemove, selectedOptions ->

                val menuItem = confirmedItem

                // --- 1. Unit Price Calculation ---
                val basePrice = menuItem.priceDT.toDouble()
                val optionsPrice = selectedOptions.sumOf { it.price.toDouble() }

                // The price per unit (item) AFTER options are added
                val unitPrice = basePrice + optionsPrice

                // --- 2. Data Mapping (UI Models to DTOs) ---

                // Map selectedOptions (Set<Option> UI model) to List<CartOptionDto> DTO
                val finalSelectedOptions: List<CartOptionDto> = selectedOptions.map { uiOption ->
                    CartOptionDto(
                        name = uiOption.name,
                        price = uiOption.price.toDouble()
                    )
                }

                // Filter Ingredients: Map UI ingredient names (Set<String>) back to DTOs
                val initialIngredientNames: List<String> = menuItem.defaultIngredients
                val namesToKeep = initialIngredientNames.filter { it !in ingredientsToRemove }

                // Map ingredients names to CartIngredientDto (the DTO needed for the Cart POST endpoint)
                val finalIngredientsDto: List<CartIngredientDto> = namesToKeep.map { name ->
                    // Assuming all ingredients in this flow are default ones, as the UI only allows removal of default ones.
                    CartIngredientDto(name = name, isDefault = true)
                }

                // --- 3. Construct AddToCartRequest DTO ---
                val menuItemId = menuItem.id

                // ⭐ FIX 4: Corrected DTO field names to match the AddToCartRequest definition
                val request = AddToCartRequest(
                    menuItemId = menuItemId,
                    quantity = quantity,
                    name = menuItem.name,
                    chosenIngredients = finalIngredientsDto,
                    chosenOptions = finalSelectedOptions,
                    calculatedPrice = unitPrice,
                )


                // Call the existing 'addItem' function in CartViewModel
                // ⭐ FIX 5: Removed customerId argument based on the ViewModel provided in the last step
                cartViewModel.addItem(
                    request = request
                )

                selectedItemForCustomization = null
            }
        )
    }
}

// -----------------------------------------------------------------------------
// CUSTOMIZATION OVERLAY AND HELPERS
// -----------------------------------------------------------------------------

@Composable
fun ItemCustomizationOverlay(
    item: MenuItem,
    onDismiss: () -> Unit,
    onConfirmAddToCart: (MenuItem, Int, Set<String>, Set<Option>) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var ingredientsToRemove by remember { mutableStateOf(setOf<String>()) }
    var selectedOptions by remember { mutableStateOf(setOf<Option>()) }

    // Calculate final total (for display only)
    val finalTotal = remember(item.priceDT, quantity, selectedOptions) {
        val optionsPrice = selectedOptions.sumOf { it.price.toDouble() }.toFloat()
        (item.priceDT + optionsPrice) * quantity
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundLight)
        ) {
            CustomizationHeader(item = item, onDismiss = onDismiss)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    QuantitySelector(
                        quantity = quantity,
                        onQuantityChange = { quantity = it }
                    )
                    Spacer(Modifier.height(24.dp))

                    // 1. Ingredients (Removal)
                    IngredientsCustomizer(
                        ingredients = item.defaultIngredients,
                        ingredientsToRemove = ingredientsToRemove,
                        onToggleIngredient = { ingredient ->
                            ingredientsToRemove = if (ingredientsToRemove.contains(ingredient)) {
                                ingredientsToRemove - ingredient
                            } else {
                                ingredientsToRemove + ingredient
                            }
                        }
                    )

                    // 2. Options (Add-ons)
                    if (item.extraOptions.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        OptionsCustomizer(
                            options = item.extraOptions,
                            selectedOptions = selectedOptions,
                            onToggleOption = { option ->
                                selectedOptions = if (selectedOptions.contains(option)) {
                                    selectedOptions - option
                                } else {
                                    selectedOptions + option
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            CustomizationFooter(
                total = finalTotal,
                onAddToCart = {
                    onConfirmAddToCart(item, quantity, ingredientsToRemove, selectedOptions)
                }
            )
        }
    }
}

// --- Supporting Composable Implementations ---

@Composable
fun CustomizationHeader(item: MenuItem, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = AppDarkText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("${item.priceDT} DT", color = AppPrimaryRed, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
                .size(32.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = AppDarkText)
        }
    }
    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppDarkText)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                colors = ButtonDefaults.buttonColors(containerColor = AppCardBackground, contentColor = AppDarkText),
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
            }
            Spacer(Modifier.width(16.dp))
            Text(quantity.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppDarkText)
            Spacer(Modifier.width(16.dp))
            FloatingActionButton(
                onClick = { onQuantityChange(quantity + 1) },
                containerColor = AppCartButtonYellow,
                contentColor = AppDarkText,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
            }
        }
    }
}

@Composable
fun IngredientsCustomizer(
    ingredients: List<String>,
    ingredientsToRemove: Set<String>,
    onToggleIngredient: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Customize Ingredients",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppDarkText
        )
        Text(
            "Remove any ingredients you don't like",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ingredients.forEach { ingredient ->
            val isSelected = !ingredientsToRemove.contains(ingredient)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppCardBackground)
                    .clickable { onToggleIngredient(ingredient) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onToggleIngredient(ingredient) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppPrimaryRed,
                        unselectedColor = Color.LightGray
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(ingredient, color = AppDarkText, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun OptionsCustomizer(
    options: List<Option>,
    selectedOptions: Set<Option>,
    onToggleOption: (Option) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Extra Options (Add-ons)",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppDarkText
        )
        Text(
            "Add extra sauces or toppings",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        options.forEach { option ->
            val isSelected = selectedOptions.contains(option)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppCardBackground)
                    .clickable { onToggleOption(option) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleOption(option) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppCartButtonYellow,
                            uncheckedColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(option.name, color = AppDarkText, fontWeight = FontWeight.Medium)
                }

                Text(
                    text = String.format("+%.2f DT", option.price),
                    color = AppPrimaryRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun CustomizationFooter(total: Float, onAddToCart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBackground)
            .padding(16.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppDarkText)
            Text(
                String.format("%.2f DT", total),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppCartButtonYellow
            )
        }

        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AppCartButtonYellow)
        ) {
            Text(
                text = "Add to Cart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppDarkText
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppDarkText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopAppBar(
    restaurantName: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppDarkText)
                }
                Text(
                    text = "Back to search",
                    color = AppDarkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }

            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = AppPrimaryRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "View Cart",
                        tint = AppDarkText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = restaurantName.firstOrNull()?.toString() ?: "",
                    color = AppDarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = restaurantName,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppDarkText
            )
        }
        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
    }
}

@Composable
fun MenuItemCard(item: MenuItem, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppCardBackground)
            .clickable { onAddClick() }
            .height(120.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.placeholder),
            error = painterResource(id = R.drawable.placeholder),
            modifier = Modifier
                .fillMaxHeight()
                .width(120.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppDarkText
            )
            Text(
                text = "${item.priceDT} DT",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = AppDarkText
            )
        }
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(48.dp),
            containerColor = AppCartButtonYellow,
            contentColor = AppDarkText
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add to Cart")
        }
    }
}

@Composable
fun MenuBottomBar(totalOrderPrice: Float, onConfirmClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Price:",
                fontSize = 18.sp,
                color = AppDarkText
            )
            Text(
                text = String.format("%.2f DT", totalOrderPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9333EA)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCartButtonYellow,
                contentColor = AppDarkText
            )
        ) {
            Text(
                text = "Confirm Order",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppDarkText)
        }
    }
}
// -----------------------------------------------------------------------------
// CATEGORY SELECTOR
// -----------------------------------------------------------------------------

@Composable
fun CategorySelector(
    categories: List<CategoryItem>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { categoryItem ->
            CategoryChip(
                categoryItem = categoryItem,
                isSelected = selectedCategory == categoryItem.category,
                onClick = {
                    onCategorySelected(
                        if (selectedCategory == categoryItem.category) null
                        else categoryItem.category
                    )
                }
            )
        }
    }
    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
}

@Composable
fun CategoryChip(
    categoryItem: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) AppPrimaryRed.copy(alpha = 0.1f)
                    else Color(0xFFFEF3F2)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = categoryItem.icon,
                fontSize = 32.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // Category Name
        Text(
            text = categoryItem.displayName,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AppPrimaryRed else AppDarkText
        )
    }
}

// -----------------------------------------------------------------------------
// EMPTY STATE WITH ANIMATION
// -----------------------------------------------------------------------------

@Composable
fun EmptyCategoryState(categoryName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie Animation
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.empty)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No items in $categoryName",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppDarkText
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Try selecting a different category",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
