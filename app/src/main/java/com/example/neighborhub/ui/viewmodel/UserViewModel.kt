//package com.example.neighborhub.ui.viewmodel
//
//import android.app.Application
//import android.content.Context
//import android.net.Uri
//import android.util.Log
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import com.cloudinary.android.callback.UploadCallback
//import com.example.neighborhub.model.User
//import com.example.neighborhub.model.data.AppDatabase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.Dispatchers
//import com.cloudinary.android.MediaManager
//import com.example.neighborhub.model.Image
//import com.example.neighborhub.repository.AuthRepository
//import com.google.firebase.auth.EmailAuthProvider
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import okhttp3.OkHttpClient
//import okhttp3.FormBody
//import okhttp3.Request
//import java.io.IOException
//import okhttp3.Callback
//import okhttp3.Response
//import java.security.MessageDigest
//
//
//class UserViewModel(application: Application) : AndroidViewModel(application) {
//    private val auth = FirebaseAuth.getInstance()
//    private val firestore = FirebaseFirestore.getInstance()
//    private val authRepository = AuthRepository()
//    private val database = AppDatabase.getInstance(application.applicationContext)
//    private val userDao = database.userDao()
//    private val context = getApplication<Application>().applicationContext
//
//
//    init { Log.d("DatabaseInit", "Database: $database, UserDao: $userDao") } // Debugging statement to verify Database launch
//
//    sealed class RegistrationResult { object Success : RegistrationResult() data class Failure(val message: String?) : RegistrationResult() }
//
//    // Updating user information in Room
//    fun updateUserInRoom(user: User) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                userDao.insertUser(user)
//                Log.d("UserViewModel", "User updated in Room successfully: $user")
//            } catch (e: Exception) {
//                Log.e("UserViewModel", "Failed to update user in Room: ${e.message}")
//            }
//        }
//    }
//
//    fun uploadProfileImage(uri: Uri): LiveData<Pair<Boolean, String?>> {
//        val uploadStatus = MutableLiveData<Pair<Boolean, String?>>()
//        try {
//            MediaManager.get().upload(uri)
//                .option("folder", "profile_pictures/") // Image source in Cloudinary
//                .callback(object : UploadCallback {
//                    override fun onStart(requestId: String?) {
//                        Log.d("UserViewModel", "Uploading image started...")
//                    }
//
//                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
//                        val imageUrl = (resultData as? Map<*, *>)?.get("secure_url") as? String
//                        if (!imageUrl.isNullOrEmpty()) {
//                            Log.d("UserViewModel", "Image uploaded successfully: $imageUrl")
//                            uploadStatus.postValue(Pair(true, imageUrl))
//                        } else {
//                            Log.e("UserViewModel", "Image upload failed: No secure_url found")
//                            uploadStatus.postValue(Pair(false, null))
//                        }
//                    }
//
//                    override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
//                        error?.let {
//                            Log.e("UserViewModel", "Image upload failed: ${it.description}")
//                            uploadStatus.postValue(Pair(false, it.description))
//                        } ?: run {
//                            Log.e("UserViewModel", "Image upload failed with unknown error")
//                            uploadStatus.postValue(Pair(false, "Unknown error occurred"))
//                        }
//                    }
//
//                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
//
//                    override fun onReschedule(
//                        requestId: String?,
//                        error: com.cloudinary.android.callback.ErrorInfo?
//                    ) {}
//                }).dispatch()
//        } catch (e: IllegalStateException) {
//            Log.e("UserViewModel", "MediaManager not initialized: ${e.message}")
//            uploadStatus.postValue(Pair(false, "MediaManager not initialized. Restart the app."))
//        }
//        return uploadStatus
//    }
//
//    // Delete profile picture
//    fun deleteProfileImage(imageUrl: String, callback: ((Boolean) -> Unit)? = null) {
//        // Verifying API credentials
//        val CLOUD_NAME = "dxzj1wktd"
//        val API_KEY = "589877463742111"
//        val API_SECRET = "8qCTGSOv63Bm_Rt1VMU5tweqW2k"
//
//        // Extract the public ID from the URL
//        val publicId = "profile_pictures/" + imageUrl.substringAfterLast("/").substringBeforeLast(".")
//        val timestamp = (System.currentTimeMillis() / 1000).toString()
//
//        // Generate the correct signature string (including invalidate=true)
//        val signatureString = "invalidate=true&public_id=$publicId&timestamp=$timestamp$API_SECRET"
//        val signature = MessageDigest.getInstance("SHA-1")
//            .digest(signatureString.toByteArray())
//            .joinToString("") { "%02x".format(it) }
//
//        // Prepare the request body (include api_key [SUPER IMPORTANT])
//        val requestBody = FormBody.Builder()
//            .add("public_id", publicId)
//            .add("timestamp", timestamp)
//            .add("signature", signature)
//            .add("invalidate", "true")
//            .add("api_key", API_KEY)
//            .build()
//
//        val requestUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy"
//
//        val request = Request.Builder().url(requestUrl).post(requestBody).build()
//
//        // Send the request
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: okhttp3.Call, e: IOException) {
//                Log.e("Cloudinary", "Failed to delete image: ${e.message}")
//                callback?.invoke(false)
//            }
//
//            override fun onResponse(call: okhttp3.Call, response: Response) {
//                val responseBody = response.body?.string() ?: "No response body"
//                if (response.isSuccessful) {
//                    Log.d("Cloudinary", "Successfully deleted image: $publicId, Response: $responseBody")
//                    callback?.invoke(true)
//                } else {
//                    Log.e("Cloudinary", "Failed to delete image: ${response.message}, Response: $responseBody")
//                    callback?.invoke(false)
//                }
//            }
//        })
//    }
//
//    fun register(
//
//        email: String,
//        password: String,
//        username: String
//    ): LiveData<RegistrationResult> {
//        val result = MutableLiveData<RegistrationResult>()
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
//                    saveUserDetailsToFirestore(userId, username, email, password, result)
//                } else {
//                    result.value = RegistrationResult.Failure(task.exception?.message)
//                }
//            }
//        return result
//
//    }
//
//    private fun saveUserDetailsToFirestore(
//        userId: String,
//        username: String,
//        email: String,
//        password: String,
//        result: MutableLiveData<RegistrationResult>) {
//        val user = hashMapOf(
//            "userId" to userId,
//            "username" to username,
//            "email" to email,
//            "password" to password)
//        firestore.collection("users").document(userId).set(user).addOnSuccessListener {
//            result.value = RegistrationResult.Success
//        }.addOnFailureListener{ e ->
//            result.value = RegistrationResult.Failure(e.message)
//        }
//    }
//
//    fun getCurrentUser() = auth.currentUser
//
//    sealed class LoginResult { object Success : LoginResult()data class Failure(val message: String?) : LoginResult() }
//
//
//    fun login(email: String, password: String): LiveData<LoginResult> {
//        val loginResult = MutableLiveData<LoginResult>()
//        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
//        firestore.collection("users").whereEqualTo("email", email).get()
//            .addOnSuccessListener { querySnapshot ->
//                if (!querySnapshot.isEmpty) {
//                    val userDocument = querySnapshot.documents[0]
//                    val userId = userDocument.id
//                    val activeSession = userDocument.getString("activeSession")
//                    // Check if the user is already logged in from another device
//                    if (activeSession != null && activeSession != deviceId) {
//                        loginResult.value = LoginResult.Failure("You are already logged in from another device.")
//                        return@addOnSuccessListener
//                    }
//                    // Proceed with Firebase Authentication
//                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Log.d("UserViewModel", "Login successful for email: $email")
//                            val firebaseUser = auth.currentUser
//                            if (firebaseUser != null) {
//                                Log.d("UserViewModel", "Fetching user data from Firestore for userId: ${firebaseUser.uid}")
//                                firestore.collection("users").document(firebaseUser.uid)
//                                    .get()
//                                    .addOnSuccessListener { document ->
//                                        // If login is successful, save user data to Room
//                                        if (document.exists()) {
//                                            Log.d("UserViewModel", "User data fetched from Firestore: ${document.data}")
//                                            val userEntity = User(
//                                                userId = firebaseUser.uid,
//                                                username = document.getString("username") ?: "Unknown",
//                                                email = document.getString("email") ?: email,
//                                                profileImageUrl = document.getString("profilePictureUrl") ?: "Unknown",
//                                                password = document.getString("password") ?: password
//                                            )
//                                            viewModelScope.launch(Dispatchers.IO) {
//                                                try {
//                                                    Log.d("UserViewModel", "Inserting user into Room: $userEntity")
//                                                    userDao.insertUser(userEntity)
//                                                    // Fetch and save app image URLs
//                                                    fetchAndSaveImageUrls(context)
//                                                    Log.d("UserViewModel", "User inserted into Room successfully")
//                                                } catch (e: Exception) {
//                                                    Log.e("UserViewModel", "Failed to insert user into Room: ${e.message}")
//                                                }
//                                            }
//                                            // Update the active session in Firestore
//                                            firestore.collection("users").document(firebaseUser.uid)
//                                                .update("activeSession", deviceId)
//                                                .addOnSuccessListener {
//                                                    Log.d("UserViewModel", "Active session updated for user: $userId")
//                                                    loginResult.value = LoginResult.Success
//                                                }
//                                                .addOnFailureListener { e ->
//                                                    Log.e("UserViewModel", "Failed to update active session: ${e.message}")
//                                                    loginResult.value = LoginResult.Failure("Failed to update active session.")
//                                                }
//                                        } else {
//                                            Log.e("UserViewModel", "User document does not exist in Firestore")
//                                            loginResult.value = LoginResult.Failure("User data not found in Firestore.")
//                                        }
//                                    }
//                                    .addOnFailureListener { e ->
//                                        Log.e("UserViewModel", "Failed to fetch user data from Firestore: ${e.message}")
//                                        loginResult.value = LoginResult.Failure("Failed to fetch user data.")
//                                    }
//                            } else {
//                                loginResult.value = LoginResult.Failure("Firebase user is null.")
//                            }
//                        } else {
//                            Log.e("UserViewModel", "Login failed: ${task.exception?.message}")
//                            loginResult.value = LoginResult.Failure(task.exception?.message)
//                        }
//                    }
//                } else {
//                    loginResult.value = LoginResult.Failure("User not found.")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("UserViewModel", "Failed to fetch user: ${e.message}")
//                loginResult.value = LoginResult.Failure("Failed to fetch user details.")
//            }
//
//        return loginResult
//    }
//
//    private suspend fun fetchAndSaveImageUrls(context: Context) {
//        try {
//            val imageCollection = firestore.collection("appImages") // to see if this true
//            val snapshot = imageCollection.get().await()
//            // Fetch all image URLs from Firestore and map them to AppImageEntity
//            val imageUrls = snapshot.documents.mapIndexedNotNull { index, document ->
//                document.getString("appImageUrl")?.let { url ->
//                    val key = when (index) {
//                        0 -> "backgroundImage" // Assign a stable key for each image
//                        1 -> "DataImage"
//                        else -> "otherImage$index"
//                    }
//                    Image(key = key, url = url)
//                }
//            }
//            // Access the database and perform operations
//            val database = AppDatabase.getInstance(context)
//            val imageDao = database.imageDao()
//            imageDao.clearTable()
//            imageDao.insertAll(imageUrls)
//            Log.d("FetchAndSave", "Image URLs successfully saved to Room")
//        } catch (e: Exception) {
//            Log.e("FetchAndSave", "Failed to fetch or save image URLs: ${e.message}")
//        }
//    }
//
//
//    fun logout(userId: String) {
//        firestore.collection("users").document(userId).update("activeSession", null)
//            .addOnSuccessListener {
//                Log.d("UserViewModel", "Active session cleared for user: $userId")
//            }
//            .addOnFailureListener { e ->
//                Log.e("UserViewModel", "Failed to clear active session: ${e.message}")
//            }
//        auth.signOut()
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                userDao.deleteAllUsers() // Clear the Room database
//                database.imageDao().clearTable()
//                Log.d("UserViewModel", "All users deleted from Room database.")
//            } catch (e: Exception) {
//                Log.e("UserViewModel", "Failed to delete all users from Room: ${e.message}")
//            }
//        }
//    }
//
//    fun fetchUserDetails(userId: String, callback: (Map<String, Any>) -> Unit) {
//        Log.d("UserViewModel", "Fetching user details for userId: $userId")
//        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
//            if (document.exists()) {
//                val userData = document.data ?: emptyMap()
//                Log.d("UserViewModel", "User data fetched from Firestore: $userData")
//                callback(userData)
//                val userEntity = User(
//                    userId = userData["userId"] as String,
//                    username = userData["username"] as? String ?: "Unknown",
//                    email = userData["email"] as? String ?: "Unknown",
//                    profileImageUrl = document.getString("profilePictureUrl") ?: "Unknown",
//                    password = userData["password"] as? String ?: "" // Fetch password
//                )
//                viewModelScope.launch(Dispatchers.IO) {
//                    try {
//                        Log.d("UserViewModel", "Caching user data in Room: $userEntity")
//                        userDao.insertUser(userEntity)
//                        Log.d("UserViewModel", "User cached in Room successfully")
//                    } catch (e: Exception) {
//                        Log.e("UserViewModel", "Failed to cache user in Room: ${e.message}")
//                    }
//                }
//            } else {
//                Log.e("UserViewModel", "User document does not exist in Firestore")
//                callback(emptyMap())
//            }
//        }
//            .addOnFailureListener { exception ->
//                Log.e("UserViewModel", "Failed to fetch user data from Firestore: ${exception.message}")
//                // Fallback to Room
//                viewModelScope.launch(Dispatchers.IO) {
//                    try {
//                        val cachedUser = userDao.getUser(userId)
//                        if (cachedUser != null) {
//                            Log.d("UserViewModel", "Returning cached user from Room: $cachedUser")
//                            val cachedData = mapOf(
//                                "userId" to cachedUser.userId,
//                                "username" to cachedUser.username,
//                                "email" to cachedUser.email,
//                                "profileImageUrl" to (cachedUser.profileImageUrl ?: "Unknown"),
//                                "password" to cachedUser.password
//                            )
//                            callback(cachedData)
//                        } else {
//                            Log.d("UserViewModel", "No cached user found in Room for userId: $userId")
//                            callback(emptyMap())
//                        }
//                    } catch (e: Exception) {
//                        Log.e("UserViewModel", "Error fetching user from Room: ${e.message}")
//                        callback(emptyMap())
//                    }
//                }
//            }
//    }
//
//    // Fetching Review Count
//    fun fetchReviewCount(userId: String, callback: (Int) -> Unit) {
//        firestore.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener { documents ->
//            callback(documents.size())
//        }
//            .addOnFailureListener {
//                callback(0)
//            }
//    }
//
//    // Fetching Review Count
//    fun fetchCommentCount(userId: String, callback: (Int) -> Unit) {
//        firestore.collection("comments").whereEqualTo("userId", userId).get().addOnSuccessListener { documents ->
//            callback(documents.size())
//        }
//            .addOnFailureListener {
//                callback(0)
//            }
//    }
//
//    // Updating Email address
//    fun updateEmail(newEmail: String, password: String, callback: (Boolean, String?) -> Unit) {
//        val user = FirebaseAuth.getInstance().currentUser
//        val credential = EmailAuthProvider.getCredential(user?.email ?: "", password)
//        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
//            if (authTask.isSuccessful) {
//                Log.d("EditPersonalDetails", "Reauthentication successful")
//                // Send verification email
//                user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { verificationTask ->
//                    if (verificationTask.isSuccessful) {
//                        Log.d("EditPersonalDetails", "Verification email sent to $newEmail")
//                        callback(true, "Verification email sent. Please check your inbox.")
//                    } else {
//                        Log.e("EditPersonalDetails", "Failed to send verification email: ${verificationTask.exception?.message}")
//                        callback(false, verificationTask.exception?.message)
//                    }
//                }
//            } else {
//                Log.e("EditPersonalDetails", "Reauthentication failed: ${authTask.exception?.message}")
//                callback(false, "Reauthentication failed: ${authTask.exception?.message}")
//            }
//        }
//    }
//
//    // Updating password
//    fun updatePassword(currentPassword: String, newPassword: String, callback: (Boolean, String?) -> Unit) {
//        val user = FirebaseAuth.getInstance().currentUser
//        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
//        // Reauthenticate the user
//        user?.reauthenticate(credential)?.addOnCompleteListener { authTask ->
//            if (authTask.isSuccessful) {
//                // Proceed with password update in Firebase Authentication
//                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
//                    if (updateTask.isSuccessful) {
//                        val userId = user.uid
//                        // Save the new password to Firestore
//                        firestore.collection("users").document(userId).update("password", newPassword)
//                            .addOnSuccessListener {
//                                Log.d("UserViewModel", "Password updated in Firestore successfully.")
//                                // Save the new password to Room
//                            }
//                            .addOnFailureListener { e ->
//                                Log.e("UserViewModel", "Failed to update password in Firestore: ${e.message}")
//                            }
//
//                        callback(true, null)
//                    } else {
//                        callback(false, updateTask.exception?.message)
//                    }
//                }
//            } else {
//                callback(false, authTask.exception?.message)
//            }
//        }
//    }
//
//
//    // Updating user data in live-data
//    fun updateUserDetails(userId: String, updatedData: Map<String, Any>, callback: (Boolean) -> Unit) {
//        firestore.collection("users").document(userId).update(updatedData)
//            .addOnSuccessListener {
//                callback(true)
//            }
//            .addOnFailureListener {
//                callback(false)
//            }
//    }
//
//    // Updating comments and review amount in live-data
//    fun updateReviewsAndComments(userId: String, updatedData: Map<String, Any>) {
//        val updatedUsername = updatedData["username"] as String
//        // Update reviews
//        firestore.collection("posts").whereEqualTo("userId", userId).get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    document.reference.update("username", updatedUsername)
//                }
//            }
//        // Update comments
//        firestore.collection("comments").whereEqualTo("userId", userId).get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    document.reference.update("username", updatedUsername)
//                }
//            }
//    }
//}
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

    // Registration Result
    sealed class RegistrationResult {
        object Success : RegistrationResult()
        data class Failure(val message: String?) : RegistrationResult()
    }

    // Login Result
    sealed class LoginResult {
        object Success : LoginResult()
        data class Failure(val message: String?) : LoginResult()
    }

    // Register a new user
    fun register(email: String, password: String, username: String): LiveData<RegistrationResult> {
        val result = MutableLiveData<RegistrationResult>()

        // Validate input fields
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

    // Save user details to Firestore
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

    // Upload profile image to Cloudinary
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

    // Delete profile image from Cloudinary
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

    // Get current user
    fun getCurrentUser() = auth.currentUser

    // Login user
    fun login(email: String, password: String): LiveData<LoginResult> {
        val loginResult = MutableLiveData<LoginResult>()
        val deviceId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)

        Log.d("UserViewModel", "Attempting to login with email: $email")

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
                            loginResult.value = LoginResult.Failure(task.exception?.message)
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

    // Logout user
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