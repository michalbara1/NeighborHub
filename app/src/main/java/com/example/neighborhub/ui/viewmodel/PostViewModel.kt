package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
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
                _filteredPosts.value = result // Initialize with all posts
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
}