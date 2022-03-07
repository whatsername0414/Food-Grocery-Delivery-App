package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.CreateOrderMutation
import com.vroomvroom.android.UpdateUserLocationMutation
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.type.OrderInput
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository
) : ViewModel() {

    private val _order by lazy { MutableLiveData<ViewState<CreateOrderMutation.Data>>() }
    val order: LiveData<ViewState<CreateOrderMutation.Data>>
        get() = _order
    val isLocationConfirmed by lazy { MutableLiveData(false) }
    var subtotal = 0.0
    var isComputed = false

    fun mutationCreateOrder(orderInput: OrderInput) {
        _order.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.mutationCreateOrder(orderInput)
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

}