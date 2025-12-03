package com.example.damprojectfinal.ui.theme.screens.chat.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Chats", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = { /* TODO: open drawer */ }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}
