package com.example.neighborhub.model.data

import androidx.room.*

import com.example.neighborhub.model.User

// Setting up the user Dao
@Dao
interface UserDao {

    // Query to fetch a user by ID
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: String): User?

    // Insert or update a user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Unit

    // Delete current user from Room
    @Query("DELETE FROM users WHERE userId = :userId")
    fun deleteCurrentUser(userId: String): Int

    // Delete all users from Room
    @Query("DELETE FROM users")
    fun deleteAllUsers()
}