package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cats")
data class CatCacheEntity(
    @PrimaryKey val id: String,
    val url: String,
    val isLiked: Boolean = false
)

