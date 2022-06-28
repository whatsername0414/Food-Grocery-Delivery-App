package com.vroomvroom.android.repository.order

import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.order.OrderDto
import com.vroomvroom.android.data.model.order.Payment
import com.vroomvroom.android.data.model.order.Status
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.view.resource.Resource

interface OrderRepository {

    suspend fun getOrders(status: Status): Resource<List<OrderDto>>?
    suspend fun getOrder(id: String): Resource<OrderDto>?
    suspend fun createOrders(
        merchantId: String,
        payment: Payment,
        deliveryFee: Double,
        totalPrice: Double,
        locationEntity: LocationEntity,
        cartItems: List<CartItemWithOptions>
    ): Resource<String>?
    suspend fun cancelOrder(id: String, reason: String): Resource<Boolean>?
    suspend fun updateOrderAddress(
        id: String,
        location: LocationEntity
    ): Resource<Boolean>?
    suspend fun createReview(
        id: String,
        merchantId: String,
        rate: Int,
        comment: String
    ): Resource<Boolean>?
}