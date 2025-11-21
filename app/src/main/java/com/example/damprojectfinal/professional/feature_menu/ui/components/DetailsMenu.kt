package com.example.damprojectfinal.professional.feature_menu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.damprojectfinal.core.dto.menu.IngredientDto
import com.example.damprojectfinal.core.dto.menu.OptionDto
import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.ItemDetailsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    professionalId: String,
    viewModel: MenuViewModel,
    navController: NavController
) {
    // 1. Fetch Data on Launch
    val dummyToken = "YOUR_AUTH_TOKEN" // Replace with actual token
    LaunchedEffect(itemId) {
        viewModel.fetchMenuItemDetails(itemId, dummyToken)
    }

    // 2. Observe State
    // Using collectAsState() to avoid lifecycle crashes for now
    val uiState by viewModel.itemDetailsUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is ItemDetailsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ItemDetailsUiState.Error -> Text("Error: ${(uiState as ItemDetailsUiState.Error).message}", Modifier.align(Alignment.Center))
                is ItemDetailsUiState.Success -> {
                    val item = (uiState as ItemDetailsUiState.Success).item
                    // Show the Editable Form
                    EditItemContent(
                        initialName = item.name,
                        initialDesc = item.description ?: "",
                        initialPrice = item.price,
                        initialIngredients = item.ingredients,
                        initialOptions = item.options,
                        onSave = { name, desc, price, ingredients, options ->
                            val updateDto = UpdateMenuItemDto(
                                name = name,
                                description = desc,
                                price = price,
                                ingredients = ingredients,
                                options = options
                            )
                            viewModel.updateMenuItem(itemId, professionalId, updateDto, dummyToken)
                            navController.popBackStack()
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EditItemContent(
    initialName: String,
    initialDesc: String,
    initialPrice: Double,
    initialIngredients: List<IngredientDto>,
    initialOptions: List<OptionDto>,
    onSave: (String, String, Double, List<IngredientDto>, List<OptionDto>) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDesc) }
    var priceStr by remember { mutableStateOf(initialPrice.toString()) }
    val ingredientsList = remember { mutableStateListOf(*initialIngredients.toTypedArray()) }
    val optionsList = remember { mutableStateListOf(*initialOptions.toTypedArray()) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Ingredients", "Options")

    Column(Modifier.fillMaxSize()) {
        // Save Button Row
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                onSave(name, description, priceStr.toDoubleOrNull() ?: 0.0, ingredientsList.toList(), optionsList.toList())
            }) { Text("Confirm Changes") }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }

        when(selectedTab) {
            0 -> Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
            1 -> IngredientsListEditor(ingredientsList)
            2 -> OptionsListEditor(optionsList)
        }
    }
}

@Composable
fun IngredientsListEditor(list: androidx.compose.runtime.snapshots.SnapshotStateList<IngredientDto>) {
    var newName by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Ingredient") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { if(newName.isNotBlank()) { list.add(IngredientDto(newName, true)); newName = "" } }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
        LazyColumn {
            items(list) { item ->
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.name)
                    IconButton(onClick = { list.remove(item) }) { Icon(Icons.Default.Delete, "Remove", tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun OptionsListEditor(list: androidx.compose.runtime.snapshots.SnapshotStateList<OptionDto>) {
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = newPrice, onValueChange = { newPrice = it }, label = { Text("Price") }, modifier = Modifier.weight(0.5f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            IconButton(onClick = {
                val p = newPrice.toDoubleOrNull()
                if(newName.isNotBlank() && p != null) { list.add(OptionDto(newName, p)); newName = ""; newPrice = "" }
            }) { Icon(Icons.Default.Add, "Add") }
        }
        LazyColumn {
            items(list) { item ->
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.name} (+$${item.price})")
                    IconButton(onClick = { list.remove(item) }) { Icon(Icons.Default.Delete, "Remove", tint = Color.Red) }
                }
            }
        }
    }
}