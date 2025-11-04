package com.example.bookingcourt.domain.model

/**
 * Data class to hold booking information during the booking flow
 * This is used to pass data between screens before creating the final Booking
 */
data class BookingData(
    val id: Long? = null, // Backend booking ID (from POST /bookings)
    val courtId: String,
    val courtName: String,
    val courtAddress: String,
    val selectedDate: String,
    val selectedSlots: Set<CourtTimeSlot>,
    val playerName: String,
    val phoneNumber: String,
    val pricePerHour: Long,
    val totalPrice: Long,
    val ownerBankInfo: BankInfo? = null,  // Thông tin ngân hàng của chủ sân
    val expireTime: String? = null        // Thời gian hết hạn thanh toán
)

data class CourtTimeSlot(
    val courtNumber: Int,
    val timeSlot: String
)
