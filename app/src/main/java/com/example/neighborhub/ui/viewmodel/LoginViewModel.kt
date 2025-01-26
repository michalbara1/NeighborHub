package com.example.neighborhub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.neighborhub.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Login user and return result as LiveData
    fun loginUser(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            // Call loginUser in AuthRepository, which returns Result
            val result = authRepository.loginUser(email, password)
            if (result.isSuccess) {
                emit(Result.success(result.getOrNull())) // Emit success with FirebaseUser if login is successful
            } else {
                emit(Result.failure<FirebaseUser?>(result.exceptionOrNull()!!)) // Emit failure with error
            }
        } catch (e: Exception) {
            emit(Result.failure<FirebaseUser?>(e)) // Emit failure if an unexpected error occurs
        }
    }

    // Method to get the current logged-in user
    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
