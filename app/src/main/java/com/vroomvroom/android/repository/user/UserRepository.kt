package com.vroomvroom.android.repository.user

import androidx.lifecycle.LiveData
import com.vroomvroom.android.data.model.user.UserEntity
import com.vroomvroom.android.view.state.ViewState

interface UserRepository {

    suspend fun getUser(): ViewState<Boolean>?
    suspend fun updateName(name: String): ViewState<Boolean>?

    //Room
    suspend fun updateUserLocale(userEntity: UserEntity)
    suspend fun updateNameLocale(id: String, name: String)
    suspend fun deleteUserLocale()
    fun getUserLocale(): LiveData<UserEntity>

}