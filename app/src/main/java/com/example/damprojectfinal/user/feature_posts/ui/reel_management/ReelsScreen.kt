package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.recyclerview.widget.RecyclerView // <--- ADDED for RecyclerView.NO_POSITION
import androidx.viewpager2.widget.ViewPager2
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.ReelsViewModel
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@OptIn(UnstableApi::class)
@Composable
fun ReelsScreen(
    navController: NavController,
    reelsViewModel: ReelsViewModel = viewModel(),
    postsViewModel: com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel = viewModel()
) {
    val context = LocalContext.current
    val reelsList by reelsViewModel.reels.collectAsState()
    val currentReelIndex by reelsViewModel.currentReelIndex.collectAsState()

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
                        navController = navController,
                        postsViewModel = postsViewModel,
                        reelsViewModel = reelsViewModel
                    )
                    // --- END MODIFIED ---

                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            
                            val reelsPagerAdapter = adapter as? ReelsPagerAdapter
                            
                            // Map looped position to actual position for ViewModel
                            val actualPosition = reelsPagerAdapter?.getActualPositionFromLooped(position) ?: position
                            reelsViewModel.onReelSelected(actualPosition)
                            
                            // Also tell the adapter the new playing position for playback control
                            reelsPagerAdapter?.setCurrentlyPlayingPosition(position)
                        }

                        // --- MODIFIED: Handle scroll state changes for better playback control and infinite loop ---
                        override fun onPageScrollStateChanged(state: Int) {
                            super.onPageScrollStateChanged(state)
                            val reelsPagerAdapter = adapter as? ReelsPagerAdapter
                            
                            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                                // When scrolling stops, check if we need to jump for infinite loop
                                val jumpPosition = reelsPagerAdapter?.shouldJumpToLoop(currentItem)
                                if (jumpPosition != null) {
                                    // Jump to maintain infinite loop (without animation)
                                    setCurrentItem(jumpPosition, false)
                                    reelsPagerAdapter?.setCurrentlyPlayingPosition(jumpPosition)
                                    // Update ViewModel with actual position
                                    val actualPos = reelsPagerAdapter.getActualPositionFromLooped(jumpPosition)
                                    reelsViewModel.onReelSelected(actualPos)
                                } else {
                                    // Normal case: ensure the current item's player is active
                                    reelsPagerAdapter?.setCurrentlyPlayingPosition(currentItem)
                                }
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
                val adapter = viewPager.adapter as? ReelsPagerAdapter
                
                // --- MODIFIED: Use submitActualList for infinite loop support ---
                val previousItemCount = adapter?.itemCount ?: 0
                adapter?.submitActualList(reelsList)
                
                if (reelsList.isNotEmpty()) {
                    // Initialize to middle position for infinite loop (only on first load or when list was empty)
                    if (previousItemCount == 0) {
                        val initialPosition = adapter?.getInitialPosition() ?: 0
                        viewPager.setCurrentItem(initialPosition, false)
                        adapter?.setCurrentlyPlayingPosition(initialPosition)
                        // Update ViewModel with actual position (0 for first item)
                        reelsViewModel.onReelSelected(0)
                    } else {
                        // For subsequent updates, maintain current position by mapping to looped position
                        // Get current actual position and map it to the new looped list
                        val currentActualPos = currentReelIndex.coerceIn(0, reelsList.size - 1)
                        val initialPos = adapter?.getInitialPosition() ?: 0
                        val loopedPosition = initialPos + currentActualPos
                        
                        // Only update if position changed significantly
                        if (kotlin.math.abs(viewPager.currentItem - loopedPosition) > reelsList.size) {
                            viewPager.setCurrentItem(loopedPosition, false)
                        }
                        adapter?.setCurrentlyPlayingPosition(viewPager.currentItem)
                    }
                }
            }
        )

        // --- Overlay: Other UI elements (e.g., username, caption, interaction buttons)
        // These are integrated within ReelItem composable now.
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
