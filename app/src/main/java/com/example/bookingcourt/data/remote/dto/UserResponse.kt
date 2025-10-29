package com.example.bookingcourt.data.remote.dto

import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class UserResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: UserData? = null,
) {
    fun toUser(): User? {
        return data?.let { userData ->
            User(
                id = userData.id.toString(),
                email = userData.email ?: "",
                fullName = userData.fullname ?: userData.phone,
                phoneNumber = userData.phone,
                avatar = userData.avatar,
                role = parseRole(userData.roles),
                isVerified = true,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                bankName = userData.bankName,
                bankAccountNumber = userData.bankAccountNumber,
                bankAccountName = userData.bankAccountName,
            )
        }
    }

    private fun parseRole(roles: List<String>?): UserRole {
        val role = roles?.firstOrNull()?.uppercase() ?: "ROLE_USER"
        return when {
            role.contains("ADMIN") -> UserRole.ADMIN
            role.contains("OWNER") -> UserRole.OWNER
            else -> UserRole.USER
        }
    }
}

data class UserData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("fullname")
    val fullname: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("roles")
    val roles: List<String>? = null,
    @SerializedName("bankName")
    val bankName: String? = null,
    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String? = null,
    @SerializedName("bankAccountName")
    val bankAccountName: String? = null,
)
