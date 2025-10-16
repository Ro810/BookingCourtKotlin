package com.example.jatpackcompose.data.api

import com.example.jatpackcompose.data.model.LoginRequest
import com.example.jatpackcompose.data.model.LoginResponse
import com.example.jatpackcompose.data.model.RegisterRequest
import com.example.jatpackcompose.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
}
