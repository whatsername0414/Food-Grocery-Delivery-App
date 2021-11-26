package com.vroomvroom.android.repository.services

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import com.vroomvroom.android.view.state.ViewState

interface FirebaseAuthRepository {
    fun getIdToken(onResult: (String?)->Unit)
    fun signInIntent(): Intent
    fun taskGoogleSignIn(data: Intent?, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun firebaseAuthWithGoogle(idToken: String, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun facebookLogin(fragment: BottomSheetDialogFragment, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun handleFacebookAccessToken(token: AccessToken, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun logInWithEmailAndPassword(emailAddress: String, password: String, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun registerWithEmailAndPassword(emailAddress: String, password: String, onResult: (ViewState<FirebaseUser>) -> Unit)
    fun resetPasswordWithEmail(emailAddress: String, onSent: (ViewState<String>) -> Unit)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun smsRetrieverClient()
}