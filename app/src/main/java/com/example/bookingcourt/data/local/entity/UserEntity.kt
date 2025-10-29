package com.example.bookingcourt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "avatar")
    val avatar: String?,
    @ColumnInfo(name = "role")
    val role: String,
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "favorite_court_ids")
    val favoriteCourtIds: List<String>,
    @ColumnInfo(name = "playing_level")
    val playingLevel: String?,
    @ColumnInfo(name = "preferred_sports")
    val preferredSports: List<String>,
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,
    @ColumnInfo(name = "bank_account_number")
    val bankAccountNumber: String? = null,
    @ColumnInfo(name = "bank_account_name")
    val bankAccountName: String? = null,
)
