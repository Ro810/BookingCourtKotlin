package com.example.bookingcourt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courts")
data class CourtEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "images")
    val images: List<String>,
    @ColumnInfo(name = "sport_type")
    val sportType: String,
    @ColumnInfo(name = "court_type")
    val courtType: String,
    @ColumnInfo(name = "price_per_hour")
    val pricePerHour: Long,
    @ColumnInfo(name = "open_time")
    val openTime: String,
    @ColumnInfo(name = "close_time")
    val closeTime: String,
    @ColumnInfo(name = "amenities")
    val amenities: String,
    @ColumnInfo(name = "rules")
    val rules: String?,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "rating")
    val rating: Float,
    @ColumnInfo(name = "total_reviews")
    val totalReviews: Int,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "max_players")
    val maxPlayers: Int,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
)
