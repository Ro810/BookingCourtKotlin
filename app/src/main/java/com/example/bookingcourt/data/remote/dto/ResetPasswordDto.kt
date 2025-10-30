package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("newPassword")
    val newPassword: String,
)

data class ResetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: String? = null,
)
