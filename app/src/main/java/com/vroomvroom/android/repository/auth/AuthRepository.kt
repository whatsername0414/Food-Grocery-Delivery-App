package com.vroomvroom.android.repository.auth

import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.data.model.user.UserEntity
import com.vroomvroom.android.view.state.ViewState

interface AuthRepository {

    suspend fun register(locationEntity: LocationEntity): ViewState<Boolean>?
    suspend fun registerPhoneNumber(number: String): ViewState<Boolean>?
    suspend fun verifyOtp(otp: String): ViewState<Boolean>?

}