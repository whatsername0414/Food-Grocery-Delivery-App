package com.vroomvroom.android.repository.services

import android.content.Intent
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.view.resource.Resource
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginManager: LoginManager,
    private val callbackManager: CallbackManager,
    private val googleSignInClient: GoogleSignInClient,
    private val client: SmsRetrieverClient
) : BaseRepository(), FirebaseAuthRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun logoutUser(listener: OnCompleteListener<Void>) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(listener)
    }

    override fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    override fun getIdToken(onResult: (String?)->Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result.token
                        onResult(token)

                    } else onResult(null)
                }
        }
    }

    override fun signInIntent()  = googleSignInClient.signInIntent

    override fun googleSignIn(data: Intent?, onResult: (Resource<FirebaseUser>) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken.orEmpty(), onResult)
            } catch (e: ApiException) {
                onResult(handleException(401))
                Log.w("FirebaseAuthBaseRepository", "Google sign in failed", e)
            }
        } else {
            onResult(handleException(401))
            Log.d("FirebaseAuthBaseRepository", exception.toString())
        }
    }

    override fun firebaseAuthWithGoogle(
        idToken: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                val exception = task.exception
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    onResult(handleException(401))
                    Log.d("AuthBottomSheetFragment", exception.toString())
                }
            }
    }

    override fun facebookLogin(
        fragment: BottomSheetDialogFragment,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        loginManager.logIn(fragment, listOf("email", "public_profile"))
        loginManager.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("FirebaseAuthRepository", "facebook:onSuccess:$result")
                handleFacebookAccessToken(result.accessToken, onResult)
            }

            override fun onCancel() {
                onResult(handleException(401))
                Log.d("FirebaseAuthRepository", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                onResult(handleException(401))
                Log.d("FirebaseAuthRepository", "facebook:onError", error)
            }
        })
    }

    override fun handleFacebookAccessToken(
        token: AccessToken,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    onResult(handleException(401))
                    Log.w("FirebaseAuthRepository", "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun logInWithEmailAndPassword(
        emailAddress: String,
        password: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    val exception = task.exception?.message
                    onResult(handleException(401, exception))
                    Log.w("FirebaseAuthRepositoryImpl", "signInWithEmail:failure", task.exception)
                }
            }
    }

    override fun registerWithEmailAndPassword(
        emailAddress: String,
        password: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FirebaseAuthRepositoryImpl", "signInWithEmail:success")
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    onResult(handleException(409))
                    Log.w("FirebaseAuthRepositoryImpl", "signInWithEmail:failure", task.exception)
                }
            }
    }

    override fun resetPasswordWithEmail(
        emailAddress: String,
        onSent: (Resource<String>) -> Unit
    ) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSent(handleSuccess("Email sent"))
                } else onSent(handleException(0))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun smsRetrieverClient(){
        client.startSmsUserConsent(null)
    }
}