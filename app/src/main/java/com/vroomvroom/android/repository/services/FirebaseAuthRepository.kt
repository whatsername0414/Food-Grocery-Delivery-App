package com.vroomvroom.android.repository.services

import android.content.Intent
import com.facebook.AccessToken
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.view.resource.Resource

interface FirebaseAuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun logoutUser(listener: OnCompleteListener<Void>)
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener)
    fun getIdToken(onResult: (String?)->Unit)
    fun signInIntent(): Intent
    fun googleSignIn(data: Intent?, onResult: (Resource<FirebaseUser>) -> Unit)
    fun firebaseAuthWithGoogle(idToken: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun facebookLogin(fragment: BottomSheetDialogFragment, onResult: (Resource<FirebaseUser>) -> Unit)
    fun handleFacebookAccessToken(token: AccessToken, onResult: (Resource<FirebaseUser>) -> Unit)
    fun logInWithEmailAndPassword(emailAddress: String, password: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun registerWithEmailAndPassword(emailAddress: String, password: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun resetPasswordWithEmail(emailAddress: String, onSent: (Resource<String>) -> Unit)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun smsRetrieverClient()
}