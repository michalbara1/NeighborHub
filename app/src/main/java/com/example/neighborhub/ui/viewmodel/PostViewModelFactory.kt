package com.example.neighborhub.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neighborhub.repository.PostRepository

class PostViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}