package com.vroomvroom.android.di

import android.content.Context
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.vroomvroom.android.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepoModule {
    @Singleton
    @Provides
    fun provideGoogleSignInOptions(@ApplicationContext app: Context): GoogleSignInOptions = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(app.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    @Singleton
    @Provides
    fun provideGoogleSignInClient(
        @ApplicationContext app: Context,
        gso: GoogleSignInOptions
    ): GoogleSignInClient = GoogleSignIn.getClient(app, gso)

    @Singleton
    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideLoginManager(): LoginManager = LoginManager.getInstance()

    @Singleton
    @Provides
    fun provideCallbackManager(): CallbackManager = CallbackManager.Factory.create()

    @Singleton
    @Provides
    fun provideClient(@ApplicationContext app: Context): SmsRetrieverClient = SmsRetriever.getClient(app)
}