package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Review DTO - Response from API
 * Matches API documentation from `/venues/{venueId}/reviews`
 */
data class ReviewDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("userFullname")
    val userFullname: String,

    @SerializedName("venueId")
    val venueId: Long,

    @SerializedName("venueName")
    val venueName: String,

    @SerializedName("bookingId")
    val bookingId: Long,

    @SerializedName("rating")
    val rating: Int, // 1-5

    @SerializedName("comment")
    val comment: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Create Review Request
 * POST /bookings/{bookingId}/review
 */
data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int, // Required, 1-5

    @SerializedName("comment")
    val comment: String? // Optional
)

