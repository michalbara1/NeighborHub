package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch
import android.content.Context


class AddPostViewModel(context: Context) : ViewModel() {

    private val repository = PostRepository(context)



    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun addPost(post: Post) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.addPost(post)
                _success.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
