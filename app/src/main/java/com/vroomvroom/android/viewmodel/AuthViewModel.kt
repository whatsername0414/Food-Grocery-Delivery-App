package com.vroomvroom.android.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.vroomvroom.android.LoginMutation
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.repository.DataRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: DataRepository,
): ViewModel() {
    private val _loginToken by lazy { MutableLiveData<ViewState<Response<LoginMutation.Data>>>() }
    private val _registerToken by lazy { MutableLiveData<ViewState<Response<RegisterMutation.Data>>>() }

    val loginToken: LiveData<ViewState<Response<LoginMutation.Data>>>
        get() = _loginToken

    val registerToken: LiveData<ViewState<Response<RegisterMutation.Data>>>
        get() = _registerToken

    fun mutationLogin(username: String, password: String) = viewModelScope.launch {
        _loginToken.postValue(ViewState.Loading())
        try {
            val response = repository.mutationLogin(username, password)
            val authErrors = response.errors?.get(0)?.message
            if (authErrors == null) {
                _loginToken.postValue(ViewState.Success(response))
                Log.d("AuthViewModel", response.data?.login?.token.toString())
                repository.saveToken(response.data?.login?.token.toString())
            } else {
                _loginToken.postValue(ViewState.Auth(authErrors))
            }
        } catch (exception: ApolloException) {
            Log.e("AuthViewModel", "ApolloException", exception)
            _loginToken.postValue(ViewState.Error("Error fetching user"))
        }
    }

    fun mutationRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String) = viewModelScope.launch {
            _registerToken.postValue(ViewState.Loading())
        try {
            val response = repository.mutationRegister(username, email, password, confirmPassword)
            val authErrors = response.errors?.get(0)?.message
            if (authErrors == null) {
                _registerToken.postValue(ViewState.Success(response))
                Log.d("AuthViewModel", response.data?.register?.token.toString())
                repository.saveToken(response.data?.register?.token.toString())
            } else {
                _registerToken.postValue(ViewState.Auth(authErrors))
            }
        } catch (exception: ApolloException) {
            Log.e("AuthViewModel", "ApolloException", exception)
            _registerToken.postValue(ViewState.Error("Error fetching user"))
        }
    }
}