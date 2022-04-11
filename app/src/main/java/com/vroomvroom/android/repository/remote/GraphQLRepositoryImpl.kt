package com.vroomvroom.android.repository.remote

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.merchant.FavoriteMerchantsMapper
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.domain.model.merchant.MerchantsMapper
import com.vroomvroom.android.type.LocationInput
import com.vroomvroom.android.type.OrderInput
import com.vroomvroom.android.view.state.ViewState
import javax.inject.Inject

class GraphQLRepositoryImpl @Inject constructor(
    private val favoriteMerchantsMapper: FavoriteMerchantsMapper,
    private val merchantsMapper: MerchantsMapper,
    private val graphQLServices: ApolloClient
) : GraphQLBaseRepository(), GraphQLRepository {

    override suspend fun queryCategory(): ViewState<CategoryQuery.Data>? {
        var result: ViewState<CategoryQuery.Data>? = null
        try {
            val response = graphQLServices.query(CategoryQuery()).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryMerchants(category: String): ViewState<Merchants>? {
        var result: ViewState<Merchants>? = null
        try {
            val response = graphQLServices.query(MerchantsQuery(category)).await()
            response.data?.let { data ->
                val mapped = merchantsMapper.mapToDomainModel(data)
                result = handleSuccess(mapped)
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
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryFavoriteMerchant(): ViewState<Merchants>? {
        var result: ViewState<Merchants>? = null
        try {
            val response = graphQLServices.query(FavoriteMerchantQuery()).await()
            response.data?.let { data ->
                val mapped = favoriteMerchantsMapper.mapToDomainModel(data)
                result = handleSuccess(mapped)
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationFavorite(
        merchantId: String,
        direction: Int
    ): ViewState<FavoriteMutation.Data>? {
        var result: ViewState<FavoriteMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(FavoriteMutation(merchantId, direction)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryOrders(): ViewState<OrdersQuery.Data>? {
        var result: ViewState<OrdersQuery.Data>? = null
        try {
            val response = graphQLServices.query(OrdersQuery()).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryOrdersByStatus(status: String): ViewState<OrdersByStatusQuery.Data>? {
        var result: ViewState<OrdersByStatusQuery.Data>? = null
        try {
            val response = graphQLServices.query(OrdersByStatusQuery(status)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryOrder(orderId: String): ViewState<OrderQuery.Data>? {
        var result: ViewState<OrderQuery.Data>? = null
        try {
            val response = graphQLServices.query(OrderQuery(orderId)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryOrdersStatus(): ViewState<OrdersStatusQuery.Data>? {
        var result: ViewState<OrdersStatusQuery.Data>? = null
        try {
            val response = graphQLServices.query(OrdersStatusQuery()).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationCreateOrder(orderInput: OrderInput): ViewState<CreateOrderMutation.Data>? {
        var result: ViewState<CreateOrderMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(CreateOrderMutation(orderInput)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationUpdateOrderNotified(orderId: String): ViewState<UpdateOrderNotifiedMutation.Data>? {
        var result: ViewState<UpdateOrderNotifiedMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(UpdateOrderNotifiedMutation(orderId)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryUser(): ViewState<UserQuery.Data>? {
        var result: ViewState<UserQuery.Data>? = null
        try {
            val response = graphQLServices.query(UserQuery()).await()
            response.data?.let { data -> result = handleSuccess(data) }
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
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun mutationUpdateUserLocation(locationEntity: UserLocationEntity): ViewState<UpdateUserLocationMutation.Data>? {
        var result: ViewState<UpdateUserLocationMutation.Data>? = null
        val locationInput = LocationInput(
            address = locationEntity.address.toString().toInput(),
            city = locationEntity.city.toString().toInput(),
            additional_information = locationEntity.addInfo.toString().toInput(),
            coordinates = arrayListOf(locationEntity.latitude, locationEntity.longitude)
        )
        try {
            val response = graphQLServices.mutate(UpdateUserLocationMutation(locationInput)).await()
            response.data?.let { data -> result = handleSuccess(data) }
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
            response.data?.let { data -> result = handleSuccess(data) }
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
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }
}