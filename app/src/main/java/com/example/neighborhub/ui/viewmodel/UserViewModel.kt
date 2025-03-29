package com.example.neighborhub.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.neighborhub.model.User
import com.example.neighborhub.model.data.AppDatabase
import com.example.neighborhub.model.Image
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.*
import java.io.IOException
import java.security.MessageDigest

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository()
    private val database = AppDatabase.getInstance(application.applicationContext)
    private val userDao = database.userDao()
    private val context = getApplication<Application>().applicationContext

    init {
        Log.d("DatabaseInit", "Database: $database, UserDao: $userDao")
    }


    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Failure(val message: String?) : RegistrationResult()
    }


    sealed class LoginResult {
        object Success : LoginResult()
        data class Failure(val message: String?) : LoginResult()
    }


    fun register(email: String, password: String, username: String): LiveData<RegistrationResult> {
        val result = MutableLiveData<RegistrationResult>()


        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            result.value = RegistrationResult.Failure("All fields are required.")
            return result
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        viewModelScope.launch {
                            val firestoreResult = saveUserDetailsToFirestore(userId, username, email, password)
                            result.postValue(firestoreResult)
                        }
                    } else {
                        result.value = RegistrationResult.Failure("User ID is null.")
                    }
                } else {
                    result.value = RegistrationResult.Failure(task.exception?.message ?: "Registration failed.")
                }
            }

        return result
    }


    private suspend fun saveUserDetailsToFirestore(
        userId: String,
        username: String,
        email: String,
        password: String
    ): RegistrationResult {
        return try {
            val user = hashMapOf(
                "userId" to userId,
                "username" to username,
                "email" to email,
                "password" to password
            )
            firestore.collection("users").document(userId).set(user).await()
            RegistrationResult.Success
        } catch (e: Exception) {
            RegistrationResult.Failure(e.message)
        }
    }


    fun uploadProfileImage(uri: Uri): LiveData<Pair<Boolean, String?>> {
        val uploadStatus = MutableLiveData<Pair<Boolean, String?>>()

        if (!NetworkUtils.isOnline(context)) {
            uploadStatus.postValue(Pair(false, "No internet connection"))
            return uploadStatus
        }

        try {
            MediaManager.get().upload(uri)
                .option("folder", "profile_pictures/")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("UserViewModel", "Upload started with requestId: $requestId")
                    }

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (!imageUrl.isNullOrEmpty()) {
                            Log.d("UserViewModel", "Image uploaded successfully: $imageUrl")
                            uploadStatus.postValue(Pair(true, imageUrl))

                            // Save the image URL to Firestore
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                firestore.collection("users").document(userId)
                                    .update("profilePictureUrl", imageUrl)
                                    .addOnSuccessListener {
                                        Log.d("UserViewModel", "Profile picture URL saved to Firestore")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("UserViewModel", "Failed to save profile picture URL: ${e.message}")
                                    }
                            }
                        } else {
                            Log.e("UserViewModel", "Image upload failed: No secure_url found")
                            uploadStatus.postValue(Pair(false, "No secure_url found"))
                        }
                    }

                    override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        error?.let {
                            Log.e("UserViewModel", "Image upload failed: ${it.description}")
                            uploadStatus.postValue(Pair(false, it.description))
                        } ?: run {
                            Log.e("UserViewModel", "Image upload failed with unknown error")
                            uploadStatus.postValue(Pair(false, "Unknown error occurred"))
                        }
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.d("UserViewModel", "Upload progress: $bytes/$totalBytes")
                    }

                    override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                        Log.d("UserViewModel", "Upload rescheduled with requestId: $requestId, error: ${error?.description}")
                    }
                }).dispatch()
        } catch (e: IllegalStateException) {
            Log.e("UserViewModel", "MediaManager not initialized: ${e.message}")
            uploadStatus.postValue(Pair(false, "MediaManager not initialized. Restart the app."))
        } catch (e: Exception) {
            Log.e("UserViewModel", "Unexpected error during upload: ${e.message}")
            uploadStatus.postValue(Pair(false, "Unexpected error: ${e.message}"))
        }

        return uploadStatus
    }




    fun deleteProfileImage(imageUrl: String, callback: ((Boolean) -> Unit)? = null) {
        val CLOUD_NAME = "dxzj1wktd"
        val API_KEY = "589877463742111"
        val API_SECRET = "8qCTGSOv63Bm_Rt1VMU5tweqW2k"

        val publicId = "profile_pictures/" + imageUrl.substringAfterLast("/").substringBeforeLast(".")
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val signatureString = "invalidate=true&public_id=$publicId&timestamp=$timestamp$API_SECRET"
        val signature = MessageDigest.getInstance("SHA-1")
            .digest(signatureString.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val requestBody = FormBody.Builder()
            .add("public_id", publicId)
            .add("timestamp", timestamp)
            .add("signature", signature)
            .add("invalidate", "true")
            .add("api_key", API_KEY)
            .build()

        val requestUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy"
        val request = Request.Builder().url(requestUrl).post(requestBody).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Cloudinary", "Failed to delete image: ${e.message}")
                callback?.invoke(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "No response body"
                if (response.isSuccessful) {
                    Log.d("Cloudinary", "Successfully deleted image: $publicId, Response: $responseBody")
                    callback?.invoke(true)
                } else {
                    Log.e("Cloudinary", "Failed to delete image: ${response.message}, Response: $responseBody")
                    callback?.invoke(false)
                }
            }
        })
    }

    fun deleteUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val credential = EmailAuthProvider.getCredential(email, password)

        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        firestore.collection("users").document(user.uid).delete()
                            .addOnSuccessListener {
                                callback(true, "User deleted successfully.")
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Failed to delete user data: ${e.message}")
                            }
                    } else {
                        callback(false, "Failed to delete user: ${deleteTask.exception?.message}")
                    }
                }
            } else {
                callback(false, "Reauthentication failed: ${authTask.exception?.message}")
            }
        }
    }


    fun getCurrentUser() = auth.currentUser


    fun login(email: String, password: String): LiveData<LoginResult> {
        val loginResult = MutableLiveData<LoginResult>()
        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)

        Log.d("UserViewModel", "Attempting to login with email: $email")


        if (email.isBlank() || password.isBlank()) {
            loginResult.value = LoginResult.Failure("Email and password cannot be empty.")
            return loginResult
        }


        if (!NetworkUtils.isOnline(context)) {
            loginResult.value = LoginResult.Failure("No internet connection")
            return loginResult
        }

        firestore.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("UserViewModel", "Firestore query successful, documents: ${querySnapshot.documents.size}")
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val userId = userDocument.id
                    val activeSession = userDocument.getString("activeSession")

                    Log.d("UserViewModel", "User document found, userId: $userId, activeSession: $activeSession")

                    if (activeSession != null && activeSession != deviceId) {
                        Log.d("UserViewModel", "User already logged in from another device")
                        loginResult.value = LoginResult.Failure("You are already logged in from another device.")
                        return@addOnSuccessListener
                    }

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("UserViewModel", "Firebase authentication successful")
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                Log.d("UserViewModel", "Updating active session in Firestore")
                                firestore.collection("users").document(firebaseUser.uid)
                                    .update("activeSession", deviceId)
                                    .addOnSuccessListener {
                                        Log.d("UserViewModel", "Active session updated successfully")
                                        loginResult.value = LoginResult.Success
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("UserViewModel", "Failed to update active session: ${e.message}")
                                        loginResult.value = LoginResult.Failure("Failed to update active session.")
                                    }
                            } else {
                                Log.e("UserViewModel", "Firebase user is null")
                                loginResult.value = LoginResult.Failure("Firebase user is null.")
                            }
                        } else {
                            Log.e("UserViewModel", "Firebase authentication failed: ${task.exception?.message}")
                            loginResult.value = LoginResult.Failure(task.exception?.message ?: "Login failed.")
                        }
                    }
                } else {
                    Log.e("UserViewModel", "No user document found in Firestore")
                    loginResult.value = LoginResult.Failure("User not found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Firestore query failed: ${e.message}")
                loginResult.value = LoginResult.Failure("Failed to fetch user details.")
            }

        return loginResult
    }


    fun logout(userId: String) {
        firestore.collection("users").document(userId).update("activeSession", null)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Active session cleared for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Failed to clear active session: ${e.message}")
            }
        auth.signOut()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.deleteAllUsers()
                database.imageDao().clearTable()
                Log.d("UserViewModel", "All users deleted from Room database.")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to delete all users from Room: ${e.message}")
            }
        }
    }

    fun updateUserDetails(userId: String, updatedData: Map<String, Any>, callback: (Boolean) -> Unit) {
        firestore.collection("users").document(userId).update(updatedData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}