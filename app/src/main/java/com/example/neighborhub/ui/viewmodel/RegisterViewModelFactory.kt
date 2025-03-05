//package com.example.neighborhub.ui.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.neighborhub.repository.AuthRepository
//
//class RegisterViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
//            return UserViewModel(authRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
