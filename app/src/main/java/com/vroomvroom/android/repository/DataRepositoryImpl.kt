package com.vroomvroom.android.repository

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.networking.WebApi
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(
    private val webServices: WebApi,
    private val preferences: UserPreferences
) : DataRepository {
    override suspend fun queryHomeData(): Response<HomeDataQuery.Data> {
        return webServices.getApolloClient().query(HomeDataQuery()).await()
    }

    override suspend fun mutationLogin(username: String, password: String): Response<LoginMutation.Data> {
        return webServices.getApolloClient().mutate(LoginMutation(username = username, password = password)).await()
    }

    override suspend fun mutationRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Response<RegisterMutation.Data> {
        return webServices.getApolloClient().mutate(RegisterMutation(
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword))
            .await()
    }

    override suspend fun saveToken(token: String) {
        preferences.saveToken(token)
    }
}