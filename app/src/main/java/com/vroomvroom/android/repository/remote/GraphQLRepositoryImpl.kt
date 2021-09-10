package com.vroomvroom.android.repository.remote

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.networking.GraphQLApi
import com.vroomvroom.android.repository.UserPreferences
import javax.inject.Inject

class GraphQLRepositoryImpl @Inject constructor(
    private val graphQLServices: GraphQLApi,
    private val preferences: UserPreferences
) : GraphQLRepository {
    override suspend fun queryHomeData(): Response<HomeDataQuery.Data> {
        return graphQLServices.getApolloClient().query(HomeDataQuery(category = "")).await()
    }

    override suspend fun queryRestaurantByCategory(category: String): Response<HomeDataQuery.Data> {
        return graphQLServices.getApolloClient().query(HomeDataQuery(category = category)).await()
    }

    override suspend fun mutationLogin(username: String, password: String): Response<LoginMutation.Data> {
        return graphQLServices.getApolloClient().mutate(LoginMutation(username = username, password = password)).await()
    }

    override suspend fun mutationRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Response<RegisterMutation.Data> {
        return graphQLServices.getApolloClient().mutate(RegisterMutation(
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