package com.vroomvroom.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.vroomvroom.android.R
import com.vroomvroom.android.db.CartItemDatabase
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.utils.Constants.CART_ITEM_DATABASE_NAME
import com.vroomvroom.android.utils.Constants.PREFERENCES_STORE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext app: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
    produceFile = {
        app.preferencesDataStoreFile(PREFERENCES_STORE_NAME)
    })


    @Singleton
    @Provides
    fun provideOkHttpClient(preferences: UserPreferences): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val token = runBlocking { preferences.token.first() }
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.builder()
            .serverUrl("http://192.168.1.10:5000/")
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

    @Singleton
    @Provides
    fun provideClient(@ApplicationContext app: Context): SmsRetrieverClient = SmsRetriever.getClient(app)
}