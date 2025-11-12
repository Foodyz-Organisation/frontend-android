package com.example.foodyz_dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.foodyz_dam.ui.navigation.AppNavigation
import com.example.foodyz_dam.ui.theme.FoodyzDamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            FoodyzDamTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
