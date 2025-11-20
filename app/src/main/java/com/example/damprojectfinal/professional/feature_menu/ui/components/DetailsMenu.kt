package com.example.damprojectfinal.professional.feature_menu.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// You may need more imports depending on how you implement the list views,
// like LazyColumn, but these cover the structure provided.

// --- Main Screen Composable ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    menuItemName: String,
    // Inject your ViewModel here (e.g., viewModel: ItemDetailsViewModel = viewModel())
    navController: NavController
) {
    // 1. Manage the selected tab state
    val tabs = listOf("Ingredients", "Options")
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) } // 0 for Ingredients, 1 for Options

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(menuItemName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = { /* Handle Save Action (e.g., viewModel.saveChanges()) */ }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) { // Added .fillMaxSize() here

            // 2. Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // 3. Content based on selected tab
            when (selectedTabIndex) {
                0 -> IngredientsTabContent()
                1 -> OptionsTabContent()
            }
        }
    }
}


// --- Tab Content Composables ---

@Composable
fun IngredientsTabContent() {
    Column(
        // Use weight modifier to ensure this content fills the remaining space below the tabs
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Add horizontal padding for content
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top button to add ingredients (always visible/sticky)
        Button(
            onClick = { /* Navigate to Add Ingredient */ },
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("+ Add Ingredient")
        }

        // --- Ingredient List/Empty State Area ---
        // This is where you would display your LazyColumn of ingredients,
        // or the empty state from your screenshot, using a Modifier.fillMaxHeight()
        // inside a Box or Column to center the empty state.

        Text(
            text = "Display Ingredient List or Empty State UI...",
            modifier = Modifier.weight(1f) // Ensures text pushes button up slightly if needed
        )
    }
}


@Composable
fun OptionsTabContent() {
    Column(
        // Use weight modifier to ensure this content fills the remaining space below the tabs
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Add horizontal padding for content
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top button to add options (always visible/sticky)
        Button(
            onClick = { /* Navigate to Add Option Group */ },
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("+ Add New Option Group")
        }

        // --- Option Group List/Empty State Area ---
        // This is where you would display your LazyColumn of option groups

        Text(
            text = "Display Option Group List or Empty State UI...",
            modifier = Modifier.weight(1f) // Ensures text pushes button up slightly if needed
        )
    }
}