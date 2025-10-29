package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Standard error response from backend API
 */
data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null,

    @SerializedName("statusCode")
    val statusCode: Int? = null
)

