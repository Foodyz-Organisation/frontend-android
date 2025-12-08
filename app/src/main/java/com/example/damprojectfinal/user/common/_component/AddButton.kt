package com.example.damprojectfinal.user.common._component

// --- Removed: import androidx.compose.animation.* related to AnimatedVisibility ---
// --- Removed: import androidx.compose.foundation.clickable ---
import androidx.compose.foundation.layout.Box // Still useful for Preview
import androidx.compose.foundation.layout.fillMaxSize // Still useful for Preview
import androidx.compose.foundation.layout.padding // Still useful for Preview
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
// --- Removed: import androidx.compose.foundation.shape.RoundedCornerShape ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// --- Removed: import androidx.compose.material.icons.filled.Edit ---
// --- Removed: import androidx.compose.material.icons.filled.PhotoCamera ---
// --- Removed: import androidx.compose.material.icons.filled.Videocam ---
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
// --- Removed: import androidx.compose.runtime.getValue ---
// --- Removed: import androidx.compose.runtime.mutableStateOf ---
// --- Removed: import androidx.compose.runtime.remember ---
// --- Removed: import androidx.compose.runtime.setValue ---
import androidx.compose.ui.Alignment // Still useful for Preview
import androidx.compose.ui.Modifier
// --- Removed: import androidx.compose.ui.draw.clip ---
// --- Removed: import androidx.compose.ui.draw.rotate ---
// --- Removed: import androidx.compose.ui.draw.shadow ---
import androidx.compose.ui.graphics.Color
// --- Removed: import androidx.compose.ui.graphics.vector.ImageVector ---
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// --- Removed: import androidx.compose.ui.unit.sp ---
import androidx.compose.ui.zIndex // Keep zIndex for overlaying content

// --- REMOVED: Define the content actions sealed class (AddAction is no longer needed) ---
/*
sealed class AddAction(val icon: ImageVector, val label: String, val color: Color) {
    data object AddPost : AddAction(Icons.Default.Edit, "Post", Color(0xFF2563EB))
    data object AddPhoto : AddAction(Icons.Default.PhotoCamera, "Photo", Color(0xFF10B981))
    data object AddVideo : AddAction(Icons.Default.Videocam, "Video", Color(0xFFF59E0B))
}
*/

@Composable
fun AddButton(
    onClick: () -> Unit // --- CHANGED: Now takes a direct onClick lambda ---
) {
    // --- REMOVED: isExpanded state ---
    // --- REMOVED: actions list ---
    // --- REMOVED: Column wrapping the entire FAB assembly ---
    // --- REMOVED: AnimatedVisibility for expanded options ---
    // --- REMOVED: Spacer ---

    // Main FAB (Always Visible, single button)
    FloatingActionButton(
        onClick = onClick, // --- CHANGED: Directly use the passed onClick lambda ---
        containerColor = Color(0xFFFFD700), // --- CHANGED: Yellow color ---
        shape = CircleShape,
        modifier = Modifier
            .size(56.dp)
            .zIndex(1f) // Ensure FAB is always on top
    ) {
        // --- SIMPLIFIED: Icon no longer rotates or changes contentDescription ---
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add new post", // Static content description
            tint = Color.White,
        )
    }
}

// --- REMOVED: ActionItem Composable (no longer needed) ---
/*
@Composable
fun ActionItem(action: AddAction, onClick: () -> Unit) {
    // ... (content of ActionItem) ...
}
*/

// Preview to see how the component looks
@Preview(showBackground = true)
@Composable
fun AddButtonPreview() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        AddButton(onClick = { // --- CHANGED: Updated for new signature ---
            println("Add button clicked!")
        })
    }
}
