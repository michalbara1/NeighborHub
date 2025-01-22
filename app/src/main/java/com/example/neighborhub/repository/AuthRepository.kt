package com.example.neighborhub.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    // Register user with Firebase
    suspend fun registerUser(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null // Return null if registration fails
        }
    }

    // Login user with Firebase
    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null // Return null if login fails
        }
    }

    // Get the current logged-in user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Logout the current user
    fun logoutUser() {
        auth.signOut()
    }
}
