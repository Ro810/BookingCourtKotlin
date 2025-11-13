package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("fullname")
    val fullname: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("bankName")
    val bankName: String? = null,
    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String? = null,
    @SerializedName("bankAccountName")
    val bankAccountName: String? = null,
)

