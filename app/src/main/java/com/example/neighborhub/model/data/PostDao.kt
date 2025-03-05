package com.example.neighborhub.model.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neighborhub.model.Post
import com.example.neighborhub.model.User


@Dao
interface PostDao {

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

    @Delete
    suspend fun delete(post: Post)
}