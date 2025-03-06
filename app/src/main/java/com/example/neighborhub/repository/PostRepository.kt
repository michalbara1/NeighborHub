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

//package com.example.neighborhub.repository
//
//import android.content.Context
//import androidx.lifecycle.LiveData
//import com.example.neighborhub.model.Post
//import com.example.neighborhub.model.data.AppDatabase
//import com.example.neighborhub.model.data.PostDao
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class PostRepository(context: Context) {
//
//    private val db = FirebaseFirestore.getInstance()
//    private val postDao: PostDao = AppDatabase.getInstance(context).postDao()
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
//    fun getPostsByUserId(userId: String): LiveData<List<Post>> {
//        return postDao.getPostsByUserId(userId)
//    }
//
//    suspend fun getPostByHeadline(headline: String): Post? {
//        return try {
//            val result = db.collection("posts")
//                .whereEqualTo("headline", headline)
//                .get().await()
//            result.documents.firstOrNull()?.toObject(Post::class.java)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    suspend fun addPost(post: Post) {
//        try {
//            db.collection("posts").add(post).await()
//            postDao.insertPosts(post)
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//}
package com.example.neighborhub.repository

import android.content.Context
import com.example.neighborhub.model.Post
import com.example.neighborhub.model.data.AppDatabase
import com.example.neighborhub.model.data.PostDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
class PostRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val postDao: PostDao = AppDatabase.getInstance(context).postDao()

    suspend fun getAllPosts(): List<Post> {
        return try {
            val result = db.collection("posts").get().await()
            result.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPostsByUserId(userId: String) = postDao.getPostsByUserId(userId)

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
        withContext(Dispatchers.IO) {
            try {
                db.collection("posts").add(post).await()
                postDao.insertPosts(post)
            } catch (e: Exception) {
                throw e
            }
        }
    }
}