package com.vroomvroom.android.view.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.data.model.merchant.Category
import com.vroomvroom.android.data.model.merchant.Merchant
import com.vroomvroom.android.repository.merchant.MerchantRepository
import com.vroomvroom.android.repository.user.UserRepository
import com.vroomvroom.android.utils.Constants.CASH_ON_DELIVERY
import com.vroomvroom.android.view.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    userRepository: UserRepository
): ViewModel() {

    private val _categories by lazy { MutableLiveData<Resource<List<Category>>>() }
    val categories: LiveData<Resource<List<Category>>>
        get() = _categories

    private val _merchants by lazy { MutableLiveData<Resource<List<Merchant>>>() }
    val merchants: LiveData<Resource<List<Merchant>>>
        get() = _merchants

    private val _favorites by lazy { MutableLiveData<Resource<List<Merchant>>>() }
    val favorites: LiveData<Resource<List<Merchant>>>
        get() = _favorites

    lateinit var merchant: Merchant
    val paymentMethod by lazy { MutableLiveData(CASH_ON_DELIVERY) }
    val isHomeScrolled by lazy { MutableLiveData(false) }
    val shouldBackToTop by lazy { MutableLiveData(false) }
    val isRefreshed by lazy { MutableLiveData(false) }
    val reviewed by lazy { MutableLiveData(false) }
    val user = userRepository.getUserLocale()
    var shouldFetchMerchants = false
    var isBottomNavViewVisible = false
    var prevDestination: Int? = null

    fun getCategories(type: String) {
        _categories.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getCategories(type)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _categories.postValue(data)
                    }
                    is Resource.Error -> {
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
        _merchants.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getMerchants(category, searchTerm)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _merchants.postValue(data)
                    }
                    is Resource.Error -> {
                        _merchants.postValue(data)
                    }
                    else -> {
                        _merchants.postValue(data)
                    }
                }
            }
        }
    }

    fun getFavorites() {
        _favorites.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = merchantRepository.getFavorites()
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _favorites.postValue(data)
                    }
                    is Resource.Error -> {
                        _favorites.postValue(data)
                    }
                    else -> {
                        _favorites.postValue(data)
                    }
                }
            }
        }
    }

}