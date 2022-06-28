package com.vroomvroom.android.repository.auth

import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.view.resource.Resource

interface AuthRepository {

    suspend fun register(locationEntity: LocationEntity): Resource<Boolean>?
    suspend fun generateEmailOtp(emailAddress: String): Resource<Boolean>
    suspend fun verifyEmailOtp(emailAddress: String, otp: String): Resource<Boolean>

}