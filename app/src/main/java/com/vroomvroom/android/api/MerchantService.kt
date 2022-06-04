package com.vroomvroom.android.api

import com.vroomvroom.android.domain.model.BaseResponse
import com.vroomvroom.android.domain.model.BaseResponseList
import com.vroomvroom.android.domain.model.merchant.Category
import com.vroomvroom.android.domain.model.merchant.MerchantDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MerchantService {

    @GET("categories")
    suspend fun getCategories(
        @Query("type") type: String,
    ): Response<BaseResponseList<Category>>

    @GET("merchants")
    suspend fun getMerchants(
        @Query("category") category: String?,
        @Query("searchTerm") searchTerm: String?
    ): Response<BaseResponseList<MerchantDto>>

    @GET("merchants/{id}")
    suspend fun getMerchant(
        @Path("id") id: String
    ): Response<BaseResponse<MerchantDto>>
}