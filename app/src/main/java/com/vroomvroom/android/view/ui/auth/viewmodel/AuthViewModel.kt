package com.vroomvroom.android.view.ui.auth.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.repository.auth.AuthRepository
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.repository.user.UserRepository
import com.vroomvroom.android.utils.SmsBroadcastReceiver
import com.vroomvroom.android.utils.SmsBroadcastReceiverListener
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val smsBroadcastReceiver: SmsBroadcastReceiver,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _isOtpSent by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isOtpSent: LiveData<ViewState<Boolean>>
        get() = _isOtpSent

    private val _isPhoneNumberVerified by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isPhoneNumberVerified: LiveData<ViewState<Boolean>>
        get() = _isPhoneNumberVerified

    private val _isRegistered by lazy { MutableLiveData<ViewState<Boolean>>() }
    val isRegistered: LiveData<ViewState<Boolean>>
        get() = _isRegistered

    val token = preferences.token.asLiveData()

    val user = userRepository.getUserLocale()
    private val _newLoggedInUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val newLoggedInUser: LiveData<ViewState<FirebaseUser>>
        get() = _newLoggedInUser
    val isPasswordResetEmailSent by lazy { MutableLiveData<ViewState<String>>() }
    val messageIntent by lazy { MutableLiveData<ViewState<Intent>>() }

    val signInIntent = firebaseAuthRepository.signInIntent()
    val broadcastReceiver = smsBroadcastReceiver

    fun registerPhoneNumber(number: String) {
        _isOtpSent.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.registerPhoneNumber(number)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isOtpSent.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isOtpSent.postValue(data)
                    }
                    else -> {
                        _isOtpSent.postValue(data)
                    }
                }
            }
        }
    }

    fun resetOtpLiveData() {
        _isOtpSent.postValue(null)
    }

    fun resetPhoneRegistration() {
        _isOtpSent.postValue(null)
    }

    fun verifyOtp(otp: String) {
        _isPhoneNumberVerified.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.verifyOtp(otp)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isPhoneNumberVerified.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isPhoneNumberVerified.postValue(data)
                    }
                    else -> {
                        _isPhoneNumberVerified.postValue(data)
                    }
                }
            }
        }
    }

    fun saveIdToken() {
        firebaseAuthRepository.getIdToken { result ->
            result?.let { token ->
                Log.d("AuthViewModel", token)
                viewModelScope.launch(Dispatchers.IO) {
                    preferences.saveToken(token)
                }
            }
        }
    }

    fun clearDataStore() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.clear()
        }
    }

    fun getLocalIdToken() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.token.first()
        }
    }

    fun register(locationEntity: LocationEntity) {
        _isRegistered.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.register(locationEntity)
            response.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _isRegistered.postValue(data)
                    }
                    is ViewState.Error -> {
                        _isRegistered.postValue(data)
                    }
                    else -> {
                        _isRegistered.postValue(data)
                    }
                }
            }
        }
    }

    fun logoutUser(successful: (Boolean) -> Unit) {
        val listener = OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                _isRegistered.postValue(null)
                _newLoggedInUser.postValue(null)
                successful.invoke(true)
            } else {
                successful.invoke(false)
            }
        }
        firebaseAuthRepository.logoutUser(listener)
    }

    fun deleteUserRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteUserLocale()
        }
    }

    fun googleSignIn(data: Intent?) {
        _newLoggedInUser.postValue(ViewState.Loading)
        firebaseAuthRepository.googleSignIn(data) { result ->
            when (result) {
                is ViewState.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }
    fun facebookLogIn(fragment: BottomSheetDialogFragment) {
        _newLoggedInUser.postValue(ViewState.Loading)
        firebaseAuthRepository.facebookLogin(fragment) { result ->
            when (result) {
                is ViewState.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun registerWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(ViewState.Loading)
        firebaseAuthRepository.registerWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is ViewState.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun logInWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(ViewState.Loading)
        firebaseAuthRepository.logInWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is ViewState.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun resetPasswordWithEmail(emailAddress: String) {
        firebaseAuthRepository.resetPasswordWithEmail(emailAddress) { result ->
            when (result) {
                is ViewState.Success -> {
                    isPasswordResetEmailSent.postValue(result)
                }
                is ViewState.Error -> {
                    isPasswordResetEmailSent.postValue(result)
                }
                else -> {
                    isPasswordResetEmailSent.postValue(result)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        firebaseAuthRepository.onActivityResult(requestCode, resultCode, data)
    }

    fun registerBroadcastReceiver() {
        firebaseAuthRepository.smsRetrieverClient()
        smsBroadcastReceiver.smsBroadcastReceiverListener = object : SmsBroadcastReceiverListener {
            override fun onIntent(intent: ViewState<Intent>) {
                when (intent) {
                    is ViewState.Success -> {
                        messageIntent.postValue(intent)
                    }
                    is ViewState.Error -> {
                        messageIntent.postValue(intent)
                    }
                    else -> {
                        messageIntent.postValue(intent)
                    }
                }
            }
        }
    }
}