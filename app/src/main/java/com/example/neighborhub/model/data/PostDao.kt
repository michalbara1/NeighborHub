package com.example.neighborhub.model.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.neighborhub.model.Post
import com.example.neighborhub.model.User

@Dao
interface PostDao {
    // Keep existing queries

    @Query("SELECT * FROM posts")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id =:id")
    fun getPostById(id: String): LiveData<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(vararg posts: Post)

    @Query("SELECT * FROM posts WHERE headline = :headline")
    fun getPostByHeadline(headline: String): Post?

    @Query("SELECT * FROM posts WHERE userId = :userId")
    fun getPostsByUserId(userId: String): LiveData<List<Post>>

    @Query("SELECT * FROM posts")
    suspend fun getAllPostsAsList(): List<Post>

    @Delete
    suspend fun delete(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deleteById(postId: String)

    // Add these new queries for location filtering

    // Get posts with valid location data
    @Query("SELECT * FROM posts WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getPostsWithLocation(): List<Post>

    // Get posts within a bounding box (useful for map display)
    @Query("SELECT * FROM posts WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getPostsInArea(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<Post>
}