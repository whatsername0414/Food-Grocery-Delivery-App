package com.vroomvroom.android.repository.remote

import com.vroomvroom.android.*
import com.vroomvroom.android.view.state.ViewState

interface GraphQLRepository {

    suspend fun queryHomeData(): ViewState<HomeDataQuery.Data>?

    suspend fun queryMerchantsByCategory(category: String): ViewState<HomeDataQuery.Data>?

    suspend fun queryMerchant(merchantId: String): ViewState<MerchantQuery.Data>?

    suspend fun mutationRegister(): ViewState<RegisterMutation.Data>?

    suspend fun mutationVerifyMobileNumber(number:String): ViewState<VerifyMobileNumberMutation.Data>?

    suspend fun mutationOtpVerification(otp: String): ViewState<OtpVerificationMutation.Data>?
}