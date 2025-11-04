package com.example.bookingcourt.core.di

import android.content.Context
import android.util.Log
import com.example.bookingcourt.BuildConfig
import com.example.bookingcourt.core.network.AuthInterceptor
import com.example.bookingcourt.core.network.NetworkMonitor
import com.example.bookingcourt.core.utils.Constants
import com.example.bookingcourt.data.local.datastore.UserPreferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "NetworkModule"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        userPreferencesDataStore: UserPreferencesDataStore,
    ): AuthInterceptor {
        Log.d(TAG, "✓✓✓ Creating AuthInterceptor ✓✓✓")
        return AuthInterceptor(userPreferencesDataStore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        Log.d(TAG, "✓✓✓ Creating OkHttpClient with AuthInterceptor ✓✓✓")

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
    ): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.AuthApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCourtApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.CourtApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.CourtApi::class.java)

    @Provides
    @Singleton
    fun provideBookingApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.BookingApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.BookingApi::class.java)

    @Provides
    @Singleton
    fun provideVenueApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.VenueApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.VenueApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.UserApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.UserApi::class.java)

    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.ReviewApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.ReviewApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): com.example.bookingcourt.data.remote.api.NotificationApi =
        retrofit.create(com.example.bookingcourt.data.remote.api.NotificationApi::class.java)
}
