package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 * Matches backend ApiResponse.java structure
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null, // Nullable vì backend có thể trả null khi lỗi

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)
