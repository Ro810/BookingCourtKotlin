package com.example.bookingcourt.data.remote.dto

import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: RegisterData? = null,
) {
    fun toUser(): User? {
        return data?.user?.let { userResponse ->
            User(
                id = userResponse.id,
                email = userResponse.email,
                fullName = userResponse.fullName,
                phoneNumber = userResponse.phone,
                avatar = userResponse.avatar,
                role = when (userResponse.role.uppercase()) {
                    "ADMIN" -> UserRole.ADMIN
                    "COURT_OWNER" -> UserRole.COURT_OWNER
                    else -> UserRole.CUSTOMER
                },
                isVerified = userResponse.isActive,
                createdAt = Instant.fromEpochMilliseconds(userResponse.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Instant.fromEpochMilliseconds(userResponse.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
            )
        }
    }
}

data class RegisterData(
    @SerializedName("user")
    val user: UserResponse,
    @SerializedName("token")
    val token: String,
)
