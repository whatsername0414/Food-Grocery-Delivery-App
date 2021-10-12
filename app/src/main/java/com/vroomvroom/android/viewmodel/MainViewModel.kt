package com.vroomvroom.android.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.db.CartItem
import com.vroomvroom.android.db.CartItemChoice
import com.vroomvroom.android.repository.UserPreferences
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
class MainViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val roomRepository: RoomRepository,
    private val preferences: UserPreferences
): ViewModel() {
    private val _homeData by lazy { MutableLiveData<ViewState<Response<HomeDataQuery.Data>>>() }
    private val _merchant by lazy { MutableLiveData<ViewState<Response<MerchantQuery.Data>>>() }

    val cartItem = roomRepository.getAllCartItem()
    val currentLocation by lazy { MutableLiveData<Location>() }
    var isLocationBottomSheetActive = MutableLiveData(false)
    var optionMap: MutableMap<String, CartItemChoice> = mutableMapOf()
    var currentMerchant: String? = null

    val homeData: LiveData<ViewState<Response<HomeDataQuery.Data>>>
        get() = _homeData
    val merchant: LiveData<ViewState<Response<MerchantQuery.Data>>>
        get() = _merchant

    fun queryHomeData() = viewModelScope.launch {
        _homeData.postValue(ViewState.Loading())
        try {
            val response = graphQLRepository.queryHomeData()
            _homeData.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            _homeData.postValue(ViewState.Error("Error fetching restaurant"))
        }
    }

    fun queryMerchantByCategory(category: String) = viewModelScope.launch {
        _homeData.postValue(ViewState.Loading())
        try {
            val response = graphQLRepository.queryMerchantByCategory(category)
            _homeData.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            _homeData.postValue(ViewState.Error("Error fetching restaurant"))
        }
    }

    fun queryMerchant(merchantId: String) = viewModelScope.launch {
        _merchant.postValue(ViewState.Loading())
        try {
            val response = graphQLRepository.queryMerchant(merchantId)
            _merchant.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            _merchant.postValue(ViewState.Error("Error fetching merchant"))
        }
    }

    fun insertCartItem(cartItem: CartItem) = viewModelScope.launch {
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

    fun saveLocation(newLocation: String) = viewModelScope.launch(Dispatchers.IO) {
        preferences.saveLocation(newLocation)
    }

    val location = preferences.location.asLiveData()
}