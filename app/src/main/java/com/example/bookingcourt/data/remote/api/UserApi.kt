package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.UserResponse
import com.example.bookingcourt.data.remote.dto.UpdateUserRequest
import com.example.bookingcourt.data.remote.dto.BaseResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UserResponse>

    @POST("users/me/request-owner-role")
    suspend fun requestOwnerRole(): Response<BaseResponseDto>

    @POST("users/me/switch-to-user-role")
    suspend fun switchToUserRole(): Response<BaseResponseDto>
}
