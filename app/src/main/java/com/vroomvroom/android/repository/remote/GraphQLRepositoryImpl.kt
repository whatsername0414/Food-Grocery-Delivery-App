package com.vroomvroom.android.repository.remote

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.*
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

    override suspend fun mutationRegister(): ViewState<RegisterMutation.Data>? {
        var result: ViewState<RegisterMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(RegisterMutation()).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationVerifyMobileNumber(number: String): ViewState<VerifyMobileNumberMutation.Data>? {
        var result: ViewState<VerifyMobileNumberMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(VerifyMobileNumberMutation(number)).await()
            response.let {
                it.data?.let { data -> result = handleSuccess(data) }
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationOtpVerification(otp: String): ViewState<OtpVerificationMutation.Data>? {
        var result: ViewState<OtpVerificationMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(OtpVerificationMutation(otp)).await()
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