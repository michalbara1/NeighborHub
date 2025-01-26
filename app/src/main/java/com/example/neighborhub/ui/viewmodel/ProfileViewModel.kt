package com.example.app.ui.profile

import androidx.lifecycle.ViewModel
import com.example.neighborhub.repository.AuthRepository

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun getCurrentUser() = authRepository.getCurrentUser()
}
