package com.vroomvroom.android.repository

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.networking.WebApi
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    private val webServices: WebApi
) : DataRepository {
    override suspend fun queryHomeData(): Response<HomeDataQuery.Data> {
        return webServices.getApolloClient().query(HomeDataQuery()).await()
    }
}