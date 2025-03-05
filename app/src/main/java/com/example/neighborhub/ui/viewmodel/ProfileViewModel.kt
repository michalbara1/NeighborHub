package com.example.neighborhub.ui.viewmodel
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserDetails() {
        val currentUser = authRepository.getCurrentUser()
        currentUser?.let { user ->
            _userName.value = user.displayName ?: "Unknown"
            fetchUserPosts(user.uid)
        } ?: run {
            _errorMessage.value = "User not logged in"
        }
    }

    private fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                val posts = postRepository.getPostsByUserId(userId).value ?: emptyList()
                _userPosts.value = posts
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}