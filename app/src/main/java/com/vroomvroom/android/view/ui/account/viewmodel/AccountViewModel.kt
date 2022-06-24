package com.vroomvroom.android.view.ui.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.repository.user.UserRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _isGetUserSuccessful by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isGetUserSuccessful: LiveData<ViewState<Boolean>>
        get() = _isGetUserSuccessful
    private val _isNameUpdated by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isNameUpdated: LiveData<ViewState<Boolean>>
        get() = _isNameUpdated

    fun getUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.getUser()
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isGetUserSuccessful.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isGetUserSuccessful.postValue(data)
                    }
                    else -> {
                        _isGetUserSuccessful.postValue(data)
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _isNameUpdated.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.updateName(name)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isNameUpdated.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isNameUpdated.postValue(data)
                    }
                    else -> {
                        _isNameUpdated.postValue(data)
                    }
                }
            }
        }
    }

}