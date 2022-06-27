package com.vroomvroom.android.repository.auth

import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.view.resource.Resource

interface AuthRepository {

    suspend fun register(locationEntity: LocationEntity): Resource<Boolean>?
    suspend fun registerPhoneNumber(number: String): Resource<Boolean>?
    suspend fun verifyOtp(otp: String): Resource<Boolean>?

}