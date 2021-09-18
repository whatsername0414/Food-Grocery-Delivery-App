package com.vroomvroom.android.repository.remote

import android.util.Log
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
        return graphQLServices.getApolloClient().query(HomeDataQuery("")).await()
    }

    override suspend fun queryMerchantByCategory(category: String): Response<HomeDataQuery.Data> {
        return graphQLServices.getApolloClient().query(HomeDataQuery(category = category)).await()
    }

    override suspend fun mutationLogin(email: String, password: String): Response<LoginMutation.Data> {
        return graphQLServices.getApolloClient().mutate(LoginMutation(email = email, password = password)).await()
    }

    override suspend fun mutationRegister(
        email: String,
        password: String,
        confirmPassword: String
    ): Response<RegisterMutation.Data> {
        return graphQLServices.getApolloClient().mutate(RegisterMutation(
            email = email,
            password = password,
            confirmPassword = confirmPassword))
            .await()
    }

    override suspend fun saveToken(token: String) {
        preferences.saveToken(token)
    }

    override val userPreferences = preferences

    override suspend fun saveLocation(newLocation: String) {
        preferences.saveLocation(newLocation)
    }
}