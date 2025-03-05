package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.repository.PostRepository

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(authRepository, postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}