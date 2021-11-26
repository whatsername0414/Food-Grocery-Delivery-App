package com.vroomvroom.android.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.CartItemEntity
import com.vroomvroom.android.domain.db.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.UserLocationEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.type.OrderInput
import com.vroomvroom.android.type.OrderProductInput
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val roomRepository: RoomRepository,
): ViewModel() {

    private val _user by lazy { MutableLiveData<ViewState<UpdateUserLocationMutation.Data>>() }
    val user: LiveData<ViewState<UpdateUserLocationMutation.Data>>
        get() = _user

    private val _category by lazy { MutableLiveData<ViewState<CategoryQuery.Data>>() }
    val category: LiveData<ViewState<CategoryQuery.Data>>
        get() = _category

    private val _merchants by lazy { MutableLiveData<ViewState<MerchantsQuery.Data>>() }
    val merchants: LiveData<ViewState<MerchantsQuery.Data>>
        get() = _merchants

    private val _merchantsByCategory by lazy { MutableLiveData<ViewState<MerchantsQuery.Data>>() }
    val merchantByCategory: LiveData<ViewState<MerchantsQuery.Data>>
        get() = _merchantsByCategory

    private val _merchant by lazy { MutableLiveData<ViewState<MerchantQuery.Data>>() }
    val merchant: LiveData<ViewState<MerchantQuery.Data>>
        get() = _merchant

    private val _order by lazy { MutableLiveData<ViewState<CreateOrderMutation.Data>>() }
    val order: LiveData<ViewState<CreateOrderMutation.Data>>
        get() = _order

    val orderProductInput = mutableListOf<OrderProductInput>()
    val isLocationConfirmed by lazy { MutableLiveData(false) }
    val isCartCardViewVisible by lazy { MutableLiveData(false) }
    val paymentMethod by lazy { MutableLiveData("Cash") }
    val cartItem = roomRepository.getAllCartItem()
    var optionMap: MutableMap<String, CartItemChoiceEntity> = mutableMapOf()
    var currentMerchant: MutableMap<String, Merchant> = mutableMapOf()
    var isSubtotalComputed = false
    var subtotal = 0

    fun mutationUpdateUserLocation(locationEntity: UserLocationEntity) {
        _user.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationUpdateUserLocation(locationEntity)
            response?.let { data ->
                when(data) {
                    is ViewState.Success -> {
                        isLocationConfirmed.postValue(true)
                        _user.postValue(data)
                    }
                    is ViewState.Error -> {
                        _user.postValue(data)
                    }
                    else -> {
                        _user.postValue(data)
                    }
                }
            }
        }
    }

    fun queryCategory() {
        _category.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryCategory()
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        Log.d("MainViewModel", "Category Fetch Success")
                        _category.postValue(data)
                    }
                    is ViewState.Error -> {
                        _category.postValue(data)
                    }
                    else -> {
                        _category.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchants() {
        _merchants.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchants("")
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _merchants.postValue(data)
                    }
                    is ViewState.Error -> {
                        _merchants.postValue(data)
                    }
                    else -> {
                        _merchants.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchantsByCategory(category: String){
        _merchantsByCategory.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchants(category)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _merchantsByCategory.postValue(data)
                    }
                    is ViewState.Error -> {
                        _merchantsByCategory.postValue(data)
                    }
                    else -> {
                        _merchantsByCategory.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchant(merchantId: String){
        _merchant.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchant(merchantId)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _merchant.postValue(data)
                    }
                    is ViewState.Error -> {
                        _merchant.postValue(data)
                    }
                    else -> {
                        _merchant.postValue(data)
                    }
                }
            }
        }
    }

    fun createOrder(orderInput: OrderInput) {
        _order.postValue(ViewState.Loading)
        viewModelScope.launch {
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

    fun insertCartItem(cartItemEntity: CartItemEntity) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val roomResponse = roomRepository.insertCartItem(cartItemEntity)
            if (optionMap.isNotEmpty()) {
                optionMap.forEach { (_, value) ->
                    val cartItemChoice = CartItemChoiceEntity(
                        name = value.name,
                        additional_price = value.additional_price,
                        optionType = value.optionType,
                        cartItemId = roomResponse.toInt()
                    )
                    roomRepository.insertCartItemChoice(cartItemChoice)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCartItem(cartItemEntity: CartItemEntity) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.updateCartItem(cartItemEntity)
    }

    fun deleteCartItem(cartItemEntity: CartItemEntity) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteCartItem(cartItemEntity)
    }

    fun deleteCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteCartItemChoice(cartItemChoiceEntity)
    }

    fun deleteAllCartItem() = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteAllCartItem()
    }

    fun deleteAllCartItemChoice() = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteAllCartItemChoice()
    }
}