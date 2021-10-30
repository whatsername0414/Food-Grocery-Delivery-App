package com.vroomvroom.android.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.repository.services.FirebaseAuthRepository
import com.vroomvroom.android.utils.CurrentUserCallback
import com.vroomvroom.android.utils.IdTokenCallback
import com.vroomvroom.android.utils.PasswordResetCallback
import com.vroomvroom.android.utils.TaskListener
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class AuthViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
) : ViewModel(), IdTokenCallback, TaskListener, CurrentUserCallback, PasswordResetCallback {

    val idToken by lazy { MutableLiveData<ViewState<String>>() }
    val currentUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val newLoggedInUser by lazy { MutableLiveData<ViewState<FirebaseUser>>() }
    val isPasswordResetEmailSent by lazy { MutableLiveData<ViewState<String>>() }

    val signInIntent = firebaseAuthRepository.signInIntent()

    fun getIdToken() {
        firebaseAuthRepository.getIdToken(this)
    }
    fun getCurrentUser() {
        firebaseAuthRepository.getCurrentUser(this)
    }
    fun taskGoogleSignIn(data: Intent?) {
        firebaseAuthRepository.taskGoogleSignIn(data, this)
    }
    fun facebookLogIn(fragment: BottomSheetDialogFragment) {
        firebaseAuthRepository.facebookLogin(fragment, this)
    }
    fun logInWithEmailAndPassword(emailAddress: String, password: String) {
        firebaseAuthRepository.logInWithEmailAndPassword(emailAddress, password, this)
    }
    fun registerWithEmailAndPassword(emailAddress: String, password: String) {
        firebaseAuthRepository.registerWithEmailAndPassword(emailAddress, password, this)
    }
    fun resetPasswordWithEmail(emailAddress: String) {
        firebaseAuthRepository.resetPasswordWithEmail(emailAddress, this)
    }
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        firebaseAuthRepository.onActivityResult(requestCode, resultCode, data)
    }

    override fun idToken(result: ViewState<String>) {
        when(result) {
            is ViewState.Success -> {
                idToken.postValue(result)
            }
            is ViewState.Error -> {
                idToken.postValue(result)
            }
            else -> Log.d("AuthViewModel", result.toString())
        }

    }

    override fun getCurrentUser(result: ViewState<FirebaseUser>) {
        when(result) {
            is ViewState.Success -> {
                currentUser.postValue(result)
            }
            is ViewState.Error -> {
                currentUser.postValue(result)
            }
            else -> Log.d("AuthViewModel", result.toString())
        }
    }
    override fun newLoggedInUser(result: ViewState<FirebaseUser>) {
        when(result) {
            is ViewState.Success -> {
                newLoggedInUser.postValue(result)
            }
            is ViewState.Error -> {
                newLoggedInUser.postValue(result)
            }
            else -> Log.d("AuthViewModel", result.toString())
        }
    }

    override fun resetPassword(result: ViewState<String>) {
        when(result) {
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