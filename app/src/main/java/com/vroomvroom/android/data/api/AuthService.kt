package com.vroomvroom.android.data.api

import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.data.model.user.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("users")
    suspend fun register(
        @Body body: LocationEntity
    ): Response<BaseResponse<UserDto>>

    @POST("users/me/register-phone-number")
    suspend fun registerPhoneNumber(
        @Body body: Map<String, String>
    ): Response<BaseResponse<Map<String, String>>>

    @POST("users/me/verify-otp")
    suspend fun verifyOtp(
        @Body body: Map<String, String>
    ): Response<BaseResponse<UserDto>>

}