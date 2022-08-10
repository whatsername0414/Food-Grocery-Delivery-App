package com.vroomvroom.android.data.api

import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.order.DeliveryAddress
import com.vroomvroom.android.data.model.order.Order
import com.vroomvroom.android.data.model.order.OrderDto
import retrofit2.Response
import retrofit2.http.*

interface OrderService {

    @GET("orders")
    suspend fun getOrders(
        @Query("type") type: String = "customer",
        @Query("status") status: Int
    ): Response<BaseResponse<List<OrderDto>>>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

    @POST("orders")
    suspend fun createOrder(
        @Body body: Order
    ): Response<BaseResponse<Map<String, String>>>

    @PATCH("orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<BaseResponse<Map<String, String>>>

    @PATCH("orders/{id}/update-address")
    suspend fun updateOrderAddress(
        @Path("id") id: String,
        @Body body: DeliveryAddress
    ): Response<BaseResponse<Map<String, String>>>

    @PUT("orders/{id}/review")
    suspend fun createReview(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<BaseResponse<Map<String, String>>>

}