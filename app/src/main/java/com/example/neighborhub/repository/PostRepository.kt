package com.example.neighborhub.repository

import com.example.neighborhub.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await  // Import for `await()` extension

class PostRepository(private val firestore: FirebaseFirestore) {

    suspend fun getPosts(): List<Post> {
        val postsList = mutableListOf<Post>()

        try {
            val snapshot = firestore.collection("posts").get().await()  // Fetch all posts
            for (document in snapshot.documents) {
                val post = document.toObject(Post::class.java)
                post?.let { postsList.add(it) }  // Safe call, adds post if not null
            }
        } catch (e: Exception) {
            throw Exception("Error fetching posts: ${e.message}")
        }

        return postsList
    }
}

