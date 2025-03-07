package com.example.neighborhub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.model.Post
import kotlinx.coroutines.launch

class PostViewModel(context: Context) : ViewModel() {
    private val repository = PostRepository(context)

    private val _allPosts = mutableListOf<Post>()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllPosts()
                if (result.isNotEmpty()) {
                    _allPosts.clear()
                    _allPosts.addAll(result)
                    _posts.value = _allPosts
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "No posts found"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}