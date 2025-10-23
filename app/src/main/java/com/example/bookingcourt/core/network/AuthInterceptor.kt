package com.example.bookingcourt.core.network

import android.util.Log
import com.example.bookingcourt.data.local.datastore.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    // Danh sách các endpoint KHÔNG cần authentication
    private val publicEndpoints = listOf(
        "auth/login",
        "auth/register",
        "auth/forgot-password",
        "auth/refresh-token",
        "auth/reset-password"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        val fullUrl = originalRequest.url.toString()

        // Kiểm tra nếu là endpoint công khai thì KHÔNG thêm token
        // So sánh với cả path đầy đủ và path không có prefix
        val isPublicEndpoint = publicEndpoints.any { endpoint ->
            requestPath.endsWith(endpoint) || requestPath.contains("/$endpoint")
        }

        Log.d(TAG, "==================== AUTH INTERCEPTOR ====================")
        Log.d(TAG, "Full URL: $fullUrl")
        Log.d(TAG, "Request Path: $requestPath")
        Log.d(TAG, "Is Public Endpoint: $isPublicEndpoint")

        val request = if (isPublicEndpoint) {
            // Endpoint công khai - không thêm Authorization header
            Log.d(TAG, "✓ Skipping auth token for public endpoint")
            originalRequest
        } else {
            // Endpoint cần authentication - thêm token nếu có
            val token = runBlocking {
                userPreferencesDataStore.accessToken.first()
            }

            Log.d(TAG, "Token exists: ${!token.isNullOrEmpty()}")

            if (!token.isNullOrEmpty()) {
                Log.d(TAG, "✓ Adding Bearer token to request")
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                Log.d(TAG, "⚠ No token available")
                originalRequest
            }
        }

        Log.d(TAG, "========================================================")

        return chain.proceed(request)
    }
}
