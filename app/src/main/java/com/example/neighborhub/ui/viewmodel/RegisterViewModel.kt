//package com.example.neighborhub.ui.viewmodel
//
//import android.app.Application
//import android.util.Log
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.liveData
//import com.example.neighborhub.model.User
//import com.example.neighborhub.model.data.AppDatabase
//import com.example.neighborhub.repository.AuthRepository
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.Dispatchers
//
//class RegisterViewModel(
//    application: Application,
//    private val authRepository: AuthRepository
//) : AndroidViewModel(application)  {
//
//
//    private val auth = FirebaseAuth.getInstance()
//    private val firestore = FirebaseFirestore.getInstance()
//
//    private val database = AppDatabase.getInstance(application.applicationContext)
//    private val userDao = database.UserDao()
//
//    init { Log.d("DatabaseInit", "Database: $database, UserDao: $userDao") } // Debugging statement to verify Database launch
//
//    // Register a new user with email and password
//    fun registerUser(email: String, password: String) = liveData(Dispatchers.IO) {
//        val result = authRepository.registerUser(email, password)
//        if (result.isSuccess) {
//            val firebaseUser = auth.currentUser
//            if (firebaseUser != null) {
//                val user = User(
//                    userId = firebaseUser.uid, // Assuming firebaseUser has a uid property
//                    username = "defaultUsername", // Replace with actual username
//                    email = email,
//                    profileImageUrl = null, // Replace with actual profile image URL if available
//                    password = password
//                )
//                try {
//                    userDao.insertUser(user) // Save user in Room database
//                    emit("Registration successful and saved locally") // Emit success message
//                } catch (e: Exception) {
//                    emit("User registered but failed to save locally: ${e.message}")
//                }
//            } else {
//                emit("User registration failed")
//            }
//        } else {
//            emit(result.exceptionOrNull()?.message ?: "Registration failed")
//        }
//    }
//
//
//}
