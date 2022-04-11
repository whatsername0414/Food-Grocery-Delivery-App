package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val roomRepository: RoomRepository,
): ViewModel() {

    private val _category by lazy { MutableLiveData<ViewState<CategoryQuery.Data>>() }
    val category: LiveData<ViewState<CategoryQuery.Data>>
        get() = _category

    private val _merchants by lazy { MutableLiveData<ViewState<Merchants>>() }
    val merchants: LiveData<ViewState<Merchants>>
        get() = _merchants

    private val _merchant by lazy { MutableLiveData<ViewState<MerchantQuery.Data>>() }
    val merchant: LiveData<ViewState<MerchantQuery.Data>>
        get() = _merchant

    private val _favoriteMerchants by lazy { MutableLiveData<ViewState<Merchants>>() }
    val favoriteMerchants: LiveData<ViewState<Merchants>>
        get() = _favoriteMerchants

    private val _favorite by lazy { MutableLiveData<ViewState<FavoriteMutation.Data>>() }
    val favorite: LiveData<ViewState<FavoriteMutation.Data>>
        get() = _favorite

    val cartItem = roomRepository.getAllCartItem()
    var optionMap: MutableMap<String, CartItemChoiceEntity> = mutableMapOf()
    val isCartCardViewVisible by lazy { MutableLiveData(false) }


    fun queryCategory() {
        _category.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryCategory()
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
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
        _merchants.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchants(category)
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

    fun favoriteMerchant() {
        _favoriteMerchants.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryFavoriteMerchant()
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _favoriteMerchants.postValue(data)
                    }
                    is ViewState.Error -> {
                        _favoriteMerchants.postValue(data)
                    }
                    else -> {
                        _favoriteMerchants.postValue(data)
                    }
                }
            }
        }
    }

    fun favorite(merchantId: String, direction: Int) {
        viewModelScope.launch {
            val response = graphQLRepository.mutationFavorite(merchantId, direction)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _favorite.postValue(data)
                    }
                    is ViewState.Error -> {
                        _favorite.postValue(data)
                    }
                    else -> {
                        _favorite.postValue(data)
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

    fun deleteCartItemChoice(cartItemChoiceEntity: CartItemChoiceEntity) = viewModelScope.launch(
        Dispatchers.IO) {
        roomRepository.deleteCartItemChoice(cartItemChoiceEntity)
    }

    fun deleteAllCartItem() = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteAllCartItem()
        roomRepository.deleteAllCartItemChoice()
    }
}