package com.example.damprojectfinal.professional.feature_menu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemManagementScreen(
    navController: NavController,
    professionalId: String,
    viewModel: MenuViewModel // <-- expect an instance, no default
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Menu Items") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to Create Item Screen
                    navController.navigate("create_menu_item/$professionalId")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Menu Item"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No items yet. Click the + button to create one.")
        }
    }
}
