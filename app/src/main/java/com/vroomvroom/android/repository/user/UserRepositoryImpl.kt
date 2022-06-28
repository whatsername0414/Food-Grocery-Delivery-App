package com.vroomvroom.android.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import com.vroomvroom.android.data.api.UserService
import com.vroomvroom.android.data.db.dao.UserDao
import com.vroomvroom.android.data.model.user.UserEntity
import com.vroomvroom.android.data.model.user.UserMapper
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.repository.merchant.MerchantRepositoryImpl
import com.vroomvroom.android.view.resource.Resource
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val service: UserService,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : UserRepository, BaseRepository() {
    override suspend fun getUser(): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val result = service.getUser()
            if (result.isSuccessful && result.code() == 200) {
                result.body()?.data?.let {
                    val user = userMapper.mapToDomainModel(it)
                    userDao.insertUser(user)
                    data = handleSuccess(true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun updateName(name: String): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf("name" to name)
            val result = service.updateName(body)
            if (result.isSuccessful && result.code() == 200) {
                val user = result.body()?.data
                updateNameLocale(user?._id.orEmpty(), user?.name.orEmpty())
                data = handleSuccess(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun generatePhoneOtp(number: String): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf("number" to number)
            val result = service.registerPhoneNumber(body)
            if (result.isSuccessful && result.code() == 201) {
                data = handleSuccess(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun verifyOtp(otp: String): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf("otp" to otp)
            val result = service.verifyOtp(body)
            if (result.isSuccessful && result.code() == 201) {
                result.body()?.data?.let {
                    val user = userMapper.mapToDomainModel(it)
                    userDao.updateUser(user)
                    data = handleSuccess(true)
                }
            } else {
                return handleException(result.code(), result.errorBody())
            }
        } catch (e: Exception) {
            Log.d(MerchantRepositoryImpl.TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    //Room
    override suspend fun updateUserLocale(userEntity: UserEntity) = userDao.updateUser(userEntity)
    override suspend fun updateNameLocale(id: String, name: String) = userDao.updateUserName(id, name)
    override suspend fun deleteUserLocale() = userDao.deleteUser()
    override fun getUserLocale(): LiveData<UserEntity> = userDao.getUser()

    companion object {
        const val TAG = "UserRepositoryImpl"
    }
}