package com.vroomvroom.android.data.api

import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.BaseResponseList
import com.vroomvroom.android.data.model.merchant.Category
import com.vroomvroom.android.data.model.merchant.MerchantDto
import retrofit2.Response
import retrofit2.http.*

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

    @GET("merchants/favorites")
    suspend fun getFavorites(): Response<BaseResponseList<MerchantDto>>

    @PUT("merchants/{id}/favorite")
    suspend fun updateFavorite(
        @Path("id") id: String
    ): Response<BaseResponse<Map<String, String>>>
}