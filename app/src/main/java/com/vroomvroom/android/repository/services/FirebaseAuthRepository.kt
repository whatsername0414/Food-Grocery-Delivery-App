package com.vroomvroom.android.repository.services

import android.content.Intent
import com.facebook.AccessToken
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.utils.CurrentUserCallback
import com.vroomvroom.android.utils.IdTokenCallback
import com.vroomvroom.android.utils.PasswordResetCallback
import com.vroomvroom.android.utils.TaskListener

interface FirebaseAuthRepository {
    fun getCurrentUser(currentUserCallback: CurrentUserCallback)
    fun getIdToken(idTokenCallback: IdTokenCallback)
    fun signInIntent(): Intent
    fun taskGoogleSignIn(data: Intent?, taskListener: TaskListener)
    fun firebaseAuthWithGoogle(idToken: String, taskListener: TaskListener)
    fun facebookLogin(fragment: BottomSheetDialogFragment, TaskListener: TaskListener)
    fun handleFacebookAccessToken(token: AccessToken, TaskListener: TaskListener)
    fun logInWithEmailAndPassword(emailAddress: String, password: String, TaskListener: TaskListener)
    fun registerWithEmailAndPassword(emailAddress: String, password: String, TaskListener: TaskListener)
    fun resetPasswordWithEmail(emailAddress: String, PasswordResetCallback: PasswordResetCallback)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}