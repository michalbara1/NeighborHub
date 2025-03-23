package com.example.neighborhub.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EmojiService {
    @GET("all")
    suspend fun getAllEmojis(): List<Emoji>

    @GET("category/{category}")
    suspend fun getEmojisByCategory(@Path("category") category: String): List<Emoji>

    @GET("search")
    suspend fun searchEmojis(@Query("q") query: String): List<Emoji>
}
