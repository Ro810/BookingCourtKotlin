package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request DTO for updating a price rule (PUT /api/pricerules/{id})
 * Fields are minimal and can be extended to match backend contract.
 */
data class UpdatePriceRuleRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("startTime")
    val startTime: String? = null,

    @SerializedName("endTime")
    val endTime: String? = null,

    @SerializedName("price")
    val price: Double,

    @SerializedName("isActive")
    val isActive: Boolean = true
)

