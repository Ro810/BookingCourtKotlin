package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * CourtAvailabilityResponse - Response from checkAvailability API
 * Backend: CourtController.checkAvailability() returns Map<String, Object>
 */
data class CourtAvailabilityResponse(
    @SerializedName("courtId")
    val courtId: Long,

    @SerializedName("available")
    val available: Boolean,

    @SerializedName("bookedSlots")
    val bookedSlots: List<BookedSlot>
)

/**
 * BookedSlot - Thông tin về slot đã được đặt
 */
data class BookedSlot(
    @SerializedName("startTime")
    val startTime: String,  // LocalDateTime as ISO string

    @SerializedName("endTime")
    val endTime: String,  // LocalDateTime as ISO string

    @SerializedName("bookingId")
    val bookingId: Long
)

/**
 * CourtRequest - Request to create a new court
 * Matches backend CourtRequest.java
 */
data class CourtRequest(
    @SerializedName("venueId")
    val venueId: Long,

    @SerializedName("description")
    val description: String?
)

