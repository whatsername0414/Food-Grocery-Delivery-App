package com.vroomvroom.android.view.ui.orders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.OrderQuery
import com.vroomvroom.android.OrdersByStatusQuery
import com.vroomvroom.android.OrdersQuery
import com.vroomvroom.android.OrdersStatusQuery
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class OrdersViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
) : ViewModel() {

    private val _orders by lazy { MutableLiveData<ViewState<OrdersQuery.Data>>() }
    val orders: LiveData<ViewState<OrdersQuery.Data>>
        get() = _orders

    private val _ordersByStatus by lazy { MutableLiveData<ViewState<OrdersByStatusQuery.Data>>() }
    val ordersByStatus: LiveData<ViewState<OrdersByStatusQuery.Data>>
        get() = _ordersByStatus

    private val _order by lazy { MutableLiveData<ViewState<OrderQuery.Data>>() }
    val order: LiveData<ViewState<OrderQuery.Data>>
        get() = _order

    private val _ordersStatus by lazy { MutableLiveData<ViewState<OrdersStatusQuery.Data>>() }
    val ordersStatus: LiveData<ViewState<OrdersStatusQuery.Data>>
        get() = _ordersStatus

    fun queryOrders() {
        _orders.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryOrders()
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

    fun queryOrdersByStatus(status: String) {
        _ordersByStatus.postValue(ViewState.Loading)
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
    fun mutationUpdateOrderStatus(orderId: String) {
        viewModelScope.launch {
            graphQLRepository.mutationUpdateOrderNotified(orderId)
        }
    }
}