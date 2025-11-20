package com.example.damprojectfinal.core.api

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.damprojectfinal.core.api.TokenManager

@Composable
fun DebugUserLogger(tokenManager: TokenManager) {
    // 1. Collect the stored data as state
    val userId by tokenManager.getUserId().collectAsState(initial = null)
    val userRole by tokenManager.getUserRole().collectAsState(initial = null)

    // 2. Log the data whenever it changes
    userId?.let { id ->
        Log.i("CURRENT_USER", "✅ User Logged In:")
        Log.i("CURRENT_USER", "   - User ID: $id")
    }
    userRole?.let { role ->
        Log.i("CURRENT_USER", "   - User Role: $role")
    }

    if (userId == null && userRole == null) {
        Log.d("CURRENT_USER", "User is currently logged out (no tokens found).")
    }
}