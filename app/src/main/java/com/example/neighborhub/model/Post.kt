package com.example.neighborhub.model

data class Post(
    val id: String = "",
    val headline: String = "No headline",
    val content: String = "No content",
    val userName: String = "Anonymous",
    val userPhotoUrl: String = ""
)
