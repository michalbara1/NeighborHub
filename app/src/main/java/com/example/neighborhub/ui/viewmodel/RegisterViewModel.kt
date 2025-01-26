package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.neighborhub.repository.AuthRepository
import kotlinx.coroutines.Dispatchers

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Register a new user with email and password
    fun registerUser(email: String, password: String) = liveData(Dispatchers.IO) {
        val result = authRepository.registerUser(email, password)
        if (result.isSuccess) {
            // Emit success message if registration is successful
            emit(result.getOrNull())
        } else {
            // Emit the error message if registration failed
            emit(result.exceptionOrNull()?.message)
        }
    }
}
