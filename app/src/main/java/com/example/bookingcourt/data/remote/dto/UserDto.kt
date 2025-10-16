package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("role")
    val role: String,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("favorite_court_ids")
    val favoriteCourtIds: List<String>?,
    @SerializedName("playing_level")
    val playingLevel: String?,
    @SerializedName("preferred_sports")
    val preferredSports: List<String>?,
)
