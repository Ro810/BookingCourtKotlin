package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalTime

/**
 * COURT MODEL - Sân cụ thể trong một Venue
 * Theo API: Court chỉ có id, description, booked và venues
 */
data class CourtDetail(
    val id: Long,
    val description: String,
    val booked: Boolean,
    val venue: CourtVenueInfo
)

data class CourtVenueInfo(
    val id: Long,
    val name: String
)

/**
 * VENUE MODEL (đã được thiết kế nhầm thành Court)
 * Model này thực ra là VENUE, không phải Court
 * ⚠️ DEPRECATED: Nên sử dụng Venue.kt thay vì model này
 * Giữ lại để tương thích với code cũ
 */
@Deprecated("Model này thực ra là Venue, nên dùng Venue.kt thay thế")
data class Court(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String>,
    val sportType: SportType,
    val courtType: CourtType,
    val pricePerHour: Long,
    val openTime: LocalTime,
    val closeTime: LocalTime,
    val amenities: List<Amenity>,
    val rules: String?,
    val ownerId: String,
    val rating: Float,
    val totalReviews: Int,
    val isActive: Boolean,
    val maxPlayers: Int,
    val courtsCount: Int = 1, // Số lượng sân con trong venue này
)

enum class SportType {
    BADMINTON,
    TABLE_TENNIS,
    TENNIS,
    FOOTBALL,
    BASKETBALL,
    VOLLEYBALL,
}

enum class CourtType {
    INDOOR,
    OUTDOOR,
    COVERED,
}

data class Amenity(
    val id: String,
    val name: String,
    val icon: String,
)
