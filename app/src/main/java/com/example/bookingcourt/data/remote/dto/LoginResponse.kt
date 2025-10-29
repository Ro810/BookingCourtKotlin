package com.example.bookingcourt.data.remote.dto

import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Clock
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
        return data?.let { loginData ->
            User(
                id = loginData.id.toString(),
                email = "",  // Backend không trả email trong login response
                fullName = loginData.phone,  // Sử dụng phone làm tên hiển thị tạm thời
                phoneNumber = loginData.phone,
                avatar = null,
                role = parseRole(loginData.roles),
                isVerified = true,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                bankName = null,
                bankAccountNumber = null,
                bankAccountName = null,
            )
        }
    }

    private fun parseRole(roles: List<String>): UserRole {
        val role = roles.firstOrNull()?.uppercase() ?: "ROLE_USER"
        return when {
            role.contains("ADMIN") -> UserRole.ADMIN
            role.contains("OWNER") -> UserRole.OWNER
            else -> UserRole.USER
        }
    }
}

data class LoginData(
    @SerializedName("jwtToken")
    val token: String,  // Map jwtToken to token for compatibility
    @SerializedName("id")
    val id: Long,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("roles")
    val roles: List<String>,
)
