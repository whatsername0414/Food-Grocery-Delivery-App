package com.vroomvroom.android.view.ui.auth.viewmodel

import android.content.Intent
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
import com.vroomvroom.android.view.resource.Resource
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

    private val _isOtpSent by lazy { MutableLiveData<Resource<Boolean>>() }
    val isOtpSent: LiveData<Resource<Boolean>>
        get() = _isOtpSent

    private val _isVerified by lazy { MutableLiveData<Resource<Boolean>>() }
    val isVerified: LiveData<Resource<Boolean>>
        get() = _isVerified

    private val _isRegistered by lazy { MutableLiveData<Resource<Boolean>>() }
    val isRegistered: LiveData<Resource<Boolean>>
        get() = _isRegistered

    val token = preferences.token.asLiveData()

    val signedInUser = firebaseAuthRepository.getCurrentUser()
    private val _newLoggedInUser by lazy { MutableLiveData<Resource<FirebaseUser>>() }
    val newLoggedInUser: LiveData<Resource<FirebaseUser>>
        get() = _newLoggedInUser
    val isPasswordResetEmailSent by lazy { MutableLiveData<Resource<String>>() }
    val messageIntent by lazy { MutableLiveData<Resource<Intent>>() }

    val signInIntent = firebaseAuthRepository.signInIntent()
    val broadcastReceiver = smsBroadcastReceiver

    fun resetOtpLiveData() {
        _isOtpSent.postValue(null)
    }

    fun resetPhoneRegistration() {
        _isOtpSent.postValue(null)
    }

    fun generateEmailOtp(emailAddress: String) {
        _isOtpSent.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.generateEmailOtp(emailAddress)
            when (response) {
                is Resource.Success ->
                    _isOtpSent.postValue(response)
                is Resource.Error -> {
                    _isOtpSent.postValue(response)
                }
                else -> Unit
            }
        }
    }

    fun verifyEmailOtp(emailAddress: String, otp: String) {
        _isVerified.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.verifyEmailOtp(emailAddress, otp)
            when (response) {
                is Resource.Success -> {
                    _isVerified.postValue(response)
                }
                is Resource.Error -> {
                    _isVerified.postValue(response)
                }
                else -> {
                    _isVerified.postValue(response)
                }
            }
        }
    }

    fun registerPhoneNumber(number: String) {
        _isOtpSent.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.generatePhoneOtp(number)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _isOtpSent.postValue(data)
                    }
                    is Resource.Error -> {
                        _isOtpSent.postValue(data)
                    }
                    else -> {
                        _isOtpSent.postValue(data)
                    }
                }
            }
        }
    }

    fun verifyPhoneOtp(otp: String) {
        _isVerified.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.verifyOtp(otp)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _isVerified.postValue(data)
                    }
                    is Resource.Error -> {
                        _isVerified.postValue(data)
                    }
                    else -> {
                        _isVerified.postValue(data)
                    }
                }
            }
        }
    }

    fun saveIdToken() {
        firebaseAuthRepository.getIdToken { result ->
            result?.let { token ->
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
        _isRegistered.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.register(locationEntity)
            response.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _isRegistered.postValue(data)
                    }
                    is Resource.Error -> {
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
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseAuthRepository.googleSignIn(data) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }
    fun facebookLogIn(fragment: BottomSheetDialogFragment) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseAuthRepository.facebookLogin(fragment) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun registerWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseAuthRepository.registerWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun logInWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseAuthRepository.logInWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
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
                is Resource.Success -> {
                    isPasswordResetEmailSent.postValue(result)
                }
                is Resource.Error -> {
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
            override fun onIntent(intent: Resource<Intent>) {
                when (intent) {
                    is Resource.Success -> {
                        messageIntent.postValue(intent)
                    }
                    is Resource.Error -> {
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