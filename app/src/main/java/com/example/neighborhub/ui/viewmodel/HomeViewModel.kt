/*package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchPosts() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val postsList = postRepository.getPosts()  // Fetch posts from Firestore
                _posts.value = postsList
                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
                _error.value = "Error fetching posts: ${e.message}"
            }
        }
    }
}*/
