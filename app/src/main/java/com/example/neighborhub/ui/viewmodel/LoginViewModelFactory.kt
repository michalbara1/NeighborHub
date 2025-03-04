//package com.example.neighborhub.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.neighborhub.repository.AuthRepository
//
//// Factory to create an instance of LoginViewModel
//class LoginViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//    // Override create() method to provide custom ViewModel creation logic
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
//            return UserViewModel(authRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
