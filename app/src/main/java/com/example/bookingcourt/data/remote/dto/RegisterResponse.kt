package com.example.bookingcourt.data.remote.dto

import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: String? = null,  // Backend trả String, không phải object
) {
    // Backend không trả user info sau register, return null
    fun toUser(): User? = null
}

// Không dùng nữa - backend không trả user info
/*
data class RegisterData(
    @SerializedName("user")
    val user: UserResponse,
    @SerializedName("token")
    val token: String,
)
*/
