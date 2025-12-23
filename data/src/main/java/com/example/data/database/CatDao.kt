package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cats")
    fun getCatsFlow(): Flow<List<CatCacheEntity>>

    @Query("SELECT * FROM cats WHERE id = :catId")
    fun getCatById(catId: String): Flow<CatCacheEntity?> // Nullable, если кота нет в кэше

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCats(cats: List<CatCacheEntity>)

    @Query("DELETE FROM cats")
    suspend fun clearCats()

    @Transaction
    suspend fun clearAndInsert(cats: List<CatCacheEntity>) {
        clearCats()
        insertCats(cats)
    }
}