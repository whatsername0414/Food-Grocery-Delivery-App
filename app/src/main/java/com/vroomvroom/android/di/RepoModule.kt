package com.vroomvroom.android.di

import android.content.Context
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.vroomvroom.android.R
import com.vroomvroom.android.db.CartItemDatabase
import com.vroomvroom.android.utils.Constants.CART_ITEM_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Singleton
    @Provides
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.builder()
            .serverUrl("http://192.168.1.5:5000/")
            .okHttpClient(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideCartItemDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        CartItemDatabase::class.java,
        CART_ITEM_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideCartItemDao(db: CartItemDatabase) = db.getCartItemDao()

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
}