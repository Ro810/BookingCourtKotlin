package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CourtDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("sport_type")
    val sportType: String,
    @SerializedName("court_type")
    val courtType: String,
    @SerializedName("price_per_hour")
    val pricePerHour: Long,
    @SerializedName("open_time")
    val openTime: String,
    @SerializedName("close_time")
    val closeTime: String,
    @SerializedName("amenities")
    val amenities: List<AmenityDto>,
    @SerializedName("rules")
    val rules: String?,
    @SerializedName("owner_id")
    val ownerId: String,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("total_reviews")
    val totalReviews: Int,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("max_players")
    val maxPlayers: Int,
)

data class AmenityDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("icon")
    val icon: String,
)

data class CourtListResponseDto(
    @SerializedName("courts")
    val courts: List<CourtDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
)
