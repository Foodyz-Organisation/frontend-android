package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import android.content.Context
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.damprojectfinal.core.dto.posts.PostResponse
import com.example.damprojectfinal.ui.theme.DamProjectFinalTheme

// The adapter for ViewPager2, which uses a ListAdapter for efficient updates
// Modified to support infinite loop scrolling
class ReelsPagerAdapter(
    private val context: Context, // Context might not be strictly needed if only creating ComposeView
    private val onReelClick: (String) -> Unit, // Callback when a reel is clicked
    private val navController: androidx.navigation.NavController,
    private val postsViewModel: com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel,
    private val reelsViewModel: ReelsViewModel
) : ListAdapter<PostResponse, ReelsPagerAdapter.ReelViewHolder>(ReelDiffCallback()) {

    // Keep track of the currently playing reel's position to control playback
    private var currentlyPlayingPosition: Int = RecyclerView.NO_POSITION
    
    // Multiplier for infinite looping - creates a large number of items
    private val loopMultiplier = 1000
    
    // The actual list of reels (not looped)
    private var actualReelsList: List<PostResponse> = emptyList()
    
    // Get the actual reel count
    private fun getActualItemCount(): Int = actualReelsList.size
    
    // Get the total item count (looped)
    override fun getItemCount(): Int {
        val actualCount = getActualItemCount()
        return if (actualCount > 0) actualCount * loopMultiplier else 0
    }
    
    // Map looped position to actual position
    private fun getActualPosition(position: Int): Int {
        val actualCount = getActualItemCount()
        return if (actualCount > 0) position % actualCount else 0
    }
    
    // Get the starting position in the middle of the looped list
    fun getInitialPosition(): Int {
        val actualCount = getActualItemCount()
        return if (actualCount > 0) (actualCount * loopMultiplier) / 2 else 0
    }
    
    // Check if we need to jump to maintain infinite loop
    fun shouldJumpToLoop(currentPosition: Int): Int? {
        val actualCount = getActualItemCount()
        if (actualCount == 0) return null
        
        val totalItems = actualCount * loopMultiplier
        val jumpThreshold = actualCount * 2 // Jump when within 2x actual count from edges
        
        // If near the beginning, jump to near the end
        if (currentPosition < jumpThreshold) {
            return totalItems - jumpThreshold
        }
        // If near the end, jump to near the beginning
        if (currentPosition > totalItems - jumpThreshold) {
            return jumpThreshold
        }
        return null
    }

    // Update the position of the currently playing reel
    fun setCurrentlyPlayingPosition(position: Int) {
        if (currentlyPlayingPosition != position) {
            val oldPosition = currentlyPlayingPosition
            currentlyPlayingPosition = position
            // Notify changes to trigger rebind for old and new positions
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition)
            }
            if (currentlyPlayingPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(currentlyPlayingPosition)
            }
        }
    }
    
    // Override submitList to store the actual list
    fun submitActualList(list: List<PostResponse>) {
        actualReelsList = list
        // Submit the looped list to the adapter
        val loopedList = if (list.isNotEmpty()) {
            List(loopMultiplier) { list }.flatten()
        } else {
            emptyList()
        }
        submitList(loopedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        // Create a ComposeView to host our ReelItem composable
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return ReelViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        // Map the looped position to actual position
        val actualPosition = getActualPosition(position)
        val reelPost = actualReelsList.getOrNull(actualPosition) ?: return
        
        // Pass whether this item is the currently focused item to the Composable
        val isCurrentItem = position == currentlyPlayingPosition

        holder.bind(reelPost, isCurrentItem, onReelClick, navController, postsViewModel, reelsViewModel)
    }
    
    // Get actual position from looped position (for ViewModel)
    fun getActualPositionFromLooped(loopedPosition: Int): Int {
        return getActualPosition(loopedPosition)
    }

    // ViewHolder that holds a ComposeView and binds the ReelItem composable
    inner class ReelViewHolder(private val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        fun bind(
            reelPost: PostResponse,
            isCurrentItem: Boolean,
            onReelClick: (String) -> Unit,
            navController: androidx.navigation.NavController,
            postsViewModel: com.example.damprojectfinal.user.feature_posts.ui.post_management.PostsViewModel,
            reelsViewModel: ReelsViewModel
        ) {
            composeView.setContent {
                // Ensure theme is applied for composables in this view holder
                DamProjectFinalTheme {
                    ReelItem(
                        reelPost = reelPost,
                        isCurrentItem = isCurrentItem,
                        onReelClick = onReelClick,
                        navController = navController,
                        postsViewModel = postsViewModel,
                        reelsViewModel = reelsViewModel
                    )
                }
            }
        }

        // --- REMOVED: No onViewRecycled() override here ---
        // Player lifecycle is managed by DisposableEffect in ReelItem.
    }
}

// DiffUtil for efficient list updates (improves performance by only updating changed items)
class ReelDiffCallback : DiffUtil.ItemCallback<PostResponse>() {
    override fun areItemsTheSame(oldItem: PostResponse, newItem: PostResponse): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: PostResponse, newItem: PostResponse): Boolean {
        return oldItem == newItem // Data classes compare all properties by default
    }
}
