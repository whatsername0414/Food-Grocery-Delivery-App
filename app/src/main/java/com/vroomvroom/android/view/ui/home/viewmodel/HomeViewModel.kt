package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.data.model.cart.CartItemEntity
import com.vroomvroom.android.data.model.merchant.Merchant
import com.vroomvroom.android.data.model.merchant.Option
import com.vroomvroom.android.repository.cart.CartRepository
import com.vroomvroom.android.repository.merchant.MerchantRepository
import com.vroomvroom.android.view.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val cartRepository: CartRepository,
): ViewModel() {

    private val _merchant by lazy { MutableLiveData<Resource<Merchant>>() }
    val merchant: LiveData<Resource<Merchant>>
        get() = _merchant

    private val _isPutFavoriteSuccessful by lazy { MutableLiveData<Resource<Boolean>>() }
    val isPutFavoriteSuccessful: LiveData<Resource<Boolean>>
        get() = _isPutFavoriteSuccessful

    val cartItem = cartRepository.getAllCartItem()
    lateinit var currentMerchantId: String
    var choseOptions = mutableMapOf<String, Option>()
    val isCartCardViewVisible by lazy { MutableLiveData(false) }

    fun getMerchant(merchantId: String){
        _merchant.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getMerchant(merchantId)
            response?.let { data ->
                withContext(Dispatchers.Main) {
                    when (data) {
                        is Resource.Success -> {
                            _merchant.postValue(data)
                        }
                        is Resource.Error -> {
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

    fun updateFavorite(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.updateFavorite(id)
            response.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _isPutFavoriteSuccessful.postValue(data)
                    }
                    is Resource.Error -> {
                        _isPutFavoriteSuccessful.postValue(data)
                    }
                    else -> {
                        _isPutFavoriteSuccessful.postValue(data)
                    }
                }
            }
        }
    }

    fun insertCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.insertCartItem(cartItemEntity)
            if (choseOptions.isNotEmpty()) {
                cartRepository.insertCartItemOptions(choseOptions)
            }
        }
    }

    fun updateCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.updateCartItem(cartItemEntity)
        }

    }

    fun deleteCartItem(cartItemEntity: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.deleteCartItem(cartItemEntity)
        }
    }

    fun deleteAllCartItem() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.deleteAllCartItem()
            deleteAllCartItemChoice()
        }
    }
    fun deleteAllCartItemChoice() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.deleteAllCartItemOption()
        }
    }
}