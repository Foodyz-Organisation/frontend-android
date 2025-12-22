package com.example.damprojectfinal.user.feature_menu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.damprojectfinal.R
import com.airbnb.lottie.compose.*
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
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

// Base URL for images
private const val BASE_URL = "http://10.0.2.2:3000/"

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
    val imagePath: String?, // Raw path for backend
    val category: Category,
    val description: String? = null, // Item description
    val calories: Int? = null, // Calories count (optional)
    val defaultIngredients: List<IngredientDto> = emptyList(), // Changed to preserve full IngredientDto
    val extraOptions: List<Option> = emptyList(),
    val createdAt: String? = null, // Date when item was created
    val updatedAt: String? = null, // Date when item was last updated
    
    // 🎯 Deal-related fields
    val discountedPrice: Float? = null, // Discounted price (if deal is active)
    val activeDealId: String? = null, // Active deal ID
    val discountPercentage: Int? = null // Discount percentage (0-100)
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
private fun List<com.example.damprojectfinal.core.dto.menu.OptionDto>?.toOptionModels(): List<Option> {
    return this?.map { Option(name = it.name, price = it.price.toFloat()) } ?: emptyList()
}
private fun MenuItemResponseDto.toUiModel(): MenuItem {
    // Generate deterministic calories based on category/ingredients if not provided (placeholder logic)
    // Using hash of name to ensure consistent calories for same item
    val nameHash = this.name.hashCode()
    val estimatedCalories = when (this.category) {
        Category.BURGER -> 350 + (nameHash % 250) // 350-600 range
        Category.PIZZA -> 300 + (nameHash % 200) // 300-500 range
        Category.PASTA -> 250 + (nameHash % 200) // 250-450 range
        Category.SALAD -> 100 + (nameHash % 100) // 100-200 range
        Category.DESSERT -> 300 + (nameHash % 200) // 300-500 range
        else -> 200 + (nameHash % 200) // 200-400 range
    }
    
    return MenuItem(
        id = this.id,
        name = this.name,
        priceDT = this.price.toFloat(),
        imageUrl = if (this.image.isNullOrEmpty()) null else BASE_URL + this.image,
        imagePath = this.image,
        category = this.category,
        description = this.description ?: "Delicious ${this.name.lowercase()} made with fresh ingredients",
        calories = estimatedCalories, // Deterministic calories based on category and name
        defaultIngredients = this.ingredients, // Keep full IngredientDto objects
        extraOptions = this.options.toOptionModels(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        
        // 🎯 Deal-related fields
        discountedPrice = this.discountedPrice?.toFloat(),
        activeDealId = this.activeDealId,
        discountPercentage = this.discountPercentage
    )
}

// Helper function to check if an item is recent (created or updated within last 7 days)
private fun MenuItem.isRecent(): Boolean {
    val daysThreshold = 7L
    val now = Date()
    
    return try {
        // Parse ISO 8601 date format (e.g., "2025-12-09T18:26:51.820Z")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        
        val createdAtDate = createdAt?.let { 
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                // Try alternative format without milliseconds
                val altFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                altFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                altFormat.parse(it)
            }
        }
        
        val updatedAtDate = updatedAt?.let { 
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                // Try alternative format without milliseconds
                val altFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                altFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                altFormat.parse(it)
            }
        }
        
        val isRecentlyCreated = createdAtDate?.let {
            val diffInMillis = now.time - it.time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            diffInDays <= daysThreshold
        } ?: false
        
        val isRecentlyUpdated = updatedAtDate?.let {
            val diffInMillis = now.time - it.time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            diffInDays <= daysThreshold
        } ?: false
        
        isRecentlyCreated || isRecentlyUpdated
    } catch (e: Exception) {
        // If date parsing fails, consider it as old
        false
    }
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
    
    // Separate items into recent and old based on creation/update date
    val (recentItems, oldItems) = remember(filteredItems) {
        val recent = filteredItems.filter { it.isRecent() }
        val old = filteredItems.filter { !it.isRecent() }
        Pair(recent, old)
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
        containerColor = Color(0xFFF8F9FA) // Light gray background for contrast
    ) { paddingValues ->
        // Content based on state
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppCartButtonYellow)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Error loading menu",
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            else -> {
                // Main scrollable content using LazyColumn for better modularity and upgradability
                // This structure makes it easy to add new sections (promotions, featured items, etc.)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFF8F9FA)), // Light gray background
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ============================================================
                    // SECTION 1: Category Selector
                    // ============================================================
                    // Easy to add: Featured categories, category promotions, etc.
                    if (availableCategories.isNotEmpty()) {
                        item {
                            CategorySelector(
                                categories = availableCategories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it }
                            )
                        }
                    }

                    // ============================================================
                    // SECTION 2: Menu Items Grid
                    // ============================================================
                    // All items displayed in a scrollable 2-column grid
                    if (filteredItems.isEmpty()) {
                        item {
                            EmptyCategoryState(
                                categoryName = selectedCategory?.name ?: "this category"
                            )
                        }
                    } else {
                        // Use items() to create a grid layout manually for better LazyColumn integration
                        // This approach allows smooth scrolling and better performance
                        items(
                            items = filteredItems.chunked(2), // Split into pairs for 2-column grid
                            key = { chunk -> chunk.firstOrNull()?.id ?: "" }
                        ) { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // First column item
                                Box(modifier = Modifier.weight(1f)) {
                                    MenuItemCard(
                                        item = rowItems[0],
                                        onAddClick = { selectedItemForCustomization = rowItems[0] }
                                    )
                                }
                                
                                // Second column item (if exists)
                                if (rowItems.size > 1) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        MenuItemCard(
                                            item = rowItems[1],
                                            onAddClick = { selectedItemForCustomization = rowItems[1] }
                                        )
                                    }
                                } else {
                                    // Empty space to maintain grid alignment
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // ============================================================
                    // SECTION 3: Bottom spacing for bottom bar
                    // ============================================================
                    // Easy to add: Restaurant info, reviews section, etc.
                    item {
                        Spacer(Modifier.height(100.dp))
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
            onConfirmAddToCart = { confirmedItem, quantity, ingredientsToRemove, selectedOptions, ingredientIntensities ->

                android.util.Log.d("DynamicMenu", "🛒 ========== ADD TO CART CLICKED ==========")
                android.util.Log.d("DynamicMenu", "Item: ${confirmedItem.name}, Quantity: $quantity")
                
                val menuItem = confirmedItem

                // --- 1. Unit Price Calculation ---
                // Use discounted price if available, otherwise use regular price
                val basePrice = if (menuItem.discountedPrice != null && menuItem.discountedPrice > 0) {
                    menuItem.discountedPrice.toDouble()
                } else {
                    menuItem.priceDT.toDouble()
                }
                val originalPrice = menuItem.priceDT.toDouble() // Always keep original price
                val optionsPrice = selectedOptions.sumOf { it.price.toDouble() }

                // The price per unit (item) AFTER options are added
                val unitPrice = basePrice + optionsPrice
                android.util.Log.d("DynamicMenu", "💰 Price calculation:")
                android.util.Log.d("DynamicMenu", "  - original=$originalPrice")
                android.util.Log.d("DynamicMenu", "  - base (discounted)=$basePrice")
                android.util.Log.d("DynamicMenu", "  - options=$optionsPrice")
                android.util.Log.d("DynamicMenu", "  - total=$unitPrice")
                if (menuItem.discountPercentage != null && menuItem.discountPercentage > 0) {
                    android.util.Log.d("DynamicMenu", "  - 🎯 Deal applied: ${menuItem.discountPercentage}% OFF (dealId=${menuItem.activeDealId})")
                }

                // --- 2. Data Mapping (UI Models to DTOs) ---

                // Map selectedOptions (Set<Option> UI model) to List<CartOptionDto> DTO
                val finalSelectedOptions: List<CartOptionDto> = selectedOptions.map { uiOption ->
                    CartOptionDto(
                        name = uiOption.name,
                        price = uiOption.price.toDouble()
                    )
                }
                android.util.Log.d("DynamicMenu", "📦 Options: ${finalSelectedOptions.size} items")
                finalSelectedOptions.forEach { opt ->
                    android.util.Log.d("DynamicMenu", "  - ${opt.name}: ${opt.price} TND")
                }

                // Filter Ingredients: Map UI ingredient names (Set<String>) back to DTOs with intensity information
                val initialIngredientNames: List<String> = menuItem.defaultIngredients.map { it.name }
                val namesToKeep = initialIngredientNames.filter { it !in ingredientsToRemove }
                android.util.Log.d("DynamicMenu", "🥘 Ingredients: initial=${initialIngredientNames.size}, removed=${ingredientsToRemove.size}, kept=${namesToKeep.size}")

                // Map ingredients to CartIngredientDto with intensity information from menu item
                val finalIngredientsDto: List<CartIngredientDto> = namesToKeep.map { name ->
                    // Find the original ingredient from menu item to get intensity type and color
                    val originalIngredient = menuItem.defaultIngredients.find { it.name == name }
                    // Get the actual intensity value selected by the user (default to 0.5 if not set)
                    val selectedIntensityValue = ingredientIntensities[name] ?: 0.5f
                    val ingredientDto = CartIngredientDto(
                        name = name,
                        isDefault = true,
                        intensityType = originalIngredient?.intensityType,
                        intensityColor = originalIngredient?.intensityColor,
                        intensityValue = if (originalIngredient?.supportsIntensity == true) selectedIntensityValue.toDouble() else null
                    )
                    android.util.Log.d("DynamicMenu", "  - $name: type=${ingredientDto.intensityType}, value=${ingredientDto.intensityValue}, color=${ingredientDto.intensityColor}")
                    ingredientDto
                }

                // --- 3. Construct AddToCartRequest DTO ---
                val menuItemId = menuItem.id

                val request = AddToCartRequest(
                    menuItemId = menuItemId,
                    quantity = quantity,
                    name = menuItem.name,
                    chosenIngredients = finalIngredientsDto,
                    chosenOptions = finalSelectedOptions,
                    calculatedPrice = unitPrice, // Discounted price if applicable
                    
                    // 🎯 Deal information
                    originalPrice = originalPrice + optionsPrice, // Original total price (with options)
                    discountPercentage = menuItem.discountPercentage,
                    dealId = menuItem.activeDealId
                )

                android.util.Log.d("DynamicMenu", "📝 Request DTO:")
                android.util.Log.d("DynamicMenu", "  - menuItemId: $menuItemId")
                android.util.Log.d("DynamicMenu", "  - name: ${request.name}")
                android.util.Log.d("DynamicMenu", "  - quantity: ${request.quantity}")
                android.util.Log.d("DynamicMenu", "  - calculatedPrice: ${request.calculatedPrice}")
                android.util.Log.d("DynamicMenu", "  - originalPrice: ${request.originalPrice}")
                android.util.Log.d("DynamicMenu", "  - discountPercentage: ${request.discountPercentage}%")
                android.util.Log.d("DynamicMenu", "  - dealId: ${request.dealId}")
                android.util.Log.d("DynamicMenu", "  - ingredients count: ${request.chosenIngredients.size}")
                android.util.Log.d("DynamicMenu", "  - options count: ${request.chosenOptions.size}")

                // Call the existing 'addItem' function in CartViewModel
                android.util.Log.d("DynamicMenu", "🚀 Calling cartViewModel.addItem()...")
                cartViewModel.addItem(
                    request = request
                )
                android.util.Log.d("DynamicMenu", "✅ addItem() call completed")

                selectedItemForCustomization = null
            }
        )
    }
}

// -----------------------------------------------------------------------------
// ANIMATED BOUNCING ICON COMPOSABLE
// -----------------------------------------------------------------------------

@Composable
fun AnimatedBouncingIcon(
    emoji: String,
    fontSize: TextUnit,
    delayMillis: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )
    
    Text(
        text = emoji,
        fontSize = fontSize,
        modifier = Modifier
            .graphicsLayer {
                translationY = bounceOffset
            }
    )
}

// -----------------------------------------------------------------------------
// CUSTOMIZATION OVERLAY AND HELPERS
// -----------------------------------------------------------------------------

@Composable
fun ItemCustomizationOverlay(
    item: MenuItem,
    onDismiss: () -> Unit,
    onConfirmAddToCart: (MenuItem, Int, Set<String>, Set<Option>, Map<String, Float>) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var ingredientsToRemove by remember { mutableStateOf(setOf<String>()) }
    var selectedOptions by remember { mutableStateOf(setOf<Option>()) }
    // Track intensity values for each ingredient (ingredient name -> intensity value)
    var ingredientIntensities by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }

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
                .background(AppCartButtonYellow.copy(alpha = 0.15f))
        ) {
            // Top Header with back, dots, and heart
            TopCustomizationHeader(onDismiss = onDismiss)
            
            // Product Info Card (rounded top corners, off-white background) - Takes up most of the screen
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8F0) // Off-white/beige
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        // Smaller Image - scrolls with content
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        spotColor = Color.Black.copy(alpha = 0.2f)
                                    )
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(3.dp)
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.placeholder),
                                    error = painterResource(id = R.drawable.placeholder),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                        
                        // Quantity and Price Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            QuantitySelectorRedesigned(
                                quantity = quantity,
                                onQuantityChange = { quantity = it }
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${String.format("%.2f", finalTotal / quantity)} TND",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppDarkText
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Product Title
                        Text(
                            text = item.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDarkText,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        // Description (if available) - More compact
                        Text(
                            text = "Fresh ingredients with premium quality. Customize your order to your taste preferences.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        // 1. Ingredients (Removal) - Main focus
                        IngredientsCustomizer(
                            ingredients = item.defaultIngredients,
                            ingredientsToRemove = ingredientsToRemove,
                            ingredientIntensities = ingredientIntensities,
                            onToggleIngredient = { ingredient ->
                                ingredientsToRemove = if (ingredientsToRemove.contains(ingredient)) {
                                    ingredientsToRemove - ingredient
                                } else {
                                    ingredientsToRemove + ingredient
                                }
                            },
                            onIntensityChange = { ingredientName, intensityValue ->
                                ingredientIntensities = ingredientIntensities + (ingredientName to intensityValue)
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
                    }
                }
            }
            
            // Add to Cart Button (Fixed at bottom)
            CustomizationFooter(
                total = finalTotal,
                onAddToCart = {
                    onConfirmAddToCart(item, quantity, ingredientsToRemove, selectedOptions, ingredientIntensities)
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
            
            // Show discounted price if applicable
            if (item.discountedPrice != null && item.discountPercentage != null && item.discountPercentage > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Discount badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF5722),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "-${item.discountPercentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    
                    // Old price (strikethrough)
                    Text(
                        "${item.priceDT} TND",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    
                    // New price
                    Text(
                        "${item.discountedPrice} TND",
                        color = AppCartButtonYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // Normal price
                Text(
                    "${item.priceDT} TND",
                    color = AppCartButtonYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
fun TopCustomizationHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun QuantitySelectorRedesigned(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minus button - Yellow square
        Button(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCartButtonYellow,
                contentColor = AppDarkText
            ),
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = quantity > 1
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease Quantity",
                tint = AppDarkText,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Quantity number - White background
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                quantity.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppDarkText
            )
        }
        
        // Plus button - Yellow square
        Button(
            onClick = { onQuantityChange(quantity + 1) },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCartButtonYellow,
                contentColor = AppDarkText
            ),
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase Quantity",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// INTENSITY TYPE ICON/COLOR HELPERS (Using Backend IntensityType)
// -----------------------------------------------------------------------------

@Composable
fun getIntensityIcons(intensityType: com.example.damprojectfinal.core.dto.menu.IntensityType?, intensityValue: Float): List<Pair<String, Int>> {
    return when (intensityType) {
        com.example.damprojectfinal.core.dto.menu.IntensityType.COFFEE -> {
            when {
                intensityValue >= 0.8f -> listOf("☕" to 0, "☕" to 100)
                intensityValue >= 0.3f -> listOf("☕" to 0)
                else -> listOf("☕" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.HARISSA -> {
            when {
                intensityValue >= 0.8f -> listOf("🔥" to 0)
                intensityValue >= 0.3f -> listOf("🌶️" to 0, "🌶️" to 150)
                else -> listOf("🌶️" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SAUCE -> {
            when {
                intensityValue >= 0.8f -> listOf("🍯" to 0, "🍯" to 100)
                intensityValue >= 0.3f -> listOf("🍯" to 0)
                else -> listOf("🍯" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SPICE -> {
            when {
                intensityValue >= 0.8f -> listOf("🌿" to 0, "🌿" to 100)
                intensityValue >= 0.3f -> listOf("🌿" to 0)
                else -> listOf("🌿" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SUGAR -> {
            when {
                intensityValue >= 0.8f -> listOf("🍬" to 0, "🍬" to 100)
                intensityValue >= 0.3f -> listOf("🍬" to 0)
                else -> listOf("🍬" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SALT -> {
            when {
                intensityValue >= 0.8f -> listOf("🧂" to 0, "🧂" to 100)
                intensityValue >= 0.3f -> listOf("🧂" to 0)
                else -> listOf("🧂" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.PEPPER -> {
            when {
                intensityValue >= 0.8f -> listOf("🫚" to 0, "🫚" to 100)
                intensityValue >= 0.3f -> listOf("🫚" to 0)
                else -> listOf("🫚" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.CHILI -> {
            when {
                intensityValue >= 0.8f -> listOf("🔥" to 0)
                intensityValue >= 0.3f -> listOf("🌶️" to 0, "🌶️" to 150)
                else -> listOf("🌶️" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.GARLIC -> {
            when {
                intensityValue >= 0.8f -> listOf("🧄" to 0, "🧄" to 100)
                intensityValue >= 0.3f -> listOf("🧄" to 0)
                else -> listOf("🧄" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.LEMON -> {
            when {
                intensityValue >= 0.8f -> listOf("🍋" to 0, "🍋" to 100)
                intensityValue >= 0.3f -> listOf("🍋" to 0)
                else -> listOf("🍋" to 0)
            }
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.CUSTOM, null -> {
            when {
                intensityValue >= 0.8f -> listOf("⭐" to 0, "⭐" to 100)
                intensityValue >= 0.3f -> listOf("⭐" to 0)
                else -> listOf("⭐" to 0)
            }
        }
    }
}

fun getIntensityColor(intensityType: com.example.damprojectfinal.core.dto.menu.IntensityType?, intensityColorHex: String?, intensityValue: Float): Color {
    // If backend provided a color, parse it and adjust by intensity
    if (intensityColorHex != null) {
        try {
            val baseColor = Color(android.graphics.Color.parseColor(intensityColorHex))
            // Adjust brightness based on intensity
            return Color(
                red = (baseColor.red * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                green = (baseColor.green * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f),
                blue = (baseColor.blue * (0.5f + intensityValue * 0.5f)).coerceIn(0f, 1f)
            )
        } catch (e: Exception) {
            // Fall through to default colors
        }
    }
    
    // Default colors based on type
    return when (intensityType) {
        com.example.damprojectfinal.core.dto.menu.IntensityType.COFFEE -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.15f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.1f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.HARISSA, 
        com.example.damprojectfinal.core.dto.menu.IntensityType.CHILI -> {
            Color(
                red = (0.6f + intensityValue * 0.4f).coerceIn(0f, 1f),
                green = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SAUCE -> {
            Color(
                red = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SPICE -> {
            Color(
                red = (0.8f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.5f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f - intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SUGAR -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.7f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.SALT -> {
            Color(
                red = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                green = (0.85f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.PEPPER -> {
            Color(
                red = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.2f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.GARLIC -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                blue = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.LEMON -> {
            Color(
                red = (0.95f + intensityValue * 0.05f).coerceIn(0f, 1f),
                green = (0.9f + intensityValue * 0.1f).coerceIn(0f, 1f),
                blue = (0.4f - intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
        com.example.damprojectfinal.core.dto.menu.IntensityType.CUSTOM, null -> {
            Color(
                red = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                green = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f),
                blue = (0.6f + intensityValue * 0.2f).coerceIn(0f, 1f)
            )
        }
    }
}

// Custom Slider with Vertical Bar Thumb Design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val trackHeight = 6.dp
    val thumbWidth = 3.dp
    val thumbHeight = 20.dp
    val density = LocalDensity.current
    
    BoxWithConstraints(modifier = modifier.height(thumbHeight)) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        val thumbOffsetPx = value * (trackWidthPx - thumbWidthPx)
        val thumbOffset = with(density) { thumbOffsetPx.toDp() }
        val activeTrackWidth = with(density) { (thumbOffsetPx + thumbWidthPx / 2).toDp() }
        
        // Inactive track (background) - rounded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(inactiveColor)
                .align(Alignment.Center)
        )
        
        // Active track (filled portion) - rounded left side
        Box(
            modifier = Modifier
                .width(activeTrackWidth)
                .height(trackHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = if (value >= 1f) 8.dp else 0.dp,
                        bottomEnd = if (value >= 1f) 8.dp else 0.dp
                    )
                )
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Vertical bar thumb
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .height(thumbHeight)
                .offset(x = thumbOffset)
                .background(activeColor)
                .align(Alignment.CenterStart)
        )
        
        // Small dot at the end of inactive track
        if (value < 1f) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(inactiveColor)
                    .offset(x = maxWidth - 4.dp)
                    .align(Alignment.CenterEnd)
            )
        }
        
        // Invisible touch target for interaction
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            thumb = {
                Box(modifier = Modifier.size(0.dp))
            },
            track = {
                Box(modifier = Modifier.fillMaxSize())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsCustomizer(
    ingredients: List<IngredientDto>,
    ingredientsToRemove: Set<String>,
    ingredientIntensities: Map<String, Float>,
    onToggleIngredient: (String) -> Unit,
    onIntensityChange: (String, Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Customize Ingredients",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppDarkText,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Tap to remove • Adjust intensity with slider",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ingredients.forEach { ingredient ->
            val isSelected = !ingredientsToRemove.contains(ingredient.name)
            
            if (ingredient.supportsIntensity == true) {
                // Display as slider with color effect - Professional design
                // Use stored intensity value or default to 0.5f
                var intensityValue by remember(ingredient.name) { 
                    mutableStateOf(ingredientIntensities[ingredient.name] ?: 0.5f) 
                }
                // Update stored intensity when slider changes
                LaunchedEffect(intensityValue) {
                    onIntensityChange(ingredient.name, intensityValue)
                }
                val intensityColor = getIntensityColor(ingredient.intensityType, ingredient.intensityColor, intensityValue)
                val intensityIcons = getIntensityIcons(ingredient.intensityType, intensityValue)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppCardBackground else Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 2.dp else 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Ingredient name row - clickable to remove
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleIngredient(ingredient.name) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Visual indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) AppCartButtonYellow else Color.LightGray
                                        )
                                )
                                Text(
                                    ingredient.name, 
                                    color = if (isSelected) AppDarkText else Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                            // Tap to remove hint
                            if (isSelected) {
                                Text(
                                    "Tap to remove",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Intensity slider row - Custom design with vertical bar thumb
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIntensitySlider(
                                value = intensityValue,
                                onValueChange = { intensityValue = it },
                                activeColor = intensityColor,
                                inactiveColor = intensityColor.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f)
                            )
                            // Intensity icons based on ingredient type with bouncing animation - moved to the right
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                intensityIcons.forEach { (emoji, delay) ->
                                    AnimatedBouncingIcon(
                                        emoji = emoji,
                                        fontSize = if (intensityValue >= 0.8f) 22.sp else 20.sp,
                                        delayMillis = delay
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Display as regular ingredient - Professional card design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleIngredient(ingredient.name) }
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppCardBackground else Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 2.dp else 0.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Visual indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AppPrimaryRed else Color.LightGray
                                    )
                            )
                            Text(
                                ingredient.name, 
                                color = if (isSelected) AppDarkText else Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                        // Tap to remove hint
                        if (isSelected) {
                            Text(
                                "Tap to remove",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
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
                    text = String.format("+%.2f TND", option.price),
                    color = AppCartButtonYellow,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF8F0), // Off-white/beige to match card
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppDarkText
                )
                Text(
                    String.format("%.2f TND", total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppCartButtonYellow
                )
            }

            Button(
                onClick = {
                    android.util.Log.d("CustomizationFooter", "🔘 Add To Cart BUTTON CLICKED!")
                    onAddToCart()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppCartButtonYellow
                )
            ) {
                Text(
                    text = "Add To Cart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
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
    Column(modifier = Modifier.fillMaxWidth().background(Color.White).statusBarsPadding()) {
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
                                containerColor = AppCartButtonYellow,
                                contentColor = AppDarkText
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
fun MenuItemCard(
    item: MenuItem, 
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Apply width constraint from modifier first, then fillMaxWidth only if no width is set
    val finalModifier = if (modifier == Modifier) {
        Modifier.fillMaxWidth()
    } else {
        modifier // Use custom modifier as-is (may include width constraint)
    }
    
    Card(
        modifier = finalModifier
            .height(260.dp) // Slightly increased for better spacing
            .clickable { onAddClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background circle for image
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF8F0)) // Light cream background
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.placeholder),
                        error = painterResource(id = R.drawable.placeholder),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            // Middle Section: Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Product Name
                Text(
                    text = item.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = AppDarkText,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Short Description
                Text(
                    text = item.description?.take(50) ?: "Fresh and delicious",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 13.sp
                )
            }

            // Bottom Section: Price and Add Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price Section with Deal Support
                if (item.discountedPrice != null && item.discountPercentage != null && item.discountPercentage > 0) {
                    // Show discount badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Discount Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF5722), // Red/Orange for discount
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "-${item.discountPercentage}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Old price (strikethrough)
                    Text(
                        text = "${String.format("%.2f", item.priceDT)} TND",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    )
                    
                    // New discounted price
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppCartButtonYellow,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${String.format("%.2f", item.discountedPrice)} TND",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    // Normal price (no discount)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppCartButtonYellow,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${String.format("%.2f", item.priceDT)} TND",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuBottomBar(totalOrderPrice: Float, onConfirmClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Total Price Section with better styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Price:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.2f TND", totalOrderPrice),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppCartButtonYellow
                    )
                }
            }
            
            // Confirm Order Button with modern design
            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppCartButtonYellow,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Confirm Order",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
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
        
        // Subtle divider
        Divider(
            color = Color(0xFFF0F0F0),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun CategoryChip(
    categoryItem: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AppCartButtonYellow else Color.White
    val textColor = if (isSelected) Color.White else AppDarkText
    
    Card(
        modifier = Modifier
            .size(width = 85.dp, height = 105.dp) // Slightly larger for better touch targets
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 3.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with subtle background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) 
                        else Color(0xFFFFF8F0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryItem.icon,
                    fontSize = 28.sp
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Name
            Text(
                text = categoryItem.displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
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
