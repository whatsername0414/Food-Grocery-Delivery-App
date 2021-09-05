package com.vroomvroom.android.repository

import com.apollographql.apollo.api.Response
import com.vroomvroom.android.HomeDataQuery

interface DataRepository {

    suspend fun queryHomeData(): Response<HomeDataQuery.Data>

}