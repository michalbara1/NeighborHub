package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.neighborhub.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import com.google.firebase.auth.FirebaseUser

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Login user and return result as LiveData
    fun loginUser(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            val result = authRepository.loginUser(email, password)
            if (result != null) {
                emit(true) // Emit true if login is successful
            } else {
                emit(false) // Emit false if login failed
            }
        } catch (e: Exception) {
            emit(false) // Emit false in case of any errors during login
        }
    }

    // Method to get the current logged-in user
    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
