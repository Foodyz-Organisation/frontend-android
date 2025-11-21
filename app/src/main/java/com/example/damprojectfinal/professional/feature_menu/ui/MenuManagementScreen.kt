package com.example.damprojectfinal.professional.feature_menu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage // 👈 Import Coil
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuListUiState
import com.example.damprojectfinal.core.dto.menu.MenuItemResponseDto
// import com.example.damprojectfinal.core.dto.menu.UpdateMenuItemDto // No longer needed here

// 🚨 IMPORTANT: Replace with your actual server address (e.g., 192.168.1.10)
private const val BASE_URL = "http://10.0.2.2:3000/"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemManagementScreen(
    navController: NavController,
    professionalId: String,
    viewModel: MenuViewModel
) {
    // 1. OBSERVE STATE
    val menuListState by viewModel.menuListUiState.collectAsState()
    val itemActionState by viewModel.uiState.collectAsState()

    // 2. DATA FETCH TRIGGER
    val dummyAuthToken = "YOUR_PROFESSIONAL_AUTH_TOKEN"

    LaunchedEffect(professionalId) {
        viewModel.fetchGroupedMenu(professionalId, dummyAuthToken)
    }

    // 3. UI STRUCTURE
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Menu Items") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to Create Screen
                    navController.navigate("create_menu_item/$professionalId")
                }
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
            // 4. HANDLE UI STATE
            when (menuListState) {
                MenuListUiState.Idle -> { /* Idle state, waiting for load */ }
                MenuListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MenuListUiState.Error -> {
                    Text(
                        text = "Error: ${(menuListState as MenuListUiState.Error).message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is MenuListUiState.Success -> {
                    val groupedMenu = (menuListState as MenuListUiState.Success).groupedMenu

                    if (groupedMenu.isEmpty()) {
                        Text(
                            "No items yet. Click the + button to create one.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        MenuSectionList(
                            navController = navController, // Pass NavController
                            professionalId = professionalId, // Pass Professional ID
                            groupedMenu = groupedMenu,
                            onDelete = { item ->
                                viewModel.deleteMenuItem(item.id, professionalId, dummyAuthToken)
                            }
                        )
                    }
                }
            }
            SnackbarHostForActions(itemActionState)
        }
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
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Iterate through each category (key in the map)
        groupedMenu.forEach { (category, items) ->
            item {
                // Category Title (e.g., BURGER)
                Text(
                    text = category,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Iterate through items within that category
            items(items) { item ->
                MenuItemCard(
                    item = item,
                    onEditClick = {
                        // Navigate to ItemDetailsScreen using the item ID and professional ID
                        navController.navigate("edit_menu_item/${item.id}/$professionalId")
                    },
                    onDeleteClick = { onDelete(item) } // Pass the item to the handler
                )
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Renders an individual Menu Item
// --------------------------------------------------
@Composable
fun MenuItemCard(
    item: MenuItemResponseDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ⭐️ IMAGE THUMBNAIL ⭐️
            AsyncImage(
                model = BASE_URL + item.image,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text Content and Price
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description ?: "No description provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Buttons Row (kept separate for bottom alignment)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Button
            TextButton(onClick = onEditClick) {
                Text("EDIT")
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Delete Button
            TextButton(onClick = onDeleteClick, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("DELETE")
            }
        }
    }
}

// --------------------------------------------------
// COMPONENT: Snackbar Host (Optional but recommended)
// --------------------------------------------------
@Composable
fun SnackbarHostForActions(state: com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState) {
    // This is where you would observe the itemActionState for Success/Error and show a Snackbar
    when (state) {
        is com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState.Success -> {
            // Show snackbar: "Item ${state.menuItem.name} action successful!"
        }
        is com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuItemUiState.Error -> {
            // Show snackbar: "Action failed: ${state.message}"
        }
        else -> Unit
    }
}