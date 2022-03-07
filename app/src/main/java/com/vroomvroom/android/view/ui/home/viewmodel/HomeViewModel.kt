package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemEntity
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val roomRepository: RoomRepository,
): ViewModel() {

    private val _merchant by lazy { MutableLiveData<ViewState<MerchantQuery.Data>>() }
    val merchant: LiveData<ViewState<MerchantQuery.Data>>
        get() = _merchant

    private val _favorite by lazy { MutableLiveData<ViewState<FavoriteMutation.Data>>() }
    val favorite: LiveData<ViewState<FavoriteMutation.Data>>
        get() = _favorite

    val cartItem = roomRepository.getAllCartItem()
    lateinit var currentMerchantId: String
    var optionMap: MutableMap<String, CartItemChoiceEntity> = mutableMapOf()
    val isCartCardViewVisible by lazy { MutableLiveData(false) }

    fun queryMerchant(merchantId: String){
        _merchant.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryMerchant(merchantId)
            response?.let { data ->
                withContext(Dispatchers.Main) {
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
    }

    fun favorite(merchantId: String, direction: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.mutationFavorite(merchantId, direction)
            response?.let { data ->
                withContext(Dispatchers.Main) {
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
    }

    fun insertCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = roomRepository.insertCartItem(cartItemEntity)
            if (optionMap.isNotEmpty()) {
                optionMap.forEach { (_, value) ->
                    val cartItemChoice = CartItemChoiceEntity(
                        name = value.name,
                        additionalPrice = value.additionalPrice,
                        optionType = value.optionType,
                        cartItemId = id.toInt()
                    )
                    roomRepository.insertCartItemChoice(cartItemChoice)
                }
            }
        }
    }

    fun updateCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.updateCartItem(cartItemEntity)
        }

    }

    fun deleteCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteCartItem(cartItemEntity)
        }
    }

    fun deleteAllCartItem() {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteAllCartItem()
            deleteAllCartItemChoice()
        }
    }
    fun deleteAllCartItemChoice() {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteAllCartItemChoice()
        }
    }
}