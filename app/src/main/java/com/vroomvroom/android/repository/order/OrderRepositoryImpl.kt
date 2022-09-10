package com.vroomvroom.android.repository.order

import android.util.Log
import com.vroomvroom.android.data.api.OrderService
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.order.*
import com.vroomvroom.android.data.model.order.OrderMapper.mapToOrder
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.view.resource.Resource
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val service: OrderService
) : OrderRepository, BaseRepository() {
    override suspend fun getOrders(status: Status): Resource<List<OrderDto>>? {
        var data: Resource<List<OrderDto>>? = null
        try {
            val result = service.getOrders(status = status.ordinal)
            result.body()?.let {
                data = handleSuccess(it.data)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun getOrder(id: String): Resource<OrderDto>? {
        var data: Resource<OrderDto>? = null
        try {
            val result = service.getOrder(id)
            result.body()?.let {
                data = handleSuccess(it.data)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun createOrders(
        merchantId: String,
        payment: Payment,
        deliveryFee: Double,
        totalPrice: Double,
        locationEntity: LocationEntity,
        cartItems: List<CartItemWithOptions>
    ): Resource<String>? {
        var data: Resource<String>? = null
        try {
            val result = service.createOrder(mapToOrder(merchantId, payment, deliveryFee,
                totalPrice, locationEntity, cartItems))
            if (result.isSuccessful) {
                result.body()?.data?.let {
                    data = handleSuccess(it["orderId"].orEmpty())
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun cancelOrder(id: String, reason: String): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf("reason" to reason)
            val result = service.cancelOrder(id, body)
            if (result.isSuccessful && result.code() == 200) {
                data = handleSuccess(true)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun updateOrderAddress(
        id: String,
        location: LocationEntity
    ): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val deliveryAddress = DeliveryAddress(location.address, location.city,
                location.addInfo, listOf(location.latitude, location.longitude))
            val result = service.updateOrderAddress(id, deliveryAddress)
            if (result.isSuccessful && result.code() == 201) {
                data = handleSuccess(true)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun createReview(
        id: String,
        merchantId: String,
        rate: Int,
        comment: String
    ): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf(
                "merchantId" to merchantId,
                "rate" to rate.toString(),
                "comment" to comment
            )
            val result = service.createReview(id, body)
            if (result.isSuccessful && result.code() == 201) {
                data = handleSuccess(true)
            } else {
                return handleException(result.code(), result.errorBody())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }


    companion object {
        const val TAG = "OrderRepositoryImpl"
    }
}