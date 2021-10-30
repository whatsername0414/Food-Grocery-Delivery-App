package com.vroomvroom.android.repository.remote

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.view.state.ViewState
import javax.inject.Inject

class GraphQLRepositoryImpl @Inject constructor(
    private val graphQLServices: ApolloClient
) : GraphQLBaseRepository(), GraphQLRepository {
    override suspend fun queryHomeData(): ViewState<HomeDataQuery.Data>? {
        var result: ViewState<HomeDataQuery.Data>? = null
        try {
            val response = graphQLServices.query(HomeDataQuery("")).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryMerchantsByCategory(category: String): ViewState<HomeDataQuery.Data>? {
        var result: ViewState<HomeDataQuery.Data>? = null
        try {
            val response = graphQLServices.query(HomeDataQuery(category)).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryMerchant(merchantId: String): ViewState<MerchantQuery.Data>? {
        var result: ViewState<MerchantQuery.Data>? = null
        try {
            val response = graphQLServices.query(MerchantQuery(merchantId)).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationLogin(email: String, password: String): ViewState<LoginMutation.Data>? {
        var result: ViewState<LoginMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(LoginMutation(email, password)).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationRegister(
        email: String,
        password: String,
        confirmPassword: String
    ): ViewState<RegisterMutation.Data>? {
        var result: ViewState<RegisterMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(RegisterMutation(
                email = email,
                password = password,
                confirmPassword = confirmPassword))
                .await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }
}