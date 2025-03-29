package com.example.neighborhub.model.data

import androidx.room.*

import com.example.neighborhub.model.User


@Dao
interface UserDao {


    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUser(userId: String): User?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Unit


    @Query("DELETE FROM users WHERE userId = :userId")
    fun deleteCurrentUser(userId: String): Int


    @Query("DELETE FROM users")
    fun deleteAllUsers()
}