package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vroomvroom.android.domain.model.merchant.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ActivityViewModel @Inject constructor(): ViewModel() {
    val paymentMethod by lazy { MutableLiveData("Cash On Delivery") }
    var currentMerchant: MutableMap<String, Merchant> = mutableMapOf()
    var favoriteDirection = 1
}