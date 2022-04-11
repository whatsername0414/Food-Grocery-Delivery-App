package com.vroomvroom.android.repository.remote

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.domain.model.merchant.MerchantsMapper
import com.vroomvroom.android.domain.model.order.OrderResponse
import com.vroomvroom.android.domain.model.order.OrderResponseMapper
import com.vroomvroom.android.domain.model.order.OrdersResponseMapper
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.type.LocationInput
import com.vroomvroom.android.type.OrderInput
import com.vroomvroom.android.type.ReviewInput
import com.vroomvroom.android.view.state.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GraphQLRepositoryImpl @Inject constructor(
    private val merchantsMapper: MerchantsMapper,
    private val ordersResponseMapper: OrdersResponseMapper,
    private val orderResponseMapper: OrderResponseMapper,
    private val graphQLServices: ApolloClient
) : BaseRepository(), GraphQLRepository {

    override suspend fun queryCategory(type: String): ViewState<CategoryQuery.Data>? {
        var result: ViewState<CategoryQuery.Data>? = null
        try {
            val response = graphQLServices.query(CategoryQuery(type)).await()
            response.data?.let { data -> result = handleSuccess(data) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryMerchants(query: String, filter: String?): ViewState<Merchants>? {
        var result: ViewState<Merchants>? = null
        try {
            val response = graphQLServices.query(MerchantsQuery(query, filter.toInput())).await()
            response.data?.let { data ->
                result = handleSuccess(
                    withContext(Dispatchers.Default) { merchantsMapper.mapToDomainModel(data) })
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

    override suspend fun mutationReview(reviewInput: ReviewInput): ViewState<ReviewMutation.Data>? {
        var result: ViewState<ReviewMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(ReviewMutation(reviewInput)).await()
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

    override suspend fun queryOrdersByStatus(status: String): ViewState<List<OrderResponse?>>? {
        var result: ViewState<List<OrderResponse?>>? = null
        try {
            val response = graphQLServices.query(OrdersByStatusQuery(status)).await()
            response.data?.let { data -> result = handleSuccess(ordersResponseMapper.mapToDomainModel(data)) }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }

    override suspend fun queryOrder(orderId: String): ViewState<OrderResponse>? {
        var result: ViewState<OrderResponse>? = null
        try {
            val response = graphQLServices.query(OrderQuery(orderId)).await()
            response.data?.let {
                    data -> result = handleSuccess(orderResponseMapper.mapToDomainModel(data)) }
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

    override suspend fun mutationUpdateDeliveryAddress(
        orderId: String,
        locationInput: LocationInput
    ): ViewState<UpdateDeliveryAddressMutation.Data>? {
        var result: ViewState<UpdateDeliveryAddressMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(UpdateDeliveryAddressMutation(orderId, locationInput)).await()
            if (response.hasErrors()) {
                return handleException(GENERAL_ERROR_CODE)
            }
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

    override suspend fun mutationCancelOrder(
        orderId: String,
        reason: String
    ): ViewState<CancelOrderMutation.Data>? {
        var result: ViewState<CancelOrderMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(CancelOrderMutation(orderId, reason)).await()
            if (response.hasErrors()) {
                return handleException(GENERAL_ERROR_CODE)
            }
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

    override suspend fun mutationUpdateName(name: String): ViewState<UpdateNameMutation.Data>? {
        var result: ViewState<UpdateNameMutation.Data>? = null
        try {
            val response = graphQLServices.mutate(UpdateNameMutation(name)).await()
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
            if (response.hasErrors()) return handleException(400)
            Log.d("GraphQLRepositoryImpl", response.errors?.first()?.message.toString())
            response.data?.let { data ->
                result = handleSuccess(data)
            }
        } catch (ae: ApolloException) {
            Log.e("GraphQLRepositoryImpl", "Error: ${ae.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return result
    }
}