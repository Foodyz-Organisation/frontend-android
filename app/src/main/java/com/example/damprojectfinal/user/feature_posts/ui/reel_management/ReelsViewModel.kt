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

    private val _reels = MutableStateFlow<List<PostResponse>>(emptyList())
    val reels: StateFlow<List<PostResponse>> = _reels.asStateFlow()

    private val _infiniteReelsList = MutableStateFlow<List<PostResponse>>(emptyList())
    val infiniteReelsList: StateFlow<List<PostResponse>> = _infiniteReelsList.asStateFlow()

    private val _currentReelIndex = MutableStateFlow(0)
    val currentReelIndex: StateFlow<Int> = _currentReelIndex.asStateFlow()

    companion object {
        private const val LOOP_MULTIPLIER = 1000
        private const val INITIAL_POSITION = 500
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var nextCursor: String? = null
    private var hasMoreToLoad = true

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        fetchReels()
    }

    private fun createInfiniteList(originalList: List<PostResponse>): List<PostResponse> {
        if (originalList.isEmpty()) return emptyList()
        
        val result = mutableListOf<PostResponse>()
        repeat(LOOP_MULTIPLIER) {
            result.addAll(originalList)
        }
        return result
    }

    fun getActualReelIndex(infinitePosition: Int): Int {
        if (_reels.value.isEmpty()) return 0
        return infinitePosition % _reels.value.size
    }

    fun getInitialPosition(): Int {
        if (_reels.value.isEmpty()) return 0
        // Start at the middle of the infinite list
        return (INITIAL_POSITION * _reels.value.size)
    }

    fun fetchReels() {
        if (_isLoading.value || !hasMoreToLoad) {
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val fetchedList = reelsApiService.getReels(
                    limit = 10,
                    cursor = nextCursor
                )

                if (fetchedList.isNotEmpty()) {
                    val newUniqueReels = mutableListOf<PostResponse>()
                    fetchedList.forEach { newReel ->
                        if (_reels.value.none { it._id == newReel._id }) {
                            newUniqueReels.add(newReel)
                        }
                    }

                    val wasEmpty = _reels.value.isEmpty()
                    _reels.value = _reels.value + newUniqueReels
                    
                    _infiniteReelsList.value = createInfiniteList(_reels.value)
                    
                    if (wasEmpty && _infiniteReelsList.value.isNotEmpty()) {
                        _currentReelIndex.value = getInitialPosition()
                    }

                    val lastReel = _reels.value.lastOrNull()
                    nextCursor = if (lastReel != null) {
                        val cursorString = "${lastReel.createdAt}_${lastReel._id}"
                        Base64.encodeToString(cursorString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    } else {
                        null
                    }

                    hasMoreToLoad = newUniqueReels.size == 10

                } else {
                    if (_reels.value.isEmpty()) {
                        _errorMessage.value = "No reels available."
                    }
                    hasMoreToLoad = false
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

    fun onReelSelected(position: Int) {
        if (position != _currentReelIndex.value) {
            _currentReelIndex.value = position
        }
        val actualIndex = getActualReelIndex(position)
        if (_reels.value.isNotEmpty() && hasMoreToLoad) {
            val uniqueReelsCount = _reels.value.size
            val cycleCount = position / uniqueReelsCount
            if (cycleCount >= 3) {
                fetchReels()
            }
        }
        incrementViewCountForActualReel(actualIndex)
    }

    fun togglePlayback(reelId: String) {
        _isPlaying.value = !_isPlaying.value
    }

    private fun incrementViewCountForActualReel(actualIndex: Int) {
        val reel = _reels.value.getOrNull(actualIndex)
        reel?.let {
            viewModelScope.launch {
                try {
                    reelsApiService.incrementReelView(it._id)
                    val updatedReels = _reels.value.toMutableList()
                    val index = updatedReels.indexOfFirst { r -> r._id == it._id }
                    if (index != -1) {
                        updatedReels[index] = updatedReels[index].copy(viewsCount = updatedReels[index].viewsCount + 1)
                        _reels.value = updatedReels
                        _infiniteReelsList.value = createInfiniteList(_reels.value)
                    }
                    println("View count incremented for reel: ${it._id}")
                } catch (e: Exception) {
                    println("Failed to increment view count for reel ${it._id}: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateReel(updatedPost: PostResponse) {
        val updatedReels = _reels.value.toMutableList()
        val index = updatedReels.indexOfFirst { it._id == updatedPost._id }
        if (index != -1) {
            updatedReels[index] = updatedPost
            _reels.value = updatedReels
            _infiniteReelsList.value = createInfiniteList(_reels.value)
        }
    }
}
