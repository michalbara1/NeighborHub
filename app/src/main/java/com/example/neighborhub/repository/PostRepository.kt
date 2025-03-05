//package com.example.neighborhub.repository
//
//import android.util.Log
//import com.example.neighborhub.model.Post
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class PostRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//
//    suspend fun getAllPosts(): List<Post> {
//        return try {
//            val result = db.collection("posts").get().await()
//            result.toObjects(Post::class.java)
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//
//    suspend fun getPostByHeadline(headline: String): Post? {
//        return try {
//            val result = db.collection("posts")
//                .whereEqualTo("headline", headline)
//                .get().await()
//            result.documents.firstOrNull()?.toObject(Post::class.java)
//        } catch (e: Exception) {
//            // Log the error for debugging
//            Log.e("PostRepository", "Error fetching post: ${e.message}")
//            null
//        }
//    }
//
//    suspend fun addPost(post: Post) {
//        try {
//            db.collection("posts").add(post).await()
//        } catch (e: Exception) {
//            throw e // Rethrow exception for the ViewModel to handle
//        }
//    }
//
//}

package com.example.neighborhub.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.neighborhub.model.Post
import com.example.neighborhub.model.data.AppDatabase
import com.example.neighborhub.model.data.PostDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class PostRepository(context: Context) {



    private val db = FirebaseFirestore.getInstance()
    private val postDao: PostDao = AppDatabase.getInstance(context).postDao()

    init {
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "dxzj1wktd"
        config["api_key"] = "589877463742111"
        config["api_secret"] = "8qCTGSOv63Bm_Rt1VMU5tweqW2k"
        MediaManager.init(context, config)
    }



    suspend fun getAllPosts(): List<Post> {
        return try {
            val result = db.collection("posts").get().await()
            result.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPostsByUserId(userId: String): LiveData<List<Post>> {
        return postDao.getPostsByUserId(userId)
    }

    suspend fun deletePost(post: Post) {
        postDao.delete(post)
    }

    suspend fun getPostByHeadline(headline: String): Post? {
        return try {
            val result = db.collection("posts")
                .whereEqualTo("headline", headline)
                .get().await()
            result.documents.firstOrNull()?.toObject(Post::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addPost(post: Post) {
        try {
            db.collection("posts").add(post).await()
            postDao.insertPosts(post)
        } catch (e: Exception) {
            throw e
        }
    }


    suspend fun uploadImageToCloudinary(imagePath: String): String? {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(imagePath)
                .unsigned("your_upload_preset")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        // Optional: Log upload start
                    }

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as? String
                        if (!imageUrl.isNullOrEmpty()) {
                            continuation.resume(imageUrl)
                        } else {
                            continuation.resume(null) // No secure_url found
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo) {
                        continuation.resume(null) // Return null on error
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        // Optional: Track upload progress
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo) {
                        continuation.resume(null) // Handle rescheduling errors
                    }
                }).dispatch()
        }
    }
}
