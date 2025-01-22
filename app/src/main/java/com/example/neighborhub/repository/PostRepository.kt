package com.example.neighborhub.repository

import android.util.Log
import com.example.neighborhub.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllPosts(): List<Post> {
        return try {
            val result = db.collection("posts").get().await()
            result.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPostByHeadline(headline: String): Post? {
        return try {
            val result = db.collection("posts")
                .whereEqualTo("headline", headline)
                .get().await()
            result.documents.firstOrNull()?.toObject(Post::class.java)
        } catch (e: Exception) {
            // Log the error for debugging
            Log.e("PostRepository", "Error fetching post: ${e.message}")
            null
        }
    }
}
