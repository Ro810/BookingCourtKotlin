package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * ReviewDto - Matches backend ReviewDTO.java
 * Response from Review APIs
 */
data class ReviewDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("userFullname")
    val userFullname: String?,

    @SerializedName("venueId")
    val venueId: Long,

    @SerializedName("venueName")
    val venueName: String?,

    @SerializedName("bookingId")
    val bookingId: Long,

    @SerializedName("rating")
    val rating: Int,  // 1-5 stars

    @SerializedName("comment")
    val comment: String?,

    @SerializedName("createdAt")
    val createdAt: String,  // Instant from backend as ISO string

    @SerializedName("updatedAt")
    val updatedAt: String?  // Instant from backend as ISO string
)

/**
 * ReviewRequest - Matches backend ReviewRequest.java
 * Request to create a new review
 */
data class ReviewRequest(
    @SerializedName("rating")
    val rating: Int,  // 1-5, required

    @SerializedName("comment")
    val comment: String?  // Optional
)

