package com.example.neighborhub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neighborhub.repository.AuthRepository

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    //private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}