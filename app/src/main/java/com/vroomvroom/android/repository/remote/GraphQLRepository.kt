package com.vroomvroom.android.repository.remote

import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.UserLocationEntity
import com.vroomvroom.android.type.OrderInput
import com.vroomvroom.android.view.state.ViewState

interface GraphQLRepository {

    suspend fun queryCategory(): ViewState<CategoryQuery.Data>?
    suspend fun queryMerchants(category: String): ViewState<MerchantsQuery.Data>?
    suspend fun queryMerchant(merchantId: String): ViewState<MerchantQuery.Data>?

    suspend fun mutationCreateOrder(orderInput: OrderInput): ViewState<CreateOrderMutation.Data>?

    suspend fun queryUser(): ViewState<UserQuery.Data>?
    suspend fun mutationRegister(): ViewState<RegisterMutation.Data>?
    suspend fun mutationUpdateUserLocation(locationEntity: UserLocationEntity): ViewState<UpdateUserLocationMutation.Data>?

    suspend fun mutationVerifyMobileNumber(number:String): ViewState<VerifyMobileNumberMutation.Data>?
    suspend fun mutationOtpVerification(otp: String): ViewState<OtpVerificationMutation.Data>?
}