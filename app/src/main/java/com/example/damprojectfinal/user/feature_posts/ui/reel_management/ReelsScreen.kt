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

    var showCommentsSheet by remember { mutableStateOf(false) }
    var selectedPostIdForComments by remember { mutableStateOf<String?>(null) }
    val activeComments by postsViewModel.activeComments.collectAsState()
    val isCommentsLoading by postsViewModel.areCommentsLoading.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var selectedPostIdForSharing by remember { mutableStateOf<String?>(null) }

    BackHandler {
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ViewPager2(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = ViewPager2.ORIENTATION_VERTICAL

                    adapter = ReelsPagerAdapter(
                        context = ctx,
                        onReelClick = { clickedReelId ->
                            reelsViewModel.togglePlayback(clickedReelId)
                        },
                        onCommentClick = { postId ->
                             selectedPostIdForComments = postId
                             postsViewModel.loadComments(postId)
                             showCommentsSheet = true
                        },
                        onShareClick = { postId ->
                            selectedPostIdForSharing = postId
                            showShareDialog = true
                        },
                        navController = navController,
                        postsViewModel = postsViewModel,
                        reelsViewModel = reelsViewModel
                    )

                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            reelsViewModel.onReelSelected(position) // Notify ViewModel of new current reel
                            (adapter as? ReelsPagerAdapter)?.setCurrentlyPlayingPosition(position)
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            super.onPageScrollStateChanged(state)
                            val reelsPagerAdapter = adapter as? ReelsPagerAdapter
                            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                                reelsPagerAdapter?.setCurrentlyPlayingPosition(currentItem)
                            } else {
                                reelsPagerAdapter?.setCurrentlyPlayingPosition(RecyclerView.NO_POSITION)
                            }
                        }
                    })
                }
            },
            update = { viewPager ->
                (viewPager.adapter as? ReelsPagerAdapter)?.submitList(infiniteReelsList)

                if (infiniteReelsList.isNotEmpty() && viewPager.currentItem != currentReelIndex) {
                    viewPager.setCurrentItem(currentReelIndex, false) // false for no smooth scroll
                }
                (viewPager.adapter as? ReelsPagerAdapter)?.setCurrentlyPlayingPosition(currentReelIndex)
            }
        )

        if (showCommentsSheet && selectedPostIdForComments != null) {
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
                }
            )
        }

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
                    
                    navController.navigate(com.example.damprojectfinal.UserRoutes.HOME_SCREEN) {
                        popUpTo(com.example.damprojectfinal.UserRoutes.HOME_SCREEN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

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
