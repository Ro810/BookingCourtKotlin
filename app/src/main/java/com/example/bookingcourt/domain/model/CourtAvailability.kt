package com.example.bookingcourt.domain.model

/**
 * Domain model cho Court Availability
 * Thông tin về tình trạng sân và các time slots đã được đặt
 */
data class CourtAvailability(
    val courtId: Long,
    val courtName: String, // VD: "Sân số 1"
    val available: Boolean,
    val bookedSlots: List<BookedSlotInfo> = emptyList()
)

/**
 * Thông tin về time slot đã được đặt
 */
data class BookedSlotInfo(
    val startTime: String, // Format: "HH:mm:ss"
    val endTime: String, // Format: "HH:mm:ss"
    val bookingId: Long
)
