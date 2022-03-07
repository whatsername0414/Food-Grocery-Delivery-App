package com.vroomvroom.android.view.ui.account.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.UpdateNameMutation
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class AccountViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
) : ViewModel() {

    private val _user by lazy { MutableLiveData<ViewState<UpdateNameMutation.Data>>() }
    val user: LiveData<ViewState<UpdateNameMutation.Data>>
        get() = _user

    fun mutationUpdateName(name: String) {
        _user.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = graphQLRepository.mutationUpdateName(name)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _user.postValue(data)
                    }
                    is ViewState.Error -> {
                        _user.postValue(data)
                    }
                    else -> {
                        _user.postValue(data)
                    }
                }
            }
        }
    }

}