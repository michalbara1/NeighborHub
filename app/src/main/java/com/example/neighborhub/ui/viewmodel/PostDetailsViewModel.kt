package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.model.Post
import kotlinx.coroutines.launch

class PostDetailsViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _post = MutableLiveData<Post?>()
    val post: LiveData<Post?> = _post

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun getPostByHeadline(headline: String?) {
        if (headline.isNullOrEmpty()) {
            _errorMessage.value = "Invalid headline"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getPostByHeadline(headline)
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
