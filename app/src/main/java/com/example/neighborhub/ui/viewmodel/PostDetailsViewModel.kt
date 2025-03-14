package com.example.neighborhub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch

class PostDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(application)

    private val _post = MutableLiveData<Post?>()
    val post: LiveData<Post?> = _post

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun getPostById(postId: String?) {
        if (postId.isNullOrEmpty()) {
            _errorMessage.value = "Invalid post ID"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getPostById(postId)
            if (result != null) {
                _post.value = result
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Post not found"
            }
            _isLoading.value = false
        }
    }
}