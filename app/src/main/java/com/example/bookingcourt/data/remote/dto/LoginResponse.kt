package com.example.bookingcourt.data.remote.dto

import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: LoginData? = null,
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

data class LoginData(
    @SerializedName("user")
    val user: UserResponse,
    @SerializedName("token")
    val token: String,
)

data class UserResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("role")
    val role: String,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("isActive")
    val isActive: Boolean,
)
