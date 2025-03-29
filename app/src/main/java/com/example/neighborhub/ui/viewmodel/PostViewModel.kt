package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch
import kotlin.math.* // For distance calculation

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    public val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _filteredPosts = MutableLiveData<List<Post>>()
    val filteredPosts: LiveData<List<Post>> get() = _filteredPosts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var allPosts = listOf<Post>()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllPosts()
                allPosts = result
                _posts.value = result
                _filteredPosts.value = result
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPosts(query: String) {
        if (query.isEmpty()) {
            _filteredPosts.value = allPosts
            return
        }

        val lowercaseQuery = query.lowercase()
        val filtered = allPosts.filter {
            it.headline.lowercase().contains(lowercaseQuery) ||
                    it.content.lowercase().contains(lowercaseQuery) ||
                    it.userName.lowercase().contains(lowercaseQuery)
        }
        _filteredPosts.value = filtered
    }




    fun getPostsWithLocation() {
        viewModelScope.launch {
            try {
                val postsWithLocation = allPosts.filter {
                    it.latitude != null && it.longitude != null
                }
                _filteredPosts.value = postsWithLocation
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error filtering posts by location"
            }
        }
    }


    fun filterPostsByDistance(userLatitude: Double, userLongitude: Double, maxDistanceMeters: Double) {
        viewModelScope.launch {
            try {
                val postsWithinDistance = allPosts.filter { post ->
                    post.latitude != null && post.longitude != null &&
                            calculateDistance(
                                userLatitude, userLongitude,
                                post.latitude!!, post.longitude!!
                            ) <= maxDistanceMeters
                }
                _filteredPosts.value = postsWithinDistance
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error filtering posts by distance"
            }
        }
    }


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}