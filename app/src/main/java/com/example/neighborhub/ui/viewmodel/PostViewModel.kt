package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.model.Post
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _allPosts = mutableListOf<Post>() // Store all posts here

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllPosts()
            if (result.isNotEmpty()) {
                _allPosts.clear()
                _allPosts.addAll(result) // Cache the original list
                _posts.value = _allPosts
                _errorMessage.value = null
            } else {
                _errorMessage.value = "No posts found"
            }
            _isLoading.value = false
        }
    }

    fun searchPosts(query: String) {
        if (query.isBlank()) {
            _posts.value = _allPosts // Reset to the original list
        } else {
            _posts.value = _allPosts.filter { post ->
                post.headline.contains(query, true) || post.userName.contains(query, true)
            }
        }
    }

}
