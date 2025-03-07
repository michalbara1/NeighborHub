package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.neighborhub.model.data.AppDatabase
import kotlinx.coroutines.launch

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userPhotoUrl = MutableLiveData<String>()
    val userPhotoUrl: LiveData<String> = _userPhotoUrl

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

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

    fun logoutUser() {
        authRepository.logoutUser()
    }
}