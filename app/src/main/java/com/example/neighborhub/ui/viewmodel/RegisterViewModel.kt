package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.neighborhub.repository.AuthRepository
import kotlinx.coroutines.Dispatchers

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Register user and return result as LiveData
    fun registerUser(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val result = authRepository.registerUser(email, password)
            if (result != null) {
                emit(true) // Emit true if registration was successful
            } else {
                emit(false) // Emit false if registration failed
            }
        } catch (e: Exception) {
            emit(false) // Emit false in case of any errors during registration
        }
    }
}
