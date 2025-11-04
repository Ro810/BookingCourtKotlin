package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class Booking(
    val id: String,
    val courtId: String,
    val courtName: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val status: BookingStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val cancellationReason: String?,
    val qrCode: String?,
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW,
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED,
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    E_WALLET,
    CREDIT_CARD,
}

/**
 * Response model khi tạo booking mới - bao gồm thông tin ngân hàng của chủ sân
 */
data class BookingWithBankInfo(
    val id: String,
    val user: BookingUserInfo,
    val court: BookingCourtInfo,
    val venue: BookingVenueInfo,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val status: BookingStatus,
    val expireTime: LocalDateTime,
    val ownerBankInfo: BankInfo,
    val notes: String?
)

data class BookingUserInfo(
    val id: String,
    val fullname: String,
    val phone: String? = null // Cho phép null vì API không luôn trả về
)

data class BookingCourtInfo(
    val id: String,
    val description: String // Giữ non-null, sẽ dùng default value nếu null
)

data class BookingVenueInfo(
    val id: String,
    val name: String // Giữ non-null, sẽ dùng default value nếu null
)
