package com.example.neighborhub.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.neighborhub.model.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.example.neighborhub.model.data.PostDao
import android.util.Log
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userPhotoUrl = MutableLiveData<String>()
    val userPhotoUrl: LiveData<String> = _userPhotoUrl

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val postDao: PostDao = AppDatabase.getInstance(application).postDao()


    // ProfileViewModel.kt
    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun fetchUserDetails() {
        val currentUser: FirebaseUser? = authRepository.getCurrentUser()
        currentUser?.let { user ->
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _userName.value = document.getString("username") ?: "Unknown"
                        _userEmail.value = document.getString("email") ?: "Unknown"
                        _userPhotoUrl.value = document.getString("profilePictureUrl") ?: ""
                    } else {
                        _errorMessage.value = "User data not found"
                    }
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = "Failed to load user data: ${e.message}"
                }
        } ?: run {
            _errorMessage.value = "User not logged in"
        }
    }

    fun updateUserDetails(newUserName: String, newUserEmail: String, newProfilePictureUrl: String?) {
        val currentUser = authRepository.getCurrentUser()
        currentUser?.let { user ->
            viewModelScope.launch {
                val result = authRepository.updateUserDetails(user.uid, newUserName, newUserEmail, newProfilePictureUrl)
                if (result.isSuccess) {
                    _userName.value = newUserName
                    _userEmail.value = newUserEmail
                    newProfilePictureUrl?.let {
                        _userPhotoUrl.value = it
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            }
        }
    }

    fun fetchUserPosts() {
        val currentUser: FirebaseUser? = authRepository.getCurrentUser()
        currentUser?.let { user ->
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("posts")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val posts = documents.map { document ->
                        document.toObject(Post::class.java)
                    }
                    _userPosts.value = posts
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = "Failed to load posts: ${e.message}"
                }
        } ?: run {
            _errorMessage.value = "User not logged in"
        }
    }




    fun updatePost(post: Post) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts").document(post.id)
            .set(post)
            .addOnSuccessListener {
                // Update the post in the list
                val currentPosts = _userPosts.value?.toMutableList() ?: mutableListOf()
                val index = currentPosts.indexOfFirst { it.id == post.id }
                if (index != -1) {
                    currentPosts[index] = post
                    _userPosts.value = currentPosts
                } else {
                    // Post doesn't exist in the current list, add it
                    currentPosts.add(post)
                    _userPosts.value = currentPosts
                }

                // Update in local database
                viewModelScope.launch(Dispatchers.IO) {
                    postDao.updatePost(post)
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to update post: ${e.message}"
            }
    }

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    fun deletePost(postId: String) {
        val firestore = FirebaseFirestore.getInstance()

        viewModelScope.launch {
            try {
                // First check if the post exists
                val documentSnapshot = withContext(Dispatchers.IO) {
                    firestore.collection("posts").document(postId).get().await()
                }

                if (!documentSnapshot.exists()) {
                    _errorMessage.value = "Post not found in Firebase"
                    return@launch
                }

                // Then try to delete it
                withContext(Dispatchers.IO) {
                    // Make sure to use await() to ensure the operation completes
                    firestore.collection("posts").document(postId).delete().await()

                    // Verify deletion happened
                    val verifySnapshot = firestore.collection("posts").document(postId).get().await()
                    if (verifySnapshot.exists()) {
                        throw Exception("Post was not deleted from Firebase")
                    }

                    // Only delete from local DB if Firebase deletion was successful
                    postDao.deleteById(postId)
                }

                // Update UI after successful deletion
                val currentPosts = _userPosts.value?.toMutableList() ?: mutableListOf()
                currentPosts.removeAll { it.id == postId }
                _userPosts.value = currentPosts
                _toastMessage.value = "Post deleted successfully"

                // Log success message
                Log.d("ProfileViewModel", "Post with ID $postId successfully deleted")

            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete post: ${e.message}"
                Log.e("ProfileViewModel", "Error deleting post with ID $postId", e)
            }
        }
    }

    // In ProfileViewModel, add this function for debugging
    fun verifyPostIds() {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val firestorePosts = firestore.collection("posts")
                    .whereEqualTo("userId", authRepository.getCurrentUser()?.uid ?: "")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.id }

                Log.d("ProfileViewModel", "Firebase Post IDs: $firestorePosts")

                val localPosts = withContext(Dispatchers.IO) {
                    postDao.getAllPostsAsList().map { it.id }
                }

                Log.d("ProfileViewModel", "Local DB Post IDs: $localPosts")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error verifying post IDs", e)
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()
    }
}