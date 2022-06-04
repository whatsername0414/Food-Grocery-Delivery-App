package com.vroomvroom.android.view.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.domain.model.merchant.Category
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.repository.merchant.MerchantRepository
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
    private val merchantRepository: MerchantRepository,
    private val graphQLRepository: GraphQLRepository
): ViewModel() {

    private val _categories by lazy { MutableLiveData<ViewState<List<Category>>>() }
    val categories: LiveData<ViewState<List<Category>>>
        get() = _categories

    private val _merchants by lazy { MutableLiveData<ViewState<Merchants>>() }
    val merchants: LiveData<ViewState<Merchants>>
        get() = _merchants

    lateinit var merchant: Merchant
    val paymentMethod by lazy { MutableLiveData(CASH_ON_DELIVERY) }
    val isHomeScrolled by lazy { MutableLiveData(false) }
    val shouldBackToTop by lazy { MutableLiveData(false) }
    val isRefreshed by lazy { MutableLiveData(false) }
    val reviewed by lazy { MutableLiveData(false) }
    val favoritesChanges = mutableMapOf<Int, Merchant>()
    var isBottomBarVisible = false
    var prevDestination: Int? = null

    fun getCategories(type: String) {
        _categories.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getCategories(type)
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

    fun getMerchants(category: String? = null, searchTerm: String? = null) {
        _merchants.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getMerchants(category, searchTerm)
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