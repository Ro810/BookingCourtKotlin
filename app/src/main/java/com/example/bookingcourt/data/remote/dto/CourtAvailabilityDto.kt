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
 * Format mới: data là object chứa thông tin venue và array courts
 */
data class CourtAvailabilityResponseDto(
    @SerializedName("venueName")
    val venueName: String,

    @SerializedName("venueId")
    val venueId: Long,

    @SerializedName("openingTime")
    val openingTime: List<Int>?, // [hour, minute]

    @SerializedName("closingTime")
    val closingTime: List<Int>?, // [hour, minute]

    @SerializedName("pricePerHour")
    val pricePerHour: Double,

    @SerializedName("courts")
    val courts: List<CourtAvailabilityDto>,

    @SerializedName("totalCourts")
    val totalCourts: Int,

    @SerializedName("availableCourts")
    val availableCourts: Int
)

/**
 * DTO cho mỗi court trong danh sách courts
 */
data class CourtAvailabilityDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("description")
    val description: String, // VD: "Sân số 1"

    @SerializedName("available")
    val available: Boolean,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("bookedSlots")
    val bookedSlots: List<BookedSlotInfoDto>? = null
)

/**
 * DTO cho thông tin booked slot trong response availability
 * API trả về startTime/endTime dưới dạng array: [year, month, day, hour, minute]
 */
data class BookedSlotInfoDto(
    @SerializedName("startTime")
    val startTime: List<Int>, // Format: [2025, 11, 7, 14, 0]

    @SerializedName("endTime")
    val endTime: List<Int>, // Format: [2025, 11, 7, 15, 0]

    @SerializedName("bookingId")
    val bookingId: Long
) {
    /**
     * Convert array time to string format HH:mm:ss
     */
    fun getStartTimeString(): String {
        if (startTime.size < 5) return "00:00:00"
        return String.format("%02d:%02d:00", startTime[3], startTime[4])
    }

    fun getEndTimeString(): String {
        if (endTime.size < 5) return "00:00:00"
        return String.format("%02d:%02d:00", endTime[3], endTime[4])
    }
}
