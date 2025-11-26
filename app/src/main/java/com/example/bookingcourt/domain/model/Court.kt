package com.example.bookingcourt.domain.model

/**
 * Court Model - Sân cụ thể trong một Venue
 * Theo API: Court chỉ có id, description, booked và venues
 */
data class CourtDetail(
    val id: Long,
    val description: String,
    val booked: Boolean = false,
    val isActive: Boolean = true, // Trạng thái khóa/mở sân (true = mở, false = khóa)
    val venue: CourtVenueInfo
)

data class CourtVenueInfo(
    val id: Long,
    val name: String
)
