package com.example.neighborhub.model.data



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.neighborhub.model.Image

@Dao
interface ImageDao {
    @Insert
    fun insertAll(appImageUrls: List<Image>)

    @Query("SELECT * FROM Image_urls")
    fun getAll(): List<Image>

    @Query("DELETE FROM Image_urls")
    fun clearTable()

    @Query("SELECT * FROM Image_urls WHERE `key` = :imageKey LIMIT 1")
    fun getImageByKey(imageKey: String): Image?

}