package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class Review(
    val id: String,
    val courtId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val rating: Int,
    val comment: String,
    val images: List<String> = emptyList(),
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isVerifiedBooking: Boolean,
    val bookingId: String, // Cần để có thể update review (delete + create)
)
