package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T, // Non-null vì response thành công luôn có data

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)
