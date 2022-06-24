package com.vroomvroom.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.data.model.user.UserEntity

@Dao
interface UserDao {
    //User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)
    @Update
    suspend fun updateUser(userEntity: UserEntity)
    @Query("UPDATE user_table SET name = :name WHERE id = :id")
    suspend fun updateUserName(id: String, name: String)
    @Query("DELETE FROM user_table")
    suspend fun deleteUser()
    @Transaction
    @Query("SELECT * FROM user_table")
    fun getUser(): LiveData<UserEntity>

    //UserLocation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(locationEntity: LocationEntity)
    @Update
    suspend fun updateLocation(locationEntity: LocationEntity)
    @Query("UPDATE location_table SET currentUse = :current_use")
    suspend fun updateLocations(current_use: Boolean = false): Int
    @Delete
    suspend fun deleteLocation(locationEntity: LocationEntity)
    @Query("DELETE FROM location_table")
    suspend fun deleteAllAddress()
    @Transaction
    @Query("SELECT * FROM location_table")
    fun getLocation(): LiveData<List<LocationEntity>>

}