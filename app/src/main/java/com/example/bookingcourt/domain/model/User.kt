package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val avatar: String?,
    val role: UserRole,
    val isVerified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val favoriteCourtIds: List<String> = emptyList(),
    val playingLevel: PlayingLevel? = null,
    val preferredSports: List<SportType> = emptyList(),
)

enum class UserRole {
    USER,        // ROLE_USER từ backend
    OWNER,       // ROLE_OWNER từ backend
    ADMIN,       // ROLE_ADMIN từ backend
}

enum class PlayingLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    PROFESSIONAL,
}
