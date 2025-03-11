package com.example.neighborhub.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
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
        return withContext(Dispatchers.IO) {
            try {
                val result = db.collection("posts").get().await()
                val posts = result.toObjects(Post::class.java)
                postDao.insertPosts(*posts.toTypedArray()) // Sync with local database
                posts  // Return posts from Firestore
            } catch (e: Exception) {
                Log.e("PostRepository", "Failed to fetch posts from Firestore", e)
                postDao.getAllPostsAsList() // Return posts from local database as List
            }
        }
    }

    fun getPostsLiveData(): LiveData<List<Post>> {
        return postDao.getAllPosts()
    }

    suspend fun getPostsByUserId(userId: String): LiveData<List<Post>> {
        return postDao.getPostsByUserId(userId)
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
        withContext(Dispatchers.IO) {
            try {
                db.collection("posts").add(post).await()
                postDao.insertPosts(post)
                Log.d("PostRepository", "Post added to Firestore and local database")
            } catch (e: Exception) {
                Log.e("PostRepository", "Failed to add post", e)
                throw e
            }
        }
    }
}