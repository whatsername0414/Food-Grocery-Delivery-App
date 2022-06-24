package com.vroomvroom.android.repository.order

import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.order.Order
import com.vroomvroom.android.data.model.order.OrderDto
import com.vroomvroom.android.data.model.order.Payment
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.view.state.ViewState

interface OrderRepository {

    suspend fun getOrders(status: String?): ViewState<List<OrderDto>>?
    suspend fun getOrder(id: String): ViewState<OrderDto>?
    suspend fun createOrders(
        merchantId: String,
        payment: Payment,
        deliveryFee: Double,
        totalPrice: Double,
        locationEntity: LocationEntity,
        cartItems: List<CartItemWithOptions>
    ): ViewState<String>?
    suspend fun cancelOrder(id: String, reason: String): ViewState<Boolean>?
    suspend fun updateOrderAddress(
        id: String,
        location: LocationEntity
    ): ViewState<Boolean>?
    suspend fun createReview(
        id: String,
        merchantId: String,
        rate: Int,
        comment: String
    ): ViewState<Boolean>?
}