package com.example.bookingcourt.domain.model

/**
 * Data class to hold booking information during the booking flow
 * This is used to pass data between screens before creating the final Booking
 */
data class BookingData(
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
    val expireTime: String? = null,       // Thời gian hết hạn thanh toán
    val startTime: String = "",           // Thời gian bắt đầu cho API (format: "2025-10-28T10:00:00")
    val endTime: String = ""              // Thời gian kết thúc cho API (format: "2025-10-28T11:00:00")
)

data class CourtTimeSlot(
    val courtNumber: Int,
    val timeSlot: String
)
