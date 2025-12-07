package com.example.damprojectfinal.user.feature_posts.ui.reel_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damprojectfinal.core.api.posts.ReelsApiService
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.dto.posts.PostResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import android.util.Base64 // <-- NEW: Import for Base64 encoding

class ReelsViewModel(
    private val reelsApiService: ReelsApiService = RetrofitClient.reelsApiService
) : ViewModel() {

    // --- State for the list of reels ---
    private val _reels = MutableStateFlow<List<PostResponse>>(emptyList())
    val reels: StateFlow<List<PostResponse>> = _reels.asStateFlow()

    // --- State for the currently visible reel's index ---
    private val _currentReelIndex = MutableStateFlow(0)
    val currentReelIndex: StateFlow<Int> = _currentReelIndex.asStateFlow()

    // --- State for loading indicator ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- State for error messages ---
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- Pagination and Looping State (now purely cursor-based) ---
    private var nextCursor: String? = null // This will store the cursor for the next page fetch
    private var hasMoreToLoad = true      // Flag to control loading more data

    // --- Playback State ---
    private val _isPlaying = MutableStateFlow(true) // True for autoplay by default
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        fetchReels()
    }

    // --- Data Fetching Logic ---
    fun fetchReels() {
        // Prevent multiple simultaneous fetches or fetching if no more data is expected
        if (_isLoading.value || !hasMoreToLoad) {
            return
        }

        _isLoading.value = true
        _errorMessage.value = null // Clear any previous error

        viewModelScope.launch {
            try {
                // Call the corrected API method, passing the nextCursor
                val fetchedList = reelsApiService.getReels(
                    limit = 10, // Fetch 10 reels at a time
                    cursor = nextCursor // Pass the cursor for the next page
                )

                if (fetchedList.isNotEmpty()) {
                    val newUniqueReels = mutableListOf<PostResponse>()
                    fetchedList.forEach { newReel ->
                        // Add only if the reel is not already in our current list (to handle backend looping)
                        if (_reels.value.none { it._id == newReel._id }) {
                            newUniqueReels.add(newReel)
                        }
                    }

                    _reels.value = _reels.value + newUniqueReels

                    // Generate the next cursor from the last newly added unique reel
                    // The backend expects "LAST_CREATED_AT_ISO_STRING_LAST_ID_STRING" Base64 encoded
                    val lastReel = _reels.value.lastOrNull() // Get the very last reel in the combined list
                    nextCursor = if (lastReel != null) {
                        val cursorString = "${lastReel.createdAt}_${lastReel._id}"
                        Base64.encodeToString(cursorString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    } else {
                        null // If no reels, no cursor
                    }

                    // Determine if there's more to load. If we got less than 'limit' new unique reels,
                    // it implies we might have exhausted the unique content.
                    // If your backend is truly designed for infinite *looping* with repetitions,
                    // 'hasMoreToLoad' would typically always remain true, and the UI relies on
                    // the filtering above to prevent duplicate display. Adjust this logic
                    // based on your backend's exact looping behavior. For a standard paginated feed,
                    // this logic is generally appropriate.
                    hasMoreToLoad = newUniqueReels.size == 10 // If we got a full batch, there might be more
                    // If less, we assume end of unique content for now.

                } else {
                    // No reels fetched. If it's the first load, show error. If not, means no more unique content.
                    if (_reels.value.isEmpty()) {
                        _errorMessage.value = "No reels available."
                    }
                    hasMoreToLoad = false // No more unique items to load
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
                e.printStackTrace()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reels: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- UI Interaction Logic ---
    fun onReelSelected(position: Int) {
        if (position != _currentReelIndex.value) {
            _currentReelIndex.value = position
        }

        // Trigger loading more reels if near the end of the current list
        // Load when 3 reels from end
        if (position >= _reels.value.size - 3 && hasMoreToLoad) {
            fetchReels()
        }

        // Also increment view count for the newly selected reel
        incrementViewCountForCurrentReel(position)
    }

    fun togglePlayback(reelId: String) {
        _isPlaying.value = !_isPlaying.value
        // This function's internal logic depends on how you manage ExoPlayer instances.
        // If ReelItem's internal logic handles this based on isCurrentItem, this might be a no-op here.
    }

    private fun incrementViewCountForCurrentReel(position: Int) {
        val reel = _reels.value.getOrNull(position)
        reel?.let {
            viewModelScope.launch {
                try {
                    // Call the corrected API method for incrementing view
                    reelsApiService.incrementReelView(it._id)
                    // Optionally, update the local PostResponse object's viewsCount for immediate UI refresh
                    val updatedReels = _reels.value.toMutableList()
                    val index = updatedReels.indexOfFirst { r -> r._id == it._id }
                    if (index != -1) {
                        updatedReels[index] = updatedReels[index].copy(viewsCount = updatedReels[index].viewsCount + 1)
                        _reels.value = updatedReels
                    }
                    println("View count incremented for reel: ${it._id}")
                } catch (e: Exception) {
                    println("Failed to increment view count for reel ${it._id}: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    // Function to update a reel after like/save operations
    fun updateReel(updatedPost: PostResponse) {
        val updatedReels = _reels.value.toMutableList()
        val index = updatedReels.indexOfFirst { it._id == updatedPost._id }
        if (index != -1) {
            updatedReels[index] = updatedPost
            _reels.value = updatedReels
        }
    }
}
