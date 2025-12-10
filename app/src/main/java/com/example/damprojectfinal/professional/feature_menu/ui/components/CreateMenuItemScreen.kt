package com.example.damprojectfinal.professional.feature_menu.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.damprojectfinal.core.dto.menu.Category
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.example.damprojectfinal.core.dto.menu.OptionDto
import com.example.damprojectfinal.core.api.MenuItemApi
import com.example.damprojectfinal.core.api.IntensityTypeConfig
import com.example.damprojectfinal.core.`object`.FileUtil // Ensure this import is correct
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

// Note: Ensure your project has the R.raw file and Lottie dependency if using LottieAnimation.
// If R.raw is not available in this scope, you might need to use a hardcoded Int resource value
// or pass it as an argument, but for regeneration, we'll assume it exists.
// Example placeholder for the Lottie resource ID
// private val LOTTIE_EMPTY_STATE_ID = com.example.damprojectfinal.R.raw.restaurante_nao_encontrado

// Helper class for UI state (String price for user input)
data class CreateOptionUi(var name: String = "", var priceStr: String = "")

// Helper class for ingredient UI state (includes supportsIntensity flag and intensityType)
data class CreateIngredientUi(
    var name: String = "", 
    var supportsIntensity: Boolean = false,
    var intensityType: com.example.damprojectfinal.core.dto.menu.IntensityType? = null
)

// ---------- Modern Food App Colors (UPDATED) ----------
// Primary Color: A vibrant "Food Delivery" Yellow (0xFFFFC107)
private val PrimaryBrand = Color(0xFFFFC107)
private val BackgroundColor = Color(0xFFF6F6F9) // Very light grey/white mix
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = Color(0xFF9A9A9D)
private val InputBackground = Color(0xFFEFEEEE) // Soft filled input background
private val ErrorColor = Color(0xFFD32F2F) // Dedicated Error Color
private val EmptyStateGray = Color(0xFFBDBDBD) // Color for empty state background

// ---------- RANDOM ICON UTILITIES ----------
// Huge list of 30+ food-related Icons
private val FoodIcons = listOf(
    // Fast Food & General
    Icons.Default.Fastfood,
    Icons.Default.LocalPizza,
    Icons.Default.KebabDining,
    Icons.Default.LunchDining,
    Icons.Default.DinnerDining,
    Icons.Default.BreakfastDining,
    Icons.Default.BrunchDining,
    Icons.Default.TakeoutDining,
    Icons.Default.RamenDining,
    Icons.Default.Bento,
    Icons.Default.Tapas,
    Icons.Default.SetMeal,

    // Drinks
    Icons.Default.LocalCafe,
    Icons.Default.EmojiFoodBeverage,
    Icons.Default.LocalBar,
    Icons.Default.Liquor,
    Icons.Default.LocalDrink,
    Icons.Default.SportsBar,
    Icons.Default.Nightlife,
    Icons.Default.WineBar,

    // Sweets & Baking
    Icons.Default.Cake,
    Icons.Default.Cookie,
    Icons.Default.Icecream,
    Icons.Default.BakeryDining,
    Icons.Default.Kitchen,

    // Ingredients & Nature
    Icons.Default.Egg,
    Icons.Default.RiceBowl,
    Icons.Default.SoupKitchen,
    Icons.Default.Grass, // Veggie
    Icons.Default.WaterDrop,

    // Utensils & Places
    Icons.Default.LocalDining,
    Icons.Default.Restaurant,
    Icons.Default.RestaurantMenu,
    Icons.Default.Storefront
)

// Randomly selects one icon from the list
@Composable
fun rememberRandomFoodIcon(): State<ImageVector> {
    return remember {
        mutableStateOf(FoodIcons.random())
    }
}

// --------------------------------------------------------------------------------------
// SCREEN: CreateMenuItemScreen
// --------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMenuItemScreen(
    navController: NavController,
    professionalId: String,
    viewModel: MenuViewModel,
    context: Context
) {
    // ---------- Form State ----------
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf<CreateIngredientUi>() }
    val options = remember { mutableStateListOf<CreateOptionUi>() }

    var newIngredient by remember { mutableStateOf("") }
    var newIngredientSupportsIntensity by remember { mutableStateOf(false) }
    var newIngredientIntensityType by remember { mutableStateOf<IntensityType?>(null) }
    var showIntensityTypeSelector by remember { mutableStateOf(false) }
    var newOptionName by remember { mutableStateOf("") }
    var newOptionPrice by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val isCategoryDropdownExpanded = remember { mutableStateOf(false) }

    // ---------- Random Icons State ----------
    // We generate these once when the screen loads so they don't change on every recomposition
    val ingredientAddIcon by rememberRandomFoodIcon()
    val optionAddIcon by rememberRandomFoodIcon()

    // ---------- Image Picker ----------
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    // ---------- ViewModel State ----------
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        val currentState = uiState
        when (currentState) {
            is MenuItemUiState.Success -> {
                showSuccessDialog = true
            }
            is MenuItemUiState.Error -> {
                errorMessage = currentState.message
                showErrorDialog = true
            }
            else -> { /* Loading or Idle - do nothing */ }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add New Dish",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            // Floating-style Bottom Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                // Error Display
                if (localError != null || uiState is MenuItemUiState.Error) {
                    val errorMsg = localError ?: (uiState as? MenuItemUiState.Error)?.message ?: ""
                    Surface(
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMsg,
                            color = ErrorColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 20.dp, spotColor = Color.Black.copy(alpha = 0.2f))
                        .background(SurfaceWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            localError = null

                            val mainPrice = priceText.toDoubleOrNull()
                            val selectedCategory = Category.values().find { it.name == category.uppercase() }

                            if (name.isBlank() || category.isBlank()) {
                                localError = "Please add a name and category"
                                return@Button
                            }
                            if (mainPrice == null || mainPrice < 0.0) {
                                localError = "Price must be valid"
                                return@Button
                            }

                            if (imageUri == null) {
                                localError = "Don't forget the photo! 📸"
                                return@Button
                            }

                            val fileWithMime = FileUtil.getFileWithMime(context, imageUri!!)
                            if (fileWithMime == null) {
                                localError = "Failed to process image"
                                return@Button
                            }

                            val dto = CreateMenuItemDto(
                                professionalId = professionalId,
                                name = name.trim(),
                                description = description.trim(),
                                price = mainPrice,
                                category = selectedCategory!!,
                                ingredients = ingredients.map { 
                                    IngredientDto(
                                        name = it.name.trim(), 
                                        isDefault = true,
                                        supportsIntensity = if (it.supportsIntensity) true else null,
                                        intensityType = it.intensityType
                                    ) 
                                },
                                options = options.mapNotNull {
                                    val price = it.priceStr.toDoubleOrNull()
                                    if (it.name.isBlank() || price == null) null
                                    else OptionDto(it.name.trim(), price)
                                }
                            )

                            viewModel.createMenuItem(dto, fileWithMime, "YOUR_JWT_TOKEN_HERE")
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = uiState !is MenuItemUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBrand,
                            contentColor = TextPrimary, // Black text on Yellow
                            disabledContainerColor = PrimaryBrand.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(30.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (uiState is MenuItemUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextPrimary, // Dark spinner on yellow
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Save to Menu", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ---------- SECTION 1: VISUALS (Image) ----------
            item {
                Text(
                    "Dish Photo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f) // Landscape food aspect ratio
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceWhite)
                        .clickable { imagePicker.launch("image/*") }
                        .then(
                            if (imageUri == null) Modifier.dashedBorder(1.dp, TextSecondary, 20.dp)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Food Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Edit Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = PrimaryBrand // Yellow icon
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Upload Photo", fontWeight = FontWeight.Bold, color = PrimaryBrand) // Yellow text
                            Text("Good food needs a good look", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // ---------- SECTION 2: ESSENTIALS ----------
            item {
                Text(
                    "Essentials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Name Input
                        FoodAppTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Dish Name (e.g. Spicy Burger)",
                            label = "Name",
                            bgColor = InputBackground
                        )

                        Spacer(Modifier.height(16.dp))

                        // Price & Category Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                FoodAppTextField(
                                    value = priceText,
                                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                                    placeholder = "0.00",
                                    label = "Price (TND)",
                                    bgColor = InputBackground,
                                    keyboardType = KeyboardType.Decimal
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            // Category Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                FoodAppTextField(
                                    value = category.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                                    onValueChange = {},
                                    placeholder = "Select",
                                    label = "Category",
                                    readOnly = true,
                                    bgColor = InputBackground,
                                    onClick = { isCategoryDropdownExpanded.value = true },
                                    trailingIcon = {
                                        Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                                    }
                                )
                                DropdownMenu(
                                    expanded = isCategoryDropdownExpanded.value,
                                    onDismissRequest = { isCategoryDropdownExpanded.value = false },
                                    modifier = Modifier.background(SurfaceWhite)
                                ) {
                                    Category.values().forEach { categoryOption ->
                                        DropdownMenuItem(
                                            text = { Text(categoryOption.name, color = TextPrimary) },
                                            onClick = {
                                                category = categoryOption.name
                                                isCategoryDropdownExpanded.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Description
                        FoodAppTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Describe the taste, texture, and ingredients...",
                            label = "Description",
                            bgColor = InputBackground,
                            singleLine = false,
                            minLines = 3
                        )
                    }
                }
            }

            // ---------- SECTION 3: INGREDIENTS ----------
            item {
                Text(
                    "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "What's inside? Users love transparency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    // Input Row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                FoodAppTextField(
                                    value = newIngredient,
                                    onValueChange = { newIngredient = it },
                                    placeholder = "Add ingredient...",
                                    bgColor = InputBackground
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            // Enhanced 'Add' Button Style with Random Icon
                            IconButton(
                                onClick = {
                                    val trimmedName = newIngredient.trim()
                                    if (trimmedName.isNotBlank() && !ingredients.any { it.name == trimmedName }) {
                                        ingredients.add(CreateIngredientUi(
                                            name = trimmedName,
                                            supportsIntensity = newIngredientSupportsIntensity,
                                            intensityType = if (newIngredientSupportsIntensity) newIngredientIntensityType else null
                                        ))
                                        newIngredient = ""
                                        newIngredientSupportsIntensity = false
                                        newIngredientIntensityType = null
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PrimaryBrand) // Solid Yellow background
                                    .size(48.dp)
                            ) {
                                // CHANGED: Tint set to Color.White
                                Icon(ingredientAddIcon, "Add Ingredient", tint = Color.White)
                            }
                        }
                        // Intensity Toggle
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = newIngredientSupportsIntensity,
                                    onCheckedChange = { 
                                        newIngredientSupportsIntensity = it
                                        if (!it) {
                                            newIngredientIntensityType = null
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryBrand,
                                        uncheckedColor = TextSecondary
                                    )
                                )
                                Text(
                                    text = "Enable intensity slider",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            // Intensity Type Selector (shown only when intensity is enabled)
                            if (newIngredientSupportsIntensity) {
                                IntensityTypeSelector(
                                    selectedType = newIngredientIntensityType,
                                    onTypeSelected = { newIngredientIntensityType = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Chips / List
                    if (ingredients.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ingredients.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)) // subtle background
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        // Small circular indicator (now yellow)
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(PrimaryBrand, CircleShape)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            if (item.supportsIntensity) {
                                                Text(
                                                    "Intensity: Adjustable",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = PrimaryBrand,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { ingredients.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Outlined.Delete, "Remove", tint = ErrorColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---------- SECTION 4: EXTRAS / OPTIONS ----------
            item {
                Text(
                    "Extras & Add-ons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    // Input Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            FoodAppTextField(
                                value = newOptionName,
                                onValueChange = { newOptionName = it },
                                placeholder = "Name (e.g. Cheese)",
                                bgColor = InputBackground
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.width(80.dp)) {
                            FoodAppTextField(
                                value = newOptionPrice,
                                onValueChange = { newOptionPrice = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = "Price",
                                bgColor = InputBackground,
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        // Enhanced 'Add' Button Style with Random Icon
                        IconButton(
                            onClick = {
                                val price = newOptionPrice.toDoubleOrNull()
                                if (newOptionName.isNotBlank() && price != null && price >= 0.0) {
                                    options.add(CreateOptionUi(newOptionName.trim(), newOptionPrice.trim()))
                                    newOptionName = ""
                                    newOptionPrice = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryBrand) // Solid Yellow background
                                .size(48.dp)
                        ) {
                            // CHANGED: Tint set to Color.White
                            Icon(optionAddIcon, "Add Option", tint = Color.White)
                        }
                    }

                    if (options.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(16.dp))

                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(option.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("+ ${option.priceStr} TND", style = MaterialTheme.typography.bodySmall, color = PrimaryBrand) // Yellow price
                                }
                                IconButton(
                                    onClick = { options.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Outlined.Delete, "Remove", tint = ErrorColor)
                                }
                            }
                        }
                    }
                }
            }

            // ---------- NEW ITEM: EMPTY STATE (Lottie Animation/Placeholder) ----------
            // Display empty state if the category is selected but the essential fields (name) are empty
            if (name.isBlank() && category.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Lottie Animation Placeholder: Replace this with your actual LottieAnimation composable
                        // and resource ID (e.g., com.example.damprojectfinal.R.raw.restaurante_nao_encontrado)

                        // Icon placeholder (as a stand-in for the Lottie animation for safe compilation)
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = "No dish details",
                            tint = EmptyStateGray,
                            modifier = Modifier.size(150.dp)
                        )

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Dish Details Required",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Please fill in the Name, Price, and Description fields to create the item in the '${category}' category.",
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Spacer for the floating button at bottom
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        CreateSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                scope.launch {
                    delay(300)
                    navController.popBackStack()
                    viewModel.resetUiState()
                }
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        CreateErrorDialog(
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }
}

// --------------------------------------------------------------------------------------
// CUSTOM COMPONENTS AND UTILITIES
// --------------------------------------------------------------------------------------

@Composable
fun FoodAppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    bgColor: Color,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (onClick != null) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) {
                    onClick()
                }
            }
        }
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.6f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                disabledContainerColor = bgColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            readOnly = readOnly,
            enabled = true,
            trailingIcon = trailingIcon,
            interactionSource = interactionSource
        )
    }
}

// --------------------------------------------------
// COMPONENT: Success Dialog for Create Screen
// --------------------------------------------------
@Composable
fun CreateSuccessDialog(
    onDismiss: () -> Unit
) {
    val scale = rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated Success Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale.value)
                        .background(
                            PrimaryBrand.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = PrimaryBrand,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Success Title
                Text(
                    text = "Success!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Success Message
                Text(
                    text = "Item created successfully!",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                
                Spacer(Modifier.height(8.dp))
                
                // OK Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBrand,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "OK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Error Dialog for Create Screen
// --------------------------------------------------
@Composable
fun CreateErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val scale = rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated Error Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale.value)
                        .background(
                            ErrorColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = ErrorColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Error Title
                Text(
                    text = "Creation Failed",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Error Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(Modifier.height(8.dp))
                
                // OK Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "OK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// INTENSITY TYPE SELECTOR COMPONENT
// -----------------------------------------------------------------------------

@Composable
fun IntensityTypeSelector(
    selectedType: IntensityType?,
    onTypeSelected: (IntensityType?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map intensity types to emoji icons and labels
    val intensityTypes = listOf(
        IntensityType.COFFEE to ("☕" to "Coffee"),
        IntensityType.HARISSA to ("🌶️" to "Harissa"),
        IntensityType.SAUCE to ("🍯" to "Sauce"),
        IntensityType.SPICE to ("🌿" to "Spice"),
        IntensityType.SUGAR to ("🍬" to "Sugar"),
        IntensityType.SALT to ("🧂" to "Salt"),
        IntensityType.PEPPER to ("🫚" to "Pepper"),
        IntensityType.CHILI to ("🌶️" to "Chili"),
        IntensityType.GARLIC to ("🧄" to "Garlic"),
        IntensityType.LEMON to ("🍋" to "Lemon"),
        IntensityType.CUSTOM to ("⭐" to "Custom")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Intensity Type:",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Horizontal scrollable row of type chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(intensityTypes.size) { index ->
                val typePair = intensityTypes[index]
                val type = typePair.first
                val emojiLabelPair = typePair.second
                val emoji = emojiLabelPair.first
                val label = emojiLabelPair.second
                val isSelected = selectedType == type
                
                Surface(
                    onClick = { 
                        onTypeSelected(if (isSelected) null else type)
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PrimaryBrand else InputBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PrimaryBrand else Color(0xFFE0E0E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 16.sp
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// Utility for dashed border
fun Modifier.dashedBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        this.then(
            Modifier.drawBehind {
                val stroke = Stroke(
                    width = strokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
                drawRoundRect(
                    color = color,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
                )
            }
        )
    }
)

// Extension for String capitalization
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}