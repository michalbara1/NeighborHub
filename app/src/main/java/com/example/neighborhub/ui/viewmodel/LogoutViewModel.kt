package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.repository.AuthRepository
import kotlinx.coroutines.launch

class LogoutViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun logoutUser() {
        viewModelScope.launch {
            authRepository.logoutUser()
        }
    }
}
