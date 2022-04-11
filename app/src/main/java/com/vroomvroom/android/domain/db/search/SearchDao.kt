package com.vroomvroom.android.domain.db.search

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(searchEntity: SearchEntity)
    @Delete
    suspend fun deleteSearch(searchEntity: SearchEntity)
    @Query("SELECT * FROM search_table ORDER BY createdAt DESC")
    suspend fun getAllSearch(): List<SearchEntity>
}