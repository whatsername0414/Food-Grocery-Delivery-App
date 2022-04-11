package com.vroomvroom.android.view.ui.auth.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.*
import com.vroomvroom.android.domain.db.user.UserEntity
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.repository.remote.GraphQLRepository
import com.vroomvroom.android.utils.SmsBroadcastReceiver
import com.vroomvroom.android.utils.SmsBroadcastReceiverListener
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class AuthViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val graphQLRepository: GraphQLRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val smsBroadcastReceiver: SmsBroadcastReceiver,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _otpGenerateConfirmation by lazy { MutableLiveData<ViewState<VerifyMobileNumberMutation.Data>>() }
    val otpGenerateConfirmation: LiveData<ViewState<VerifyMobileNumberMutation.Data>>
        get() = _otpGenerateConfirmation

    private val _otpVerificationResult by lazy { MutableLiveData<ViewState<OtpVerificationMutation.Data>>() }
    val otpVerificationResult: LiveData<ViewState<OtpVerificationMutation.Data>>
        get() = _otpVerificationResult

    private val _user by lazy { MutableLiveData<ViewState<RegisterMutation.Data>>() }
    val user: LiveData<ViewState<RegisterMutation.Data>>
        get() = _user

    val token = preferences.token.asLiveData()

    val userRecord = roomRepository.getUser()
    val newLoggedInUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val isPasswordResetEmailSent by lazy { MutableLiveData<ViewState<String>>() }
    val messageIntent by lazy { MutableLiveData<ViewState<Intent>>() }

    val signInIntent = firebaseAuthRepository.signInIntent()
    val broadcastReceiver = smsBroadcastReceiver

    fun mutationVerifyMobileNumber(number: String) {
        _otpGenerateConfirmation.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationVerifyMobileNumber(number)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> _otpGenerateConfirmation.postValue(data)
                    is ViewState.Error -> _otpGenerateConfirmation.postValue(data)
                    else -> _otpGenerateConfirmation.postValue(data)
                }
            }
        }
    }

    fun mutationOtpVerification(otp: String) {
        _otpVerificationResult.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationOtpVerification(otp)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> _otpVerificationResult.postValue(data)
                    is ViewState.Error -> _otpVerificationResult.postValue(data)
                    else -> _otpVerificationResult.postValue(data)
                }
            }
        }
    }

    fun saveIdToken() {
        firebaseAuthRepository.getIdToken { result ->
            result?.let { token ->
                Log.d("AuthViewModel", token)
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

    fun register() {
        _user.postValue(ViewState.Loading)
        viewModelScope.launch {
            val response = graphQLRepository.mutationRegister()
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

    fun insertUserRecord(userEntity: UserEntity) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.insertUser(userEntity)
    }

    fun updateUserRecord(userEntity: UserEntity) = viewModelScope.launch(Dispatchers.IO) {
        roomRepository.updateUser(userEntity)
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
                else -> {
                    newLoggedInUser.postValue(result)
                }
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
                else -> {
                    newLoggedInUser.postValue(result)
                }
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
                else -> {
                    newLoggedInUser.postValue(result)
                }
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
                else -> {
                    newLoggedInUser.postValue(result)
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