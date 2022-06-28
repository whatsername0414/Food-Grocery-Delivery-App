package com.vroomvroom.android.data.api

import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.user.UserDto
import retrofit2.Response
import retrofit2.http.*

interface UserService {

    @GET("users/me")
    suspend fun getUser(): Response<BaseResponse<UserDto>>

    @PATCH("users/me/update-name")
    suspend fun updateName(
        @Body body: Map<String, String>
    ): Response<BaseResponse<UserDto>>

    @POST("users/me/phone-otp")
    suspend fun registerPhoneNumber(
        @Body body: Map<String, String>
    ): Response<BaseResponse<Map<String, String>>>

    @POST("users/me/verify-phone-otp")
    suspend fun verifyOtp(
        @Body body: Map<String, String>
    ): Response<BaseResponse<UserDto>>


}