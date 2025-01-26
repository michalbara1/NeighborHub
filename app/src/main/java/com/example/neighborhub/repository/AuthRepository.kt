package com.example.neighborhub.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    // Registration function
    suspend fun registerUser(email: String, password: String): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success("Registration successful")
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> Result.failure(Exception("Invalid email address"))
                "ERROR_WEAK_PASSWORD" -> Result.failure(Exception("Password should be at least 6 characters"))
                "ERROR_EMAIL_ALREADY_IN_USE" -> Result.failure(Exception("Email already in use"))
                else -> Result.failure(e) // For any other error, return the original exception
            }
        } catch (e: Exception) {
            Result.failure(Exception("An unknown error occurred"))
        }
    }

    // Login user with Firebase
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            Log.d("AuthRepository", "Attempting to log in user with email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "User logged in successfully: ${result.user?.email}")
            Result.success(result.user) // Return the FirebaseUser inside a Result
        } catch (e: FirebaseAuthException) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> Result.failure(Exception("Invalid email address"))
                "ERROR_WRONG_PASSWORD" -> Result.failure(Exception("Incorrect password"))
                else -> Result.failure(Exception("Login failed: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            Result.failure(Exception("An unknown error occurred"))
        }
    }

    // Get the current logged-in user
    fun getCurrentUser(): FirebaseUser? {
        val user = auth.currentUser
        Log.d("AuthRepository", "Current user: ${user?.email ?: "No user logged in"}")
        return user
    }

    // Logout the current user
    fun logoutUser() {
        auth.signOut()
        Log.d("AuthRepository", "User logged out")
    }
}
