package com.vroomvroom.android.repository.services

import android.content.Intent
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.vroomvroom.android.utils.CurrentUserCallback
import com.vroomvroom.android.utils.IdTokenCallback
import com.vroomvroom.android.utils.PasswordResetCallback
import com.vroomvroom.android.utils.TaskListener
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginManager: LoginManager,
    private val callbackManager: CallbackManager,
    private val googleSignInClient: GoogleSignInClient
) : FirebaseAuthBaseRepository(), FirebaseAuthRepository {

    override fun getCurrentUser(currentUserCallback: CurrentUserCallback) {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                currentUserCallback.getCurrentUser(handleSuccess(user))
            } else {
                currentUserCallback.getCurrentUser(handleException(0))
            }
        }

    }

    override fun getIdToken(idTokenCallback: IdTokenCallback) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.token?.let { token ->
                            idTokenCallback.idToken(handleSuccess(token))
                        }
                    } else idTokenCallback.idToken(handleException(0))
                }
        }
    }

    override fun signInIntent()  = googleSignInClient.signInIntent

    override fun taskGoogleSignIn(data: Intent?, taskListener: TaskListener) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!, taskListener)
            } catch (e: ApiException) {
                taskListener.newLoggedInUser(handleException(401))
                Log.w("FirebaseAuthBaseRepository", "Google sign in failed", e)
            }
        } else {
            taskListener.newLoggedInUser(handleException(401))
            Log.d("FirebaseAuthBaseRepository", exception.toString())
        }
    }

    override fun firebaseAuthWithGoogle(idToken: String, taskListener: TaskListener) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                val exception = task.exception
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        taskListener.newLoggedInUser(handleSuccess(user))
                    }
                } else {
                    taskListener.newLoggedInUser(handleException(401))
                    Log.d("AuthBottomSheetFragment", exception.toString())
                }
            }
    }

    override fun facebookLogin(fragment: BottomSheetDialogFragment, TaskListener: TaskListener) {
        loginManager.logIn(fragment, listOf("email", "public_profile"))
        loginManager.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("FirebaseAuthRepository", "facebook:onSuccess:$result")
                handleFacebookAccessToken(result.accessToken, TaskListener)
            }

            override fun onCancel() {
                TaskListener.newLoggedInUser(handleException(401))
                Log.d("FirebaseAuthRepository", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                TaskListener.newLoggedInUser(handleException(401))
                Log.d("FirebaseAuthRepository", "facebook:onError", error)
            }
        })
    }

    override fun handleFacebookAccessToken(token: AccessToken, TaskListener: TaskListener) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        TaskListener.newLoggedInUser(handleSuccess(user))
                    }
                } else {
                    TaskListener.newLoggedInUser(handleException(401))
                    Log.w("FirebaseAuthRepository", "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun logInWithEmailAndPassword(
        emailAddress: String,
        password: String,
        TaskListener: TaskListener
    ) {
        auth.signInWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        TaskListener.newLoggedInUser(handleSuccess(user))
                    }
                } else {
                    val exception = task.exception?.message
                    exception?.let {
                        if (it.contains("password")) {
                            TaskListener.newLoggedInUser(handleException(403))
                        } else TaskListener.newLoggedInUser(handleException(404))
                    }
                    Log.w("LoginFragment", "signInWithEmail:failure", task.exception)
                }
            }
    }

    override fun registerWithEmailAndPassword(
        emailAddress: String,
        password: String,
        TaskListener: TaskListener
    ) {
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("RegisterFragment", "signInWithEmail:success")
                    auth.currentUser?.let { user ->
                        TaskListener.newLoggedInUser(handleSuccess(user))
                    }
                } else {
                    TaskListener.newLoggedInUser(handleException(409))
                    Log.w("RegisterFragment", "signInWithEmail:failure", task.exception)
                }
            }
    }

    override fun resetPasswordWithEmail(emailAddress: String, PasswordResetCallback: PasswordResetCallback) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    PasswordResetCallback.resetPassword(handleSuccess("Email sent"))
                } else PasswordResetCallback.resetPassword(handleException(0))
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}