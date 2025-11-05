package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO cho thông tin về các time slots đã được đặt
 */
data class BookedSlotDto(
    @SerializedName("courtId")
    val courtId: Long,

    @SerializedName("courtNumber")
    val courtNumber: Int,

    @SerializedName("startTime")
    val startTime: String, // Format: "2025-11-05T10:00:00"

    @SerializedName("endTime")
    val endTime: String, // Format: "2025-11-05T11:00:00"

    @SerializedName("status")
    val status: String, // PENDING_PAYMENT, PAYMENT_UPLOADED, CONFIRMED

    @SerializedName("bookingId")
    val bookingId: String
)

/**
 * DTO cho API GET /venues/{venueId}/courts/availability
 * Response từ backend cho thông tin availability của các courts
 */
data class CourtAvailabilityDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("description")
    val description: String, // VD: "Sân số 1"

    @SerializedName("available")
    val available: Boolean,

    @SerializedName("bookedSlots")
    val bookedSlots: List<BookedSlotInfoDto>? = null
)

/**
 * DTO cho thông tin booked slot trong response availability
 */
data class BookedSlotInfoDto(
    @SerializedName("startTime")
    val startTime: String, // Format: "2025-11-05T10:00:00"

    @SerializedName("endTime")
    val endTime: String, // Format: "2025-11-05T10:30:00"

    @SerializedName("bookingId")
    val bookingId: String
)
