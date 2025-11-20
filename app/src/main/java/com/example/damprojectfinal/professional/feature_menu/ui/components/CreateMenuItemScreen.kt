package com.example.damprojectfinal.professional.feature_menu.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.damprojectfinal.core.dto.menu.Category
import com.example.damprojectfinal.core.dto.menu.CreateMenuItemDto
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.OptionDto
import com.example.damprojectfinal.core.`object`.FileUtil
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel

// Helper class for UI state (String price for user input)
data class CreateOptionUi(var name: String = "", var priceStr: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMenuItemScreen(
    navController: NavController,
    professionalId: String,
    viewModel: MenuViewModel,
    context: Context
) {
    // ---------- Form State ----------
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") } // Added description field
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf<String>("Patty", "Bun") } // Start with defaults
    val options = remember { mutableStateListOf<CreateOptionUi>() }

    var newIngredient by remember { mutableStateOf("") } // New state for adding ingredients
    var newOptionName by remember { mutableStateOf("") }
    var newOptionPrice by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val isCategoryDropdownExpanded = remember { mutableStateOf(false) } // For category dropdown

    // ---------- Image Picker ----------
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    // ---------- ViewModel State ----------
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on successful creation
    LaunchedEffect(uiState) {
        if (uiState is MenuItemUiState.Success) {
            navController.popBackStack()
            viewModel.resetUiState() // Reset state after success
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Menu Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is MenuItemUiState.Error -> Text((uiState as MenuItemUiState.Error).message, color = MaterialTheme.colorScheme.error)
                    is MenuItemUiState.Loading -> { /* Handled in the button */ }
                    else -> {}
                }
                localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        localError = null

                        val mainPrice = priceText.toDoubleOrNull()
                        val selectedCategory = Category.values().find { it.name == category.uppercase() }

                        if (name.isBlank() || category.isBlank()) {
                            localError = "Name and Category are required"
                            return@Button
                        }
                        if (mainPrice == null || mainPrice < 0.0) {
                            localError = "Enter a valid price ≥ 0"
                            return@Button
                        }

                        if (imageUri == null) {
                            localError = "Please select an image"
                            return@Button
                        }

                        // Convert imageUri to FileWithMime
                        val fileWithMime = FileUtil.getFileWithMime(context, imageUri!!)
                        if (fileWithMime == null) {
                            localError = "Failed to read image file"
                            return@Button
                        }

                        // Build DTO
                        val dto = CreateMenuItemDto(
                            professionalId = professionalId,
                            name = name.trim(),
                            description = description.trim(),
                            price = mainPrice,
                            category = selectedCategory!!,
                            ingredients = ingredients.map { IngredientDto(it.trim(), isDefault = true) },
                            options = options.mapNotNull {
                                val price = it.priceStr.toDoubleOrNull()
                                if (it.name.isBlank() || price == null) null
                                else OptionDto(it.name.trim(), price)
                            }
                        )

                        // Call ViewModel
                        viewModel.createMenuItem(dto, fileWithMime, "YOUR_JWT_TOKEN_HERE")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is MenuItemUiState.Loading
                ) {
                    if (uiState is MenuItemUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm Item")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // ---------- Item Details ----------
            item {
                Text("Item Details", style = MaterialTheme.typography.titleLarge)
            }

            // Name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Price
            item {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Price") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }


            // ---------- Category Dropdown ----------
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { }, // Cannot be directly edited
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, null, Modifier.clickable { isCategoryDropdownExpanded.value = true })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCategoryDropdownExpanded.value = true }
                    )
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded.value,
                        onDismissRequest = { isCategoryDropdownExpanded.value = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Category.values().forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption.name) },
                                onClick = {
                                    category = categoryOption.name
                                    isCategoryDropdownExpanded.value = false
                                }
                            )
                        }
                    }
                }
            }

            // ---------- Image Uploader ----------
            item {
                Spacer(Modifier.height(16.dp))
                Text("Item Image", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("No Image Selected", color = Color.DarkGray)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = { imagePicker.launch("image/*") }) {
                    Text(if (imageUri != null) "Change Image" else "Select Image")
                }
            }

            // ---------- Ingredients Section ----------
            item {
                Spacer(Modifier.height(24.dp))
                Text("Ingredients", style = MaterialTheme.typography.titleLarge)
            }

            // Input for new ingredient
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newIngredient,
                        onValueChange = { newIngredient = it },
                        label = { Text("Add Ingredient Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newIngredient.isNotBlank() && !ingredients.contains(newIngredient.trim())) {
                                ingredients.add(newIngredient.trim())
                                newIngredient = ""
                            }
                        },
                        enabled = newIngredient.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                    }
                }
            }

            // List of ingredients
            itemsIndexed(ingredients) { index, ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(ingredient, modifier = Modifier.weight(1f))
                    IconButton(onClick = { ingredients.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Ingredient", tint = Color.Red)
                    }
                }
                Divider()
            }

            // ---------- Options Section ----------
            item {
                Spacer(Modifier.height(24.dp))
                Text("Options (e.g., Extra Cheese)", style = MaterialTheme.typography.titleLarge)
            }

            // Input for new option
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newOptionName,
                        onValueChange = { newOptionName = it },
                        label = { Text("Name") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = newOptionPrice,
                        onValueChange = { newOptionPrice = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val price = newOptionPrice.toDoubleOrNull()
                            if (newOptionName.isNotBlank() && price != null && price >= 0.0) {
                                options.add(CreateOptionUi(newOptionName.trim(), newOptionPrice.trim()))
                                newOptionName = ""
                                newOptionPrice = ""
                            }
                        },
                        enabled = newOptionName.isNotBlank() && newOptionPrice.toDoubleOrNull() != null,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Option")
                    }
                }
            }

            // List of options
            itemsIndexed(options) { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${option.name} (+${option.priceStr}$)", modifier = Modifier.weight(1f))
                    IconButton(onClick = { options.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Option", tint = Color.Red)
                    }
                }
                Divider()
            }

            item { Spacer(Modifier.height(50.dp)) }
        }
    }
}

// Utility to get filename from Uri
fun getFileName(context: Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}