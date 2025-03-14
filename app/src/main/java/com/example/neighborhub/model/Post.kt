package com.example.neighborhub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "headline") var headline: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    @ColumnInfo(name = "userName") var userName: String = "",
    @ColumnInfo(name = "userPhotoUrl") var userPhotoUrl: String = "",
    @ColumnInfo(name = "imageUrl") var imageUrl: String? = null,
    @ColumnInfo(name = "userId") var userId: String = "",
    var lastUpdated: Long? = 0
)