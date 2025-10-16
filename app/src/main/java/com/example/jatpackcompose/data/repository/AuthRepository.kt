package com.example.jatpackcompose.data.repository

import com.example.jatpackcompose.data.api.RetrofitClient
import com.example.jatpackcompose.data.model.LoginRequest
import com.example.jatpackcompose.data.model.LoginResponse
import com.example.jatpackcompose.data.model.RegisterRequest
import com.example.jatpackcompose.data.model.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val authApiService = RetrofitClient.authApiService

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Login failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        username: String,
        password: String
    ): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(
                    RegisterRequest(fullName, email, username, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Đăng ký thất bại: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
