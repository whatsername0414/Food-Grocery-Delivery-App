package com.vroomvroom.android.view.ui.orders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.toInput
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.order.LocationInputMapper
import com.vroomvroom.android.domain.model.order.OrderResponse
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.type.ReviewInput
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class OrdersViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val locationInputMapper: LocationInputMapper
) : ViewModel() {

    private val _orders by lazy { MutableLiveData<ViewState<OrdersQuery.Data>>() }
    val orders: LiveData<ViewState<OrdersQuery.Data>>
        get() = _orders

    private val _cancelled by lazy { MutableLiveData<ViewState<CancelOrderMutation.Data>>() }
    val cancelled: LiveData<ViewState<CancelOrderMutation.Data>>
        get() = _cancelled

    private val _ordersByStatus by lazy { MutableLiveData<ViewState<List<OrderResponse?>>>() }
    val ordersByStatus: LiveData<ViewState<List<OrderResponse?>>>
        get() = _ordersByStatus

    private val _order by lazy { MutableLiveData<ViewState<OrderResponse>>() }
    val order: LiveData<ViewState<OrderResponse>>
        get() = _order

    private val _ordersStatus by lazy { MutableLiveData<ViewState<OrdersStatusQuery.Data>>() }
    val ordersStatus: LiveData<ViewState<OrdersStatusQuery.Data>>
        get() = _ordersStatus

    private val _changeAddress by lazy { MutableLiveData<ViewState<UpdateDeliveryAddressMutation.Data>>() }
    val changeAddress: LiveData<ViewState<UpdateDeliveryAddressMutation.Data>>
        get() = _changeAddress

    private val _review by lazy { MutableLiveData<ViewState<ReviewMutation.Data>>() }
    val review: LiveData<ViewState<ReviewMutation.Data>>
        get() = _review

    lateinit var merchantId: String

//    fun queryOrders() {
//        _orders.postValue(ViewState.Loading)
//        viewModelScope.launch {
//            val response = graphQLRepository.queryOrders()
//            response?.let { data ->
//                when (data) {
//                    is ViewState.Success -> {
//                        _orders.postValue(data)
//                    }
//                    is ViewState.Error -> {
//                        _orders.postValue(data)
//                    }
//                    else -> {
//                        _orders.postValue(data)
//                    }
//                }
//
//            }
//        }
//    }

    fun queryOrdersByStatus(status: String) {
        _ordersByStatus.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryOrdersByStatus(status)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _ordersByStatus.postValue(data)
                    }
                    is ViewState.Error -> {
                        _ordersByStatus.postValue(data)
                    }
                    else -> {
                        _ordersByStatus.postValue(data)
                    }
                }
            }
        }
    }

    fun queryOrder(orderId: String) {
        _order.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryOrder(orderId)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _order.postValue(data)
                    }
                    is ViewState.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun queryOrdersStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryOrdersStatus()
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _ordersStatus.postValue(data)
                    }
                    else -> Unit
                }
            }
        }
    }
    fun mutationUpdateDeliveryAddress(orderId: String, userLocationEntity: UserLocationEntity) {
        _changeAddress.postValue(ViewState.Loading)
        val address = locationInputMapper.mapToDomainModel(userLocationEntity)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.mutationUpdateDeliveryAddress(orderId, address)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _changeAddress.postValue(data)
                    }
                    is ViewState.Error -> {
                        _changeAddress.postValue(data)
                    }
                    else -> {
                        _changeAddress.postValue(data)
                    }
                }
            }
        }
    }
    fun mutationUpdateOrderNotified(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            graphQLRepository.mutationUpdateOrderNotified(orderId)
        }
    }
    fun mutationCancelOrder(orderId: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.mutationCancelOrder(orderId, reason)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _cancelled.postValue(data)
                    }
                    is ViewState.Error -> {
                        _cancelled.postValue(data)
                    }
                    else -> {
                        _cancelled.postValue(data)
                    }
                }
            }
        }
    }
    fun mutationReview(merchantId: String, orderId: String, rate: Int, review: String?) {
        val reviewInput = ReviewInput(merchantId, orderId, rate, review.toInput())
        viewModelScope.launch(Dispatchers.IO) {
            _review.postValue(ViewState.Loading)
            val response = graphQLRepository.mutationReview(reviewInput)
            response?.let { data ->
                when(data) {
                    is ViewState.Success -> {
                        _review.postValue(data)
                    }
                    is ViewState.Error -> {
                        _review.postValue(data)
                    }
                    else -> {
                        _review.postValue(data)
                    }
                }
            }
        }
    }
}