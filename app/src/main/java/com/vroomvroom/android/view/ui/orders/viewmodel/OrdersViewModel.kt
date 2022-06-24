package com.vroomvroom.android.view.ui.orders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.data.model.order.OrderDto
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.repository.order.OrderRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _isCancelled by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isCancelled: LiveData<ViewState<Boolean>>
        get() = _isCancelled

    private val _isAddressUpdated by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isAddressUpdated: LiveData<ViewState<Boolean>>
        get() = _isAddressUpdated

    private val _isReviewCreated by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isReviewCreated: LiveData<ViewState<Boolean>>
        get() = _isReviewCreated

    private val _orders by lazy { MutableLiveData<ViewState<List<OrderDto?>>>() }
    val orders: LiveData<ViewState<List<OrderDto?>>>
        get() = _orders

    private val _order by lazy { MutableLiveData<ViewState<OrderDto>>() }
    val order: LiveData<ViewState<OrderDto>>
        get() = _order

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

    fun getOrdersByStatus(status: String) {
        _orders.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = orderRepository.getOrders(status)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _orders.postValue(data)
                    }
                    is ViewState.Error -> {
                        _orders.postValue(data)
                    }
                    else -> {
                        _orders.postValue(data)
                    }
                }
            }
        }
    }

    fun getOrder(id: String) {
        _order.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = orderRepository.getOrder(id)
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

    fun updateOrderAddress(
        orderId: String,
        location: LocationEntity
    ) {
        _isAddressUpdated.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = orderRepository.updateOrderAddress(
                orderId,
                location
            )
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isAddressUpdated.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isAddressUpdated.postValue(data)
                    }
                    else -> {
                        _isAddressUpdated.postValue(data)
                    }
                }
            }
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = orderRepository.cancelOrder(orderId, reason)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isCancelled.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isCancelled.postValue(data)
                    }
                    else -> {
                        _isCancelled.postValue(data)
                    }
                }
            }
        }
    }
    fun createReview(orderId: String, merchantId: String, rate: Int, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isReviewCreated.postValue(ViewState.Loading)
            val response = orderRepository.createReview(orderId, merchantId, rate, comment)
            response?.let { data ->
                when(data) {
                    is ViewState.Success -> {
                        _isReviewCreated.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isReviewCreated.postValue(data)
                    }
                    else -> {
                        _isReviewCreated.postValue(data)
                    }
                }
            }
        }
    }
}