package com.example.neighborhub.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import kotlinx.coroutines.launch

class AddPostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun addPost(post: Post) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.addPost(post)
                _success.value = true
                _errorMessage.value = null
                Log.d("AddPostViewModel", "Post added successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add post: ${e.message}"
                Log.e("AddPostViewModel", "Failed to add post", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(uri: Uri, callback: (String?) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", "post_images/")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("AddPostViewModel", "Image upload started: $requestId")
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        Log.d("AddPostViewModel", "Image uploaded successfully: $imageUrl")
                    } else {
                        Log.e("AddPostViewModel", "Image upload failed: No secure_url found")
                    }
                    callback(imageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("AddPostViewModel", "Image upload failed: ${error?.description}")
                    callback(null)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            }).dispatch()
    }

}