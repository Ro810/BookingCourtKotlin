package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Court DTO from backend API
 * Endpoint: GET /api/courts
 *
 * Một Court (sân con) thuộc về một Venue (cơ sở)
 * Backend trả về thông tin Venue cha trong field "venues"
 */
data class CourtDetailDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("description")
    val description: String,

    @SerializedName("booked")
    val booked: Boolean? = null, // Optional, không phải tất cả endpoint đều trả về

    @SerializedName("venues")
    val venue: VenueSimpleDto // Thông tin Venue cha
)

/**
 * Simplified Venue info in Court response
 * Chứa thông tin cơ bản của Venue cha khi query Court
 */
data class VenueSimpleDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("numberOfCourt")
    val numberOfCourt: Int? = null // Optional, chỉ có khi create/update court
)
