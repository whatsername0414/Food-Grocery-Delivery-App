package com.vroomvroom.android.view.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.CategoryQuery
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.utils.Constants.CASH_ON_DELIVERY
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository
): ViewModel() {

    private val _categories by lazy { MutableLiveData<ViewState<CategoryQuery.Data>>() }
    val categories: LiveData<ViewState<CategoryQuery.Data>>
        get() = _categories

    private val _merchants by lazy { MutableLiveData<ViewState<Merchants>>() }
    val merchants: LiveData<ViewState<Merchants>>
        get() = _merchants

    lateinit var merchant: MerchantQuery.GetMerchant
    val paymentMethod by lazy { MutableLiveData(CASH_ON_DELIVERY) }
    val isHomeScrolled by lazy { MutableLiveData(false) }
    val shouldBackToTop by lazy { MutableLiveData(false) }
    val isRefreshed by lazy { MutableLiveData(false) }
    val reviewed by lazy { MutableLiveData(false) }
    val favoritesChanges = mutableMapOf<Int, Merchant>()
    var isBottomBarVisible = false
    var prevDestination: Int? = null

    fun queryCategory(type: String) {
        _categories.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryCategory(type)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _categories.postValue(data)
                    }
                    is ViewState.Error -> {
                        _categories.postValue(data)
                    }
                    else -> {
                        _categories.postValue(data)
                    }
                }
            }
        }
    }

    fun queryMerchants(query: String, filter: String?) {
        _merchants.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.queryMerchants(query, filter)
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

}