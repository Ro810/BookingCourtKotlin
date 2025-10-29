package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("phone")
    val username: String,  // Parameter name vẫn là username để giữ UI compatibility
    @SerializedName("password")
    val password: String,
)
