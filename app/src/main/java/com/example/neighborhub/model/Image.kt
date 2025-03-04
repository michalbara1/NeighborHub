package com.example.neighborhub.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Image_urls")
data class Image(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val url: String
)