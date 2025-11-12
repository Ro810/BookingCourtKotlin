package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class Booking(
    val id: String,
    val courtId: String,
    val courtName: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val status: BookingStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val cancellationReason: String?,
    val qrCode: String?,
)

enum class BookingStatus {
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_UPLOADED,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED,
    NO_SHOW,
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED,
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    E_WALLET,
    CREDIT_CARD,
}

/**
 * ✨ NEW: Booking Item model - đại diện cho 1 sân trong booking
 */
data class BookingItem(
    val courtId: String,
    val courtName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val price: Long
)

/**
 * Thông tin user trong booking
 */
data class BookingUserInfo(
    val id: String,
    val fullname: String,
    val phone: String?
)

/**
 * Thông tin court trong booking
 */
data class BookingCourtInfo(
    val id: String,
    val description: String
)

/**
 * Thông tin venue trong booking
 */
data class BookingVenueInfo(
    val id: String,
    val name: String
)

/**
 * Response model khi tạo booking mới - bao gồm thông tin ngân hàng của chủ sân
 * ✅ UPDATED: Hỗ trợ bookingItems array cho nhiều sân
 */
data class BookingWithBankInfo(
    val id: String,
    val user: BookingUserInfo,

    // ✨ NEW: Danh sách sân đã đặt (có thể nhiều sân)
    val bookingItems: List<BookingItem>? = null,

    // 🔄 LEGACY: Thông tin sân đơn (backward compatible)
    val court: BookingCourtInfo? = null,

    val venue: BookingVenueInfo,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val status: BookingStatus,
    val expireTime: LocalDateTime,
    val ownerBankInfo: BankInfo,
    val notes: String?
) {
    /**
     * Helper: Lấy tên courts để hiển thị
     * - Nếu có bookingItems: "Sân 1, Sân 2, Sân 3"
     * - Nếu không: dùng court.description
     */
    fun getCourtsDisplayName(): String {
        return when {
            !bookingItems.isNullOrEmpty() -> {
                if (bookingItems.size == 1) {
                    bookingItems.first().courtName
                } else {
                    "${bookingItems.size} sân"
                }
            }
            court != null -> court.description
            else -> "Sân"
        }
    }

    /**
     * Helper: Số lượng sân đã đặt
     */
    fun getCourtsCount(): Int {
        return bookingItems?.size ?: 1
    }
}

/**
 * Model chi tiết booking cho payment flow
 * Bao gồm tất cả thông tin cần thiết cho user và owner
 * ✅ UPDATED: Hỗ trợ bookingItems array cho nhiều sân
 */
data class BookingDetail(
    val id: String,
    val user: BookingUserInfo,

    // ✨ NEW: Danh sách sân đã đặt (có thể nhiều sân)
    val bookingItems: List<BookingItem>? = null,

    // 🔄 LEGACY: Thông tin sân đơn (backward compatible)
    val court: BookingCourtInfo? = null,

    val venue: BookingVenueInfo,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val status: BookingStatus,
    val paymentProofUploaded: Boolean,
    val paymentProofUrl: String?,
    val paymentProofUploadedAt: String?,
    val rejectionReason: String?,
    val expireTime: LocalDateTime?,
    val ownerBankInfo: BankInfo?,
    val venueAddress: String
) {
    /**
     * Helper: Lấy tên courts để hiển thị
     * - Nếu có bookingItems: "Sân 1, Sân 2" hoặc "3 sân"
     * - Nếu không: dùng court.description
     */
    fun getCourtsDisplayName(): String {
        return when {
            !bookingItems.isNullOrEmpty() -> {
                if (bookingItems.size == 1) {
                    bookingItems.first().courtName
                } else {
                    bookingItems.joinToString(", ") { it.courtName }
                }
            }
            court != null -> court.description
            else -> "N/A"
        }
    }

    /**
     * Helper: Đếm số lượng sân
     */
    fun getCourtsCount(): Int {
        return bookingItems?.size ?: 1
    }
}
