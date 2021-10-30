package com.vroomvroom.android.repository.remote

import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.view.state.ViewState

interface GraphQLRepository {

    suspend fun queryHomeData(): ViewState<HomeDataQuery.Data>?

    suspend fun queryMerchantsByCategory(category: String): ViewState<HomeDataQuery.Data>?

    suspend fun queryMerchant(merchantId: String): ViewState<MerchantQuery.Data>?

    suspend fun mutationLogin(email:String, password:String): ViewState<LoginMutation.Data>?

    suspend fun mutationRegister(email:String, password:String, confirmPassword:String): ViewState<RegisterMutation.Data>?
}