package com.example.damprojectfinal.user.common._component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

// Define the content actions
sealed class AddAction(val icon: ImageVector, val label: String, val color: Color) {
    data object AddPost : AddAction(Icons.Default.Edit, "Post", Color(0xFF2563EB))
    data object AddPhoto : AddAction(Icons.Default.PhotoCamera, "Photo", Color(0xFF10B981))
    data object AddVideo : AddAction(Icons.Default.Videocam, "Video", Color(0xFFF59E0B))
}

@Composable
fun AddButton(
    onActionSelected: (AddAction) -> Unit
) {
    // State to manage whether the action buttons are expanded or collapsed
    var isExpanded by remember { mutableStateOf(false) }

    // List of actions to show when expanded
    val actions = listOf(
        AddAction.AddVideo,
        AddAction.AddPhoto,
        AddAction.AddPost
    )

    // The entire multi-action button assembly is placed in a Column
    // and aligned to the bottom end, typical for a FAB.
    Column(
        modifier = Modifier
            .wrapContentSize()
            .zIndex(1f), // Ensure FAB is always on top
        horizontalAlignment = Alignment.End
    ) {
        // Expanded Options (Video, Photo, Post)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                actions.forEach { action ->
                    ActionItem(
                        action = action,
                        onClick = {
                            isExpanded = false // Collapse after selection
                            onActionSelected(action) // Execute the action
                        }
                    )
                }
            }
        }

        // Spacer between options and the main FAB
        Spacer(modifier = Modifier.height(if (isExpanded) 16.dp else 0.dp))

        // Main FAB (Always Visible)
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded }, // Toggle state on click
            containerColor = Color(0xFF2563EB), // Blue color for the main button
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            // Rotate the Icon from Plus to Close when expanded
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close options" else "Open Add Options",
                tint = Color.White,
                modifier = Modifier.rotate(if (isExpanded) 45f else 0f) // Simple rotation for feedback
            )
        }
    }
}

@Composable
fun ActionItem(action: AddAction, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(Color.White)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .widthIn(min = 100.dp)
    ) {
        // Label on the left
        Text(
            text = action.label,
            fontSize = 14.sp,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Small secondary FAB on the right
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(action.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Preview to see how the component looks
@Preview(showBackground = true)
@Composable
fun AddButtonPreview() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        AddButton(onActionSelected = { action ->
            println("Action selected: ${action.label}")
        })
    }
}