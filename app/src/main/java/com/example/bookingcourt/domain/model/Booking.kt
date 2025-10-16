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
