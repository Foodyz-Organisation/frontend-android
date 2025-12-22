package com.example.damprojectfinal.professional.feature_menu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.damprojectfinal.core.api.BaseUrlProvider
import com.example.damprojectfinal.core.`object`.FileUtil
import com.example.damprojectfinal.core.`object`.FileWithMime
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.IntensityType
import com.example.damprojectfinal.core.dto.menu.OptionDto
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.ItemDetailsUiState
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState

// Custom Brand Colors
private val PrimaryBrandOrange = Color(0xFFFA4A0C) // Retained for destructive actions
private val BrandYellow = Color(0xFFFFC107) // ⭐ NEW PRIMARY ACCENT
private val BackgroundLight = Color(0xFFF6F6F9)
private val CardBackground = Color.White // Explicitly using White for cards
private val CategoryBackgroundGray = Color(0xFFF0F0F0) // Used for subtle dividers/backgrounds
private val TextPrimary = Color(0xFF000000)
private val TextSecondary = Color(0xFF9A9A9D)

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
    var selectedImageUri by mutableStateOf<android.net.Uri?>(null) // Store selected image URI
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
    val updateUiState by viewModel.uiState.collectAsState()
    var editableState by remember { mutableStateOf<EditableItemState?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasInitiatedUpdate by remember { mutableStateOf(false) }
    
    // Observe update state and handle navigation/errors
    LaunchedEffect(updateUiState) {
        if (!hasInitiatedUpdate) return@LaunchedEffect
        
        when (val state = updateUiState) {
            is MenuItemUiState.Success -> {
                // Refetch item details to show updated data
                viewModel.fetchMenuItemDetails(itemId, dummyToken)
                showSuccessDialog = true
                hasInitiatedUpdate = false
            }
            is MenuItemUiState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
                hasInitiatedUpdate = false
            }
            else -> { /* Loading or Idle - do nothing */ }
        }
    }

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
                actions = {
                    // Delete button (trash icon)
                    if (uiState is ItemDetailsUiState.Success) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Item",
                                tint = PrimaryBrandOrange
                            )
                        }
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
                                
                                // Debug: Log the update DTO to verify ingredients are included
                                android.util.Log.d("DetailsMenu", "Update DTO: name=${updateDto.name}, ingredients count=${updateDto.ingredients?.size ?: 0}")
                                updateDto.ingredients?.forEachIndexed { index, ingredient ->
                                    android.util.Log.d("DetailsMenu", "Ingredient $index: name=${ingredient.name}, supportsIntensity=${ingredient.supportsIntensity}")
                                }
                                
                                // Check if there's a new image to upload
                                val imageFile: FileWithMime? = editableState!!.selectedImageUri?.let { uri ->
                                    FileUtil.getFileWithMime(context, uri)
                                }
                                
                                // Mark that we've initiated an update
                                hasInitiatedUpdate = true
                                
                                // Perform update
                                if (imageFile != null) {
                                    // Update with image (multipart)
                                    viewModel.updateMenuItemWithImage(itemId, professionalId, updateDto, imageFile, dummyToken)
                                } else {
                                    // Update without image (JSON only)
                                    viewModel.updateMenuItem(itemId, professionalId, updateDto, dummyToken)
                                }
                            },
                            enabled = updateUiState !is MenuItemUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandYellow,
                                contentColor = TextPrimary // Dark text on yellow button
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (updateUiState is MenuItemUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirm Changes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
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
                    EditItemContent(
                        state = state,
                        onImageSelect = { uri ->
                            // Store the selected image URI
                            state.selectedImageUri = uri
                        }
                    )
                }
                else -> {}
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteDialog && uiState is ItemDetailsUiState.Success) {
            val item = (uiState as ItemDetailsUiState.Success).item
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Item", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete \"${item.name}\"? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMenuItem(item.id, professionalId, dummyToken)
                            showDeleteDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandYellow,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Success Dialog
        if (showSuccessDialog) {
            SuccessDialog(
                onDismiss = {
                    showSuccessDialog = false
                    // Small delay to show success message, then navigate
                    scope.launch {
                        delay(300)
                        navController.popBackStack()
                        // Reset UI state after navigation
                        viewModel.resetUiState()
                    }
                }
            )
        }
        
        // Error Dialog
        if (showErrorDialog) {
            ErrorDialog(
                message = errorMessage,
                onDismiss = { showErrorDialog = false }
            )
        }
    }
}

// --------------------------------------------------
// COMPONENT: Edit Item Content (Single List View)
// --------------------------------------------------
@Composable
fun EditItemContent(
    state: EditableItemState,
    onImageSelect: (Uri) -> Unit
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
                onImageSelect = onImageSelect
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
    onImageSelect: (Uri) -> Unit // Changed to pass Uri directly
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                onImageSelect(it) // Pass Uri directly
            }
        }
    )
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
                // Show selected image if available, otherwise show original
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Dish Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = BaseUrlProvider.getFullImageUrl(imagePath),
                        contentDescription = "Dish Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Overlay Button for Image Update
                Button(
                    onClick = { 
                        // Launch image picker
                        imagePickerLauncher.launch("image/*")
                    },
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
            var supportsIntensity by remember { mutableStateOf(false) }
            var selectedIntensityType by remember { mutableStateOf<IntensityType?>(null) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        onClick = { 
                            if(newName.isNotBlank()) { 
                                list.add(IngredientDto(
                                    name = newName.trim(),
                                    isDefault = true,
                                    supportsIntensity = if (supportsIntensity) true else null,
                                    intensityType = if (supportsIntensity) selectedIntensityType else null
                                ))
                                newName = ""
                                supportsIntensity = false
                                selectedIntensityType = null
                            } 
                        },
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
                // Intensity Toggle
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = supportsIntensity,
                            onCheckedChange = { 
                                supportsIntensity = it
                                if (!it) {
                                    selectedIntensityType = null
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandYellow,
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
                    if (supportsIntensity) {
                        IntensityTypeSelectorForEdit(
                            selectedType = selectedIntensityType,
                            onTypeSelected = { selectedIntensityType = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name, 
                                    style = MaterialTheme.typography.bodyLarge, 
                                    color = TextPrimary, 
                                    fontWeight = FontWeight.Medium
                                )
                                if (item.supportsIntensity == true) {
                                    Text(
                                        "Intensity: Adjustable",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandYellow,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = { list.remove(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Remove", tint = PrimaryBrandOrange)
                            }
                        }
                        // Show slider if supportsIntensity is true
                        if (item.supportsIntensity == true) {
                            var intensityValue by remember(item.name) { mutableStateOf(0.5f) }
                            Spacer(Modifier.height(8.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Intensity",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        "${(intensityValue * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = intensityValue,
                                    onValueChange = { intensityValue = it },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = BrandYellow,
                                        activeTrackColor = BrandYellow,
                                        inactiveTrackColor = CategoryBackgroundGray
                                    )
                                )
                            }
                        }
                        Divider(color = CategoryBackgroundGray, thickness = 0.5.dp)
                    }
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

// --------------------------------------------------
// COMPONENT: Success Dialog (Beautiful Pop-up)
// --------------------------------------------------
@Composable
fun SuccessDialog(
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
            colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                            BrandYellow.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = BrandYellow,
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
                    text = "Item updated successfully!",
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
                        containerColor = BrandYellow,
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
// COMPONENT: Error Dialog (Beautiful Pop-up)
// --------------------------------------------------
@Composable
fun ErrorDialog(
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
            colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                            PrimaryBrandOrange.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = PrimaryBrandOrange,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Error Title
                Text(
                    text = "Update Failed",
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
                        containerColor = PrimaryBrandOrange,
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
// INTENSITY TYPE SELECTOR FOR EDIT SCREEN
// -----------------------------------------------------------------------------

@Composable
fun IntensityTypeSelectorForEdit(
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
                    color = if (isSelected) BrandYellow else CategoryBackgroundGray,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BrandYellow else Color(0xFFE0E0E0)
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
