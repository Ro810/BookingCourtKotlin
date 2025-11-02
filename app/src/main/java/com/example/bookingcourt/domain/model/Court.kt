package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalTime

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
    val courtsCount: Int = 1, // Số lượng sân từ API
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
