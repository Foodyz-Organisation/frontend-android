package com.example.damprojectfinal.user.feature_posts.ui.post_management


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme // Import your app's theme
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel // Import your PostsViewModel
import kotlinx.coroutines.launch // Import for rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    navController: NavController,
    postId: String,
    initialCaption: String, // Caption to pre-fill the text field
    postsViewModel: PostsViewModel = viewModel()
) {
    var caption by remember { mutableStateOf(initialCaption) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Use rememberCoroutineScope for launching coroutines
    val errorMessage by postsViewModel.errorMessage.collectAsState() // Observe error messages

    // Effect to show snackbar for errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            postsViewModel.clearError() // Clear error after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (caption.isNotBlank()) {
                                postsViewModel.updatePostCaption(postId, caption)
                                // Navigate back immediately after attempting update.
                                // The UI update (post list refresh) will happen in PostsScreen
                                // as PostsViewModel's _posts StateFlow updates.
                                navController.popBackStack()
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Caption cannot be empty",
                                        actionLabel = "Dismiss",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        enabled = caption.isNotBlank() // Enable save only if caption is not blank
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Caption") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 10
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPostScreenPreview() {
    DamProjectFinalTheme {
        // For preview, we need a dummy NavController
        val navController = rememberNavController()
        EditPostScreen(
            navController = navController,
            postId = "preview_post_id_123",
            initialCaption = "This is a preview caption for an editable post. It can be quite long to test the text field.",
            // postsViewModel = viewModel() // ViewModel is auto-provided by viewModel() call in Composable
        )
    }
}
