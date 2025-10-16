package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean,
    val createdAt: LocalDateTime,
)

enum class NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    BOOKING_REMINDER,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PROMOTION,
    SYSTEM,
}
