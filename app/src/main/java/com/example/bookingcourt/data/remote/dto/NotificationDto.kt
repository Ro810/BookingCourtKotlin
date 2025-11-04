package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * NotificationDto - Matches backend NotificationDTO.java
 * Response from Notification APIs
 */
data class NotificationDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("bookingId")
    val bookingId: Long?,

    @SerializedName("type")
    val type: String,  // NotificationType enum: BOOKING_CREATED, BOOKING_CONFIRMED, BOOKING_REJECTED, PAYMENT_RECEIVED, BOOKING_CANCELLED

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,  // Instant from backend as ISO string

    @SerializedName("senderName")
    val senderName: String?  // Tên người gửi (nếu có)
)

/**
 * NotificationType enum matching backend
 */
enum class NotificationType {
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    BOOKING_REJECTED,
    PAYMENT_RECEIVED,
    BOOKING_CANCELLED,
    BOOKING_EXPIRED
}

