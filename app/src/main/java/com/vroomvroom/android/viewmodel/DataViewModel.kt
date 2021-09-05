package com.vroomvroom.android.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.HomeDataQuery
import com.vroomvroom.android.repository.DataRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: DataRepository,
): ViewModel() {
    private val _homeData by lazy { MutableLiveData<ViewState<Response<HomeDataQuery.Data>>>() }

    val mainCategories: LiveData<ViewState<Response<HomeDataQuery.Data>>>
        get() = _homeData

    fun queryMainCategories() = viewModelScope.launch {
        _homeData.postValue(ViewState.Loading())
        try {
            val response = repository.queryHomeData()
            _homeData.postValue(ViewState.Success(response))
        } catch (e: ApolloException) {
            Log.d("ApolloException", "Failure", e)
            _homeData.postValue(ViewState.Error("Error fetching categories"))
        }
    }
}