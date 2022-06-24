package com.vroomvroom.android.repository.auth

import android.util.Log
import com.vroomvroom.android.data.api.AuthService
import com.vroomvroom.android.data.db.dao.UserDao
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.data.model.user.UserMapper
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.repository.merchant.MerchantRepositoryImpl
import com.vroomvroom.android.repository.user.UserRepositoryImpl
import com.vroomvroom.android.view.state.ViewState
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: AuthService,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : AuthRepository, BaseRepository() {
    override suspend fun register(locationEntity: LocationEntity): ViewState<Boolean>? {
        var data: ViewState<Boolean>? = null
        try {
            val result = service.register(locationEntity)
            if (result.isSuccessful && result.code() == 200) {
                result.body()?.data?.let {
                    val user = userMapper.mapToDomainModel(it)
                    userDao.insertUser(user)
                    data = handleSuccess(true)
                }
            } else {
                handleSuccess(false)
            }
        } catch (e: Exception) {
            Log.d(MerchantRepositoryImpl.TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun registerPhoneNumber(number: String): ViewState<Boolean>? {
        var data: ViewState<Boolean>? = null
        try {
            val body = mapOf("number" to number)
            val result = service.registerPhoneNumber(body)
            if (result.isSuccessful && result.code() == 201) {
                data = handleSuccess(true)
            }
        } catch (e: Exception) {
            Log.e(UserRepositoryImpl.TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun verifyOtp(otp: String): ViewState<Boolean>? {
        var data: ViewState<Boolean>? = null
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


}