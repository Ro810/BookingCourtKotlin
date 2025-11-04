package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * BookingResponseDto - Matches backend BookingResponse.java
 * Backend fields: id, userId, userName, courtId, courtName, venuesName,
 * startTime, endTime, totalPrice, status, expireTime, paymentProofUploaded,
 * paymentProofUrl, paymentProofUploadedAt, rejectionReason, ownerBankInfo
 */
data class BookingResponseDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("userName")
    val userName: String?,

    @SerializedName("courtId")
    val courtId: Long,

    @SerializedName("courtName")
    val courtName: String?,

    @SerializedName("venuesName")
    val venuesName: String?,

    @SerializedName("startTime")
    val startTime: String,  // LocalDateTime from backend as ISO string

    @SerializedName("endTime")
    val endTime: String,  // LocalDateTime from backend as ISO string

    @SerializedName("totalPrice")
    val totalPrice: Double,

    @SerializedName("status")
    val status: String,  // BookingStatus enum: PENDING_PAYMENT, PENDING_CONFIRMATION, CONFIRMED, REJECTED, CANCELLED, EXPIRED

    @SerializedName("expireTime")
    val expireTime: String?,  // LocalDateTime - when payment expires

    @SerializedName("paymentProofUploaded")
    val paymentProofUploaded: Boolean?,

    @SerializedName("paymentProofUrl")
    val paymentProofUrl: String?,

    @SerializedName("paymentProofUploadedAt")
    val paymentProofUploadedAt: String?,  // LocalDateTime

    @SerializedName("rejectionReason")
    val rejectionReason: String?,

    @SerializedName("ownerBankInfo")
    val ownerBankInfo: OwnerBankInfoDto?
)

/**
 * OwnerBankInfoDTO - Thông tin ngân hàng của chủ sân
 * Matches backend OwnerBankInfoDTO.java
 */
data class OwnerBankInfoDto(
    @SerializedName("bankName")
    val bankName: String?,

    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String?,

    @SerializedName("bankAccountName")
    val bankAccountName: String?
)

/**
 * PaymentProofRequestDto - Request for confirming payment
 * Matches backend PaymentProofRequest.java
 */
data class PaymentProofRequestDto(
    @SerializedName("paymentProofUrl")
    val paymentProofUrl: String
)

