package com.vroomvroom.android.viewmodel

import android.content.Intent
import android.location.Address
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.model.MerchantModel
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.utils.IdTokenCallback
import com.vroomvroom.android.utils.TaskListener
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
    private val preferences: UserPreferences
): ViewModel() {
    private val _homeData by lazy { MutableLiveData<ViewState<HomeDataQuery.Data>>() }
    private val _merchant by lazy { MutableLiveData<ViewState<MerchantQuery.Data>>() }

    val currentLocation by lazy { MutableLiveData<Location>() }
    val address by lazy { MutableLiveData<Address>() }
    val paymentMethod by lazy { MutableLiveData("Cash") }
    var optionMap: MutableMap<String, CartItemChoice> = mutableMapOf()
    var currentMerchant: MutableMap<String, MerchantModel> = mutableMapOf()
    val cartItem = roomRepository.getAllCartItem()

    val homeData: LiveData<ViewState<HomeDataQuery.Data>>
        get() = _homeData
    val merchant: LiveData<ViewState<MerchantQuery.Data>>
        get() = _merchant

    fun queryHomeData() {
        _homeData.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryHomeData()
            response.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _homeData.postValue(data)
                    }
                    is ViewState.Error -> {
                        _homeData.postValue(data)
                    }
                    else -> {
                        _homeData.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchantsByCategory(category: String){
        _homeData.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchantsByCategory(category)
            response.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _homeData.postValue(data)
                    }
                    is ViewState.Error -> {
                        _homeData.postValue(data)
                    }
                    else -> {
                        _homeData.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchant(merchantId: String){
        _merchant.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.queryMerchant(merchantId)
            response.let { data ->
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

    fun insertCartItem(cartItem: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val roomResponse = roomRepository.insertCartItem(cartItem)
            if (optionMap.isNotEmpty()) {
                optionMap.forEach { (_, value) ->
                    val cartItemChoice = CartItemChoice(
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

    fun updateCartItem(cartItem: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.updateCartItem(cartItem)
    }

    fun deleteCartItem(cartItem: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteCartItem(cartItem)
    }

    fun deleteCartItemChoice(cartItemChoice: CartItemChoice) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.deleteCartItemChoice(cartItemChoice)
    }

    fun saveLocation(newLocation: String) = viewModelScope.launch(Dispatchers.IO) {
        preferences.saveLocation(newLocation)
    }

    val location = preferences.location.asLiveData()
}