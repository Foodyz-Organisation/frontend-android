package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme



@OptIn(UnstableApi::class)
@Composable
fun ReelsScreen(
    navController: NavController,
    reelsViewModel: ReelsViewModel = viewModel(),
    postsViewModel: com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel = viewModel()
) {
    val context = LocalContext.current
    val reelsList by reelsViewModel.reels.collectAsState()
    val infiniteReelsList by reelsViewModel.infiniteReelsList.collectAsState()
    val currentReelIndex by reelsViewModel.currentReelIndex.collectAsState()

    // --- NEW: Comments Sheet State ---
    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }
    val activeComments by postsViewModel.activeComments.collectAsState()
    val isCommentsLoading by postsViewModel.areCommentsLoading.collectAsState()

    // --- NEW: Share Dialog State ---
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedPostIdForSharing by remember { mutableStateOf<String?>(null) }

    // Handle back button press for navigation
    BackHandler {
        navController.popBackStack()
    }

    // --- Compose UI container for Reels ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Reels typically have a black background
    ) {
        // --- AndroidView to host ViewPager2 ---
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ViewPager2(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = ViewPager2.ORIENTATION_VERTICAL // Set to vertical scrolling

                    // --- MODIFIED: Initialize adapter correctly ---
                    // The ReelsPagerAdapter needs a context and the onReelClick listener
                    adapter = ReelsPagerAdapter(
                        context = ctx,
                        onReelClick = { clickedReelId ->
                            // Handle reel clicks (e.g., pause/play).
                            // This logic will be in the ViewModel to manage playback state.
                            reelsViewModel.togglePlayback(clickedReelId)
                        },
                        // --- NEW: Handle Comment Click ---
                        onCommentClick = { postId ->
                             selectedPostIdForComments = postId
                             postsViewModel.loadComments(postId)
                             showCommentsSheet = true
                        },
                        // --- NEW: Handle Share Click ---
                        onShareClick = { postId ->
                            selectedPostIdForSharing = postId
                            showShareDialog = true
                        },
                        // --- NEW: Handle Order Click ---
                        onOrderClick = { professionalId, foodType ->
                            // Navigate to menu with optional highlightCategory
                            // Set highlightCategory in savedStateHandle before navigation
                            navController.currentBackStackEntry?.savedStateHandle?.set("highlightCategory", foodType)
                            navController.navigate("menu_order_route/$professionalId")
                        },
                        navController = navController,
                        postsViewModel = postsViewModel,
                        reelsViewModel = reelsViewModel
                    )
                    // --- END MODIFIED ---

                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            reelsViewModel.onReelSelected(position) // Notify ViewModel of new current reel
                            // Also tell the adapter the new playing position for playback control
                            (adapter as? ReelsPagerAdapter)?.setCurrentlyPlayingPosition(position)
                        }

                        // --- MODIFIED: Handle scroll state changes for better playback control ---
                        override fun onPageScrollStateChanged(state: Int) {
                            super.onPageScrollStateChanged(state)
                            val reelsPagerAdapter = adapter as? ReelsPagerAdapter
                            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                                // When scrolling stops, ensure the current item's player is active
                                reelsPagerAdapter?.setCurrentlyPlayingPosition(currentItem)
                            } else {
                                // When scrolling, pause the current item's player
                                // Pass NO_POSITION to indicate no reel is actively playing
                                reelsPagerAdapter?.setCurrentlyPlayingPosition(RecyclerView.NO_POSITION)
                            }
                        }
                        // --- END MODIFIED ---
                    })
                }
            },
            update = { viewPager ->
                // --- MODIFIED: Use submitList for ListAdapter with infinite list ---
                (viewPager.adapter as? ReelsPagerAdapter)?.submitList(infiniteReelsList)

                // Ensure ViewPager2 scrolls to the current index (or initial position on first load)
                if (infiniteReelsList.isNotEmpty() && viewPager.currentItem != currentReelIndex) {
                    viewPager.setCurrentItem(currentReelIndex, false) // false for no smooth scroll
                }
                // --- MODIFIED: Set the playing position initially and on update ---
                // This ensures the correct reel plays if the list changes or viewPager state is restored
                (viewPager.adapter as? ReelsPagerAdapter)?.setCurrentlyPlayingPosition(currentReelIndex)
            }
        )

        // --- NEW: Comments Sheet ---
        if (showCommentsSheet && selectedPostIdForComments != null) {
            // Find the post from the actual reels list (not infinite list)
            val selectedPost = reelsList.find { it._id == selectedPostIdForComments }
            com.example.damprojectfinal.user.feature_posts.ui.post_management.CommentsSheet(
                post = selectedPost,
                comments = activeComments,
                isLoading = isCommentsLoading,
                onAddComment = { text ->
                    selectedPostIdForComments?.let { postId ->
                        postsViewModel.createComment(postId, text)
                    }
                },
                onDismiss = {
                    showCommentsSheet = false
                    selectedPostIdForComments = null
                    postsViewModel.clearActiveComments()
                },
                postsViewModel = postsViewModel,
                postId = selectedPostIdForComments ?: ""
            )
        }
        
        // --- NEW: Share Dialog ---
        if (showShareDialog && selectedPostIdForSharing != null) {
            com.example.damprojectfinal.user.common._component.SharePostDialog(
                postId = selectedPostIdForSharing!!,
                onDismiss = {
                    showShareDialog = false
                    selectedPostIdForSharing = null
                },
                onShareSuccess = {
                    showShareDialog = false
                    selectedPostIdForSharing = null
                }
            )
        }
    }
}

// ------------------------------------------------------
// 👁️ Preview Composable
// ------------------------------------------------------

@Preview(showBackground = true, name = "Reels Screen Preview")
@Composable
fun ReelsScreenPreview() {
    DamProjectFinalTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            // Provide a dummy NavController for the preview
            ReelsScreen(navController = rememberNavController())
        }
    }
}
