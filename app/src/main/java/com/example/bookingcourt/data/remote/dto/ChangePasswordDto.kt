package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request DTO
data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String
)

// Response DTO
data class ChangePasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
)

