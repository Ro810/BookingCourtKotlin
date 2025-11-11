package com.example.bookingcourt.domain.model

/**
 * Item đặt sân - đại diện cho 1 sân với khung giờ cụ thể
 */
data class BookingItemData(
    val courtId: String,      // Format: "venueId_courtId" (e.g., "1_3")
    val courtName: String,    // Tên sân (e.g., "Sân số 3")
    val startTime: String,    // Thời gian bắt đầu (format: "2025-11-11T21:30:00")
    val endTime: String,      // Thời gian kết thúc (format: "2025-11-11T22:00:00")
    val price: Long           // Giá sân này
)

/**
 * Data class to hold booking information during the booking flow
 * This is used to pass data between screens before creating the final Booking
 */
data class BookingData(
    val courtId: String,       // ⚠️ LEGACY: Sân đầu tiên (để backward compatible)
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
    val startTime: String = "",           // ⚠️ LEGACY: Thời gian bắt đầu sân đầu tiên
    val endTime: String = "",             // ⚠️ LEGACY: Thời gian kết thúc sân đầu tiên

    // ✅ NEW: Danh sách các sân đã đặt (hỗ trợ nhiều sân)
    val bookingItems: List<BookingItemData>? = null
)

data class CourtTimeSlot(
    val courtNumber: Int,
    val timeSlot: String
)
