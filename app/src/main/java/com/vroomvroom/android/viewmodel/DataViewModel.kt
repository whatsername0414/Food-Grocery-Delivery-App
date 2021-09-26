package com.vroomvroom.android.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.repository.UserPreferences
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: GraphQLRepository,
    private val preferences: UserPreferences
): ViewModel() {
    val currentLocation: MutableLiveData<Location> by lazy { MutableLiveData<Location>() }
    private val _homeData by lazy { MutableLiveData<ViewState<Response<HomeDataQuery.Data>>>() }
    private val tag: String = "DataViewModel"

    val homeData: LiveData<ViewState<Response<HomeDataQuery.Data>>>
        get() = _homeData

    fun queryHomeData() = viewModelScope.launch {
        _homeData.postValue(ViewState.Loading())
        try {
            val response = repository.queryHomeData()
            _homeData.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            Log.e(tag, "ApolloException", e)
            _homeData.postValue(ViewState.Error("Error fetching restaurant"))
        }
    }

    fun queryMerchantByCategory(category: String) = viewModelScope.launch {
        _homeData.postValue(ViewState.Loading())
        try {
            val response = repository.queryMerchantByCategory(category)
            _homeData.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            Log.e(tag, "ApolloException",e)
            _homeData.postValue(ViewState.Error("Error fetching restaurant"))
        }
    }

    fun saveLocation(newLocation: String) = viewModelScope.launch(Dispatchers.IO) {
        preferences.saveLocation(newLocation)
    }

    val location = preferences.location.asLiveData()
}