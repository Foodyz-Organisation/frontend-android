package com.example.damprojectfinal.professional.feature_menu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.OptionDto
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.ItemDetailsUiState

// Custom Brand Colors
private val PrimaryBrandOrange = Color(0xFFFA4A0C) // Retained for destructive actions
private val BrandYellow = Color(0xFFFFC107) // ⭐ NEW PRIMARY ACCENT
private val BackgroundLight = Color(0xFFF6F6F9)
private val CardBackground = Color.White // Explicitly using White for cards
private val CategoryBackgroundGray = Color(0xFFF0F0F0) // Used for subtle dividers/backgrounds
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = Color(0xFF9A9A9D)
private const val BASE_URL = "http://10.0.2.2:3000/"

// --- State Holder Class for Hoisting (Image path added) ---
class EditableItemState(
    initialName: String,
    initialDesc: String,
    initialPrice: Double,
    initialIngredients: List<IngredientDto>,
    initialOptions: List<OptionDto>,
    initialImage: String? // Added image path
) {
    var name by mutableStateOf(initialName)
    var description by mutableStateOf(initialDesc)
    var priceStr by mutableStateOf(initialPrice.toString())
    var imagePath by mutableStateOf(initialImage ?: "") // State for image path
    val ingredientsList = mutableStateListOf(*initialIngredients.toTypedArray())
    val optionsList = mutableStateListOf(*initialOptions.toTypedArray())

    fun toUpdateDto(): UpdateMenuItemDto {
        val price = priceStr.toDoubleOrNull() ?: 0.0
        return UpdateMenuItemDto(
            name = name,
            description = description,
            price = price,
            ingredients = ingredientsList.toList(),
            options = optionsList.toList()
        )
    }
}

@Composable
fun rememberEditableItemState(
    initialName: String,
    initialDesc: String,
    initialPrice: Double,
    initialIngredients: List<IngredientDto>,
    initialOptions: List<OptionDto>,
    initialImage: String?
): EditableItemState = remember {
    EditableItemState(
        initialName, initialDesc, initialPrice, initialIngredients, initialOptions, initialImage
    )
}

// --------------------------------------------------
// SCREEN: Main Composable
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    professionalId: String,
    viewModel: MenuViewModel,
    navController: NavController
) {
    val dummyToken = "YOUR_AUTH_TOKEN"
    LaunchedEffect(itemId) {
        viewModel.fetchMenuItemDetails(itemId, dummyToken)
    }

    val uiState by viewModel.itemDetailsUiState.collectAsState()
    var editableState by remember { mutableStateOf<EditableItemState?>(null) }


    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Edit Dish Details", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        bottomBar = {
            // Check if we have a successful state AND the editable state has been initialized
            if (uiState is ItemDetailsUiState.Success && editableState != null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = CardBackground // Use white for the bottom bar surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                val updateDto = editableState!!.toUpdateDto()
                                viewModel.updateMenuItem(itemId, professionalId, updateDto, dummyToken)
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandYellow,
                                contentColor = TextPrimary // Dark text on yellow button
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Changes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is ItemDetailsUiState.Loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = BrandYellow
                )
                is ItemDetailsUiState.Error -> Text("Error: ${(uiState as ItemDetailsUiState.Error).message}", Modifier.align(Alignment.Center), color = PrimaryBrandOrange) // Changed error color here
                is ItemDetailsUiState.Success -> {
                    val item = (uiState as ItemDetailsUiState.Success).item

                    // Initialize the editable state including the image
                    val state = rememberEditableItemState(
                        initialName = item.name,
                        initialDesc = item.description ?: "",
                        initialPrice = item.price,
                        initialIngredients = item.ingredients,
                        initialOptions = item.options,
                        initialImage = item.image
                    )
                    LaunchedEffect(item.id) {
                        editableState = state
                    }
                    EditItemContent(state = state)
                }
                else -> {}
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Edit Item Content (Single List View)
// --------------------------------------------------
@Composable
fun EditItemContent(
    state: EditableItemState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Image Section
        item {
            ImageSection(
                imagePath = state.imagePath,
                // In a real app, this callback would trigger an image picker/upload logic
                onImageSelect = { /* new path */ }
            )
        }

        // 2. Item Info Section (General Details)
        item {
            ItemInfoSection(
                name = state.name,
                onNameChange = { state.name = it },
                priceStr = state.priceStr,
                onPriceChange = { state.priceStr = it },
                description = state.description,
                onDescriptionChange = { state.description = it }
            )
        }

        // 3. Ingredients Editor Section
        item {
            IngredientsListEditor(state.ingredientsList)
        }

        // 4. Options Editor Section
        item {
            OptionsListEditor(state.optionsList)
        }

        // Add padding at the bottom to avoid FAB overlap
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

// --------------------------------------------------
// COMPONENT: Image Preview and Selection
// --------------------------------------------------
@Composable
fun ImageSection(
    imagePath: String,
    onImageSelect: (String) -> Unit // Placeholder for image selection logic
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Dish Photo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CategoryBackgroundGray) // Gray background if image fails to load
            ) {
                AsyncImage(
                    model = BASE_URL + imagePath,
                    contentDescription = "Dish Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Button for Image Update
                Button(
                    onClick = { /* TODO: Trigger image picker */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Image", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}


// --------------------------------------------------
// COMPONENT: Item Info Section (Now a standalone card)
// --------------------------------------------------
@Composable
fun ItemInfoSection(
    name: String, onNameChange: (String) -> Unit,
    priceStr: String, onPriceChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Basic Dish Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Divider(color = CategoryBackgroundGray)

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Dish Name", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = CategoryBackgroundGray
                )
            )

            // Price Field
            OutlinedTextField(
                value = priceStr,
                onValueChange = onPriceChange,
                label = { Text("Base Price (TND)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = CategoryBackgroundGray
                )
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Detailed Description", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = CategoryBackgroundGray
                )
            )
        }
    }
}

// --------------------------------------------------
// COMPONENT: Ingredients List Editor (Now a standalone card)
// --------------------------------------------------
@Composable
fun IngredientsListEditor(list: androidx.compose.runtime.snapshots.SnapshotStateList<IngredientDto>) {
    var newName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Key Ingredients (${list.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(color = CategoryBackgroundGray, modifier = Modifier.padding(bottom = 16.dp))

            // Add New Ingredient Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Ingredient Name", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandYellow,
                        unfocusedBorderColor = CategoryBackgroundGray
                    )
                )
                Button(
                    onClick = { if(newName.isNotBlank()) { list.add(IngredientDto(newName, true)); newName = "" } },
                    enabled = newName.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, "Add Ingredient", Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            // Current Ingredients List
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (list.isEmpty()) {
                    item { Text("No ingredients added yet. These are typically included by default.", color = TextSecondary, modifier = Modifier.padding(top = 8.dp)) }
                }
                items(list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
                        IconButton(
                            onClick = { list.remove(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Remove", tint = PrimaryBrandOrange) // ⭐ Changed tint to PrimaryBrandOrange
                        }
                    }
                    Divider(color = CategoryBackgroundGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Options List Editor (Now a standalone card)
// --------------------------------------------------
@Composable
fun OptionsListEditor(list: androidx.compose.runtime.snapshots.SnapshotStateList<OptionDto>) {
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Custom Options (${list.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider(color = CategoryBackgroundGray, modifier = Modifier.padding(bottom = 16.dp))

            // Add New Option Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Option Name (e.g., Large size)", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow, unfocusedBorderColor = CategoryBackgroundGray)
                )
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("Price (TND)", color = TextSecondary) },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow, unfocusedBorderColor = CategoryBackgroundGray)
                )
                Button(
                    onClick = {
                        val p = newPrice.toDoubleOrNull()
                        if(newName.isNotBlank() && p != null) { list.add(OptionDto(newName, p)); newName = ""; newPrice = "" }
                    },
                    enabled = newName.isNotBlank() && newPrice.toDoubleOrNull() != null,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, "Add Option", Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            // Current Options List
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (list.isEmpty()) {
                    item { Text("No custom options added yet. (e.g., Extra Cheese, Different Size)", color = TextSecondary, modifier = Modifier.padding(top = 8.dp)) }
                }
                items(list) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(
                            text = "+TND ${String.format("%.3f", item.price)}", // Display with TND
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBrandOrange // Using Orange for price emphasis
                        )
                        IconButton(
                            onClick = { list.remove(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Remove", tint = PrimaryBrandOrange) // ⭐ Changed tint to PrimaryBrandOrange
                        }
                    }
                    Divider(color = CategoryBackgroundGray, thickness = 0.5.dp)
                }
            }
        }
    }
}
