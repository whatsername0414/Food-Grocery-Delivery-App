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
import com.vroomvroom.android.repository.UserPreferences
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: GraphQLRepository,
    private val preferences: UserPreferences
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
                preferences.saveToken(response.data?.login?.token.toString())
            } else {
                _loginToken.postValue(ViewState.Auth(authErrors))
            }
        } catch (exception: ApolloException) {
            Log.e("AuthViewModel", "ApolloException", exception)
            _loginToken.postValue(ViewState.Error("Error fetching user"))
        }
    }

    fun mutationRegister(
        email: String,
        password: String,
        confirmPassword: String) = viewModelScope.launch {
            _registerToken.postValue(ViewState.Loading())
        try {
            Log.d("AuthViewModel", email)
            val response = repository.mutationRegister(email, password, confirmPassword)
            val authErrors = response.errors?.get(0)?.message
            if (authErrors == null) {
                _registerToken.postValue(ViewState.Success(response))
                preferences.saveToken(response.data?.register?.token.toString())
            } else {
                _registerToken.postValue(ViewState.Auth(authErrors))
            }
        } catch (exception: ApolloException) {
            Log.e("AuthViewModel", "ApolloException", exception)
            _registerToken.postValue(ViewState.Error("Error fetching new user"))
        }
    }
}