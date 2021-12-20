package com.vroomvroom.android.view.ui.orders.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class OrdersActivityViewModel @Inject constructor(): ViewModel() {
    val isRefreshed by lazy { MutableLiveData<Boolean>() }
}