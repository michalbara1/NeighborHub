package com.example.neighborhub.repository

import android.util.Log
import com.example.neighborhub.api.Emoji
import com.example.neighborhub.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmojiRepository {
    private val TAG = "EmojiRepository"

    suspend fun getAllEmojis(): Result<List<Emoji>> = withContext(Dispatchers.IO) {
        try {
            val emojis = RetrofitInstance.api.getAllEmojis()
            Log.d(TAG, "Fetched ${emojis.size} emojis")
            Result.success(emojis)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emojis", e)
            Result.failure(e)
        }
    }

    suspend fun getEmojisByCategory(category: String): Result<List<Emoji>> = withContext(Dispatchers.IO) {
        try {
            val emojis = RetrofitInstance.api.getEmojisByCategory(category)
            Result.success(emojis)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emojis by category: $category", e)
            Result.failure(e)
        }
    }

    suspend fun searchEmojis(query: String): Result<List<Emoji>> = withContext(Dispatchers.IO) {
        try {
            val emojis = RetrofitInstance.api.searchEmojis(query)
            Result.success(emojis)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching emojis with query: $query", e)
            Result.failure(e)
        }
    }
}