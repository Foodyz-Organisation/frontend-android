// app/src/main/java/com/example/damprojectfinal/user/feature_posts/ui/ReelsPagerAdapter.kt
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
class ReelsPagerAdapter(
    private val context: Context, // Context might not be strictly needed if only creating ComposeView
    private val onReelClick: (String) -> Unit // Callback when a reel is clicked
) : ListAdapter<PostResponse, ReelsPagerAdapter.ReelViewHolder>(ReelDiffCallback()) {

    // Keep track of the currently playing reel's position to control playback
    private var currentlyPlayingPosition: Int = RecyclerView.NO_POSITION

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
        val reelPost = getItem(position)
        // Pass whether this item is the currently focused item to the Composable
        val isCurrentItem = position == currentlyPlayingPosition

        holder.bind(reelPost, isCurrentItem, onReelClick)
    }

    // ViewHolder that holds a ComposeView and binds the ReelItem composable
    inner class ReelViewHolder(private val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        fun bind(reelPost: PostResponse, isCurrentItem: Boolean, onReelClick: (String) -> Unit) {
            composeView.setContent {
                // Ensure theme is applied for composables in this view holder
                DamProjectFinalTheme {
                    ReelItem(
                        reelPost = reelPost,
                        isCurrentItem = isCurrentItem,
                        onReelClick = onReelClick
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
