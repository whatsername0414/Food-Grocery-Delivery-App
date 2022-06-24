package com.vroomvroom.android.data.db.dao

import androidx.room.*
import com.vroomvroom.android.data.model.search.SearchEntity

@Dao
interface SearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(searchEntity: SearchEntity)
    @Delete
    suspend fun deleteSearch(searchEntity: SearchEntity)
    @Query("SELECT * FROM search_table ORDER BY createdAt DESC")
    suspend fun getAllSearch(): List<SearchEntity>
}