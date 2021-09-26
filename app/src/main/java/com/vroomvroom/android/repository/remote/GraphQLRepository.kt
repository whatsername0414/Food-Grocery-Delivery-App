package com.vroomvroom.android.repository.remote

import com.apollographql.apollo.api.Response
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.repository.UserPreferences

interface GraphQLRepository {

    suspend fun queryHomeData(): Response<HomeDataQuery.Data>

    suspend fun queryMerchantByCategory(category: String): Response<HomeDataQuery.Data>

    suspend fun mutationLogin(email:String, password:String): Response<LoginMutation.Data>

    suspend fun mutationRegister(email:String, password:String, confirmPassword:String): Response<RegisterMutation.Data>
}