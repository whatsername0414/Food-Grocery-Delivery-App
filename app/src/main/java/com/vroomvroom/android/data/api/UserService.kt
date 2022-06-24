package com.vroomvroom.android.data.api

import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.user.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface UserService {

    @GET("users/me")
    suspend fun getUser(): Response<BaseResponse<UserDto>>

    @PATCH("users/me/update-name")
    suspend fun updateName(
        @Body body: Map<String, String>
    ): Response<BaseResponse<UserDto>>

}