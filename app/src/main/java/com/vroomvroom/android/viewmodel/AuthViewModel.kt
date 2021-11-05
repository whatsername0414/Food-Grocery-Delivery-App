package com.vroomvroom.android.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.OtpVerificationMutation
import com.vroomvroom.android.RegisterMutation
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.SmsBroadcastReceiver
import com.vroomvroom.android.VerifyMobileNumberMutation
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.utils.SmsBroadcastReceiverListener
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class AuthViewModel @Inject constructor(
    private val graphQLRepository: GraphQLRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val smsBroadcastReceiver: SmsBroadcastReceiver,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _otpGenerateConfirmation by lazy { MutableLiveData<ViewState<VerifyMobileNumberMutation.Data>>() }
    val otpGenerateConfirmation: LiveData<ViewState<VerifyMobileNumberMutation.Data>>
        get() = _otpGenerateConfirmation

    private val _userRecord by lazy { MutableLiveData<ViewState<RegisterMutation.Data>>() }
    val userRecord: LiveData<ViewState<RegisterMutation.Data>>
        get() = _userRecord

    private val _updatedUser by lazy { MutableLiveData<ViewState<OtpVerificationMutation.Data>>() }
    val updatedUser: LiveData<ViewState<OtpVerificationMutation.Data>>
        get() = _updatedUser

    val currentUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val newLoggedInUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val isPasswordResetEmailSent by lazy { MutableLiveData<ViewState<String>>() }
    val messageIntent by lazy { MutableLiveData<ViewState<Intent>>() }

    val signInIntent = firebaseAuthRepository.signInIntent()
    val broadcastReceiver = smsBroadcastReceiver

    fun mutationVerifyMobileNumber(number: String) {
        _otpGenerateConfirmation.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationVerifyMobileNumber(number)
            response.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _otpGenerateConfirmation.postValue(data)
                    }
                    is ViewState.Error -> {
                        _otpGenerateConfirmation.postValue(data)
                    }
                    else -> {
                        _otpGenerateConfirmation.postValue(data)
                    }
                }
            }
        }
    }

    fun mutationOtpVerification(otp: String) {
        _updatedUser.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationOtpVerification(otp)
            response.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _updatedUser.postValue(data)
                    }
                    is ViewState.Error -> {
                        _updatedUser.postValue(data)
                    }
                    else -> {
                        _updatedUser.postValue(data)
                    }
                }
            }
        }
    }

    fun saveIdToken() {
        firebaseAuthRepository.getIdToken { result ->
            result?.let { token ->
                viewModelScope.launch {
                    preferences.saveToken(token)
                }
            }
        }
    }

    fun getIdToken() {
        viewModelScope.launch {
            preferences.token.first()
        }
    }

    fun getCurrentUser() {
        firebaseAuthRepository.getCurrentUser { result ->
            when (result) {
                is ViewState.Success -> {
                    currentUser.postValue(result)
                }
                is ViewState.Error -> {
                    currentUser.postValue(result)
                }
                else -> Log.d("AuthViewModel", result.toString())
            }
        }
    }

    fun register() {
        _userRecord.postValue(ViewState.Loading)
        firebaseAuthRepository.getIdToken { result->
            if (result != null) {
                viewModelScope.launch {
                    val response = graphQLRepository.mutationRegister()
                    response?.let { data ->
                        when (data) {
                            is ViewState.Success -> {
                                _userRecord.postValue(data)
                            }
                            is ViewState.Error -> {
                                _userRecord.postValue(data)
                            }
                            else -> {
                                _userRecord.postValue(data)
                            }
                        }
                    }
                }
            }
        }

    }

    fun taskGoogleSignIn(data: Intent?) {
        firebaseAuthRepository.taskGoogleSignIn(data) { result ->
            when (result) {
                is ViewState.Success -> {
                    newLoggedInUser.postValue(result)

                }
                is ViewState.Error -> {
                    newLoggedInUser.postValue(result)
                }
                else -> Log.d("AuthViewModel", result.toString())
            }
        }
    }
    fun facebookLogIn(fragment: BottomSheetDialogFragment) {
        firebaseAuthRepository.facebookLogin(fragment) { result ->
            when (result) {
                is ViewState.Success -> {
                    newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    newLoggedInUser.postValue(result)
                }
                else -> Log.d("AuthViewModel", result.toString())
            }
        }
    }

    fun registerWithEmailAndPassword(emailAddress: String, password: String) {
        firebaseAuthRepository.registerWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is ViewState.Success -> {
                    newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    newLoggedInUser.postValue(result)
                }
                else -> Log.d("AuthViewModel", result.toString())
            }
        }
    }

    fun logInWithEmailAndPassword(emailAddress: String, password: String) {
        firebaseAuthRepository.logInWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is ViewState.Success -> {
                    newLoggedInUser.postValue(result)
                }
                is ViewState.Error -> {
                    newLoggedInUser.postValue(result)
                }
                else -> Log.d("AuthViewModel", result.toString())
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
                else -> Log.d("AuthViewModel", result.toString())
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
                    is ViewState.Success -> messageIntent.postValue(intent)
                    is ViewState.Error -> messageIntent.postValue(intent)
                    else -> Log.d("AuthViewModel", intent.toString())
                }
            }
        }
    }
}