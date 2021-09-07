package com.vroomvroom.android.repository

import com.apollographql.apollo.api.Response
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.RegisterMutation

interface DataRepository {

    suspend fun queryHomeData(): Response<HomeDataQuery.Data>

    suspend fun mutationLogin(username:String, password:String): Response<LoginMutation.Data>

    suspend fun mutationRegister(username:String, email:String, password:String, confirmPassword:String): Response<RegisterMutation.Data>

    suspend fun saveToken(token: String)
}