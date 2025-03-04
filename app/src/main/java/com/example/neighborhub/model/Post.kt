package com.example.neighborhub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "headline") var headline: String = "No headline",
    @ColumnInfo(name = "content") var content: String = "No content",
    @ColumnInfo(name = "userName") var userName: String = "Anonymous",
    @ColumnInfo(name = "userPhotoUrl") var userPhotoUrl: String = "",
    @ColumnInfo(name = "userId") var userId: String = "",
    @ColumnInfo(name = "postId") var postId: String = UUID.randomUUID().toString(),
    var lastUpdated: Long? = null
)
