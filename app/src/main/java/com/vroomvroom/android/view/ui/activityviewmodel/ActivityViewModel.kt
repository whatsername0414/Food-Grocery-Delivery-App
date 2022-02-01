package com.vroomvroom.android.view.ui.activityviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vroomvroom.android.MerchantQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ActivityViewModel @Inject constructor(): ViewModel() {

    lateinit var merchant: MerchantQuery.GetMerchant
    val paymentMethod by lazy { MutableLiveData("Cash On Delivery") }
    val isRefreshed by lazy { MutableLiveData(false) }
    var favoriteDirection = 1
    val reviewed by lazy { MutableLiveData(false) }
    var prevDestination: Int? = null
}