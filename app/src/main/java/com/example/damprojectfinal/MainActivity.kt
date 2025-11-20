package com.example.damprojectfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Keep your edge-to-edge setup
        setContent {
            DamProjectFinalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pass the scaffold inner padding as a Modifier to AppNavigation
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
