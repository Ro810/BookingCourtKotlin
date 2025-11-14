package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class BookingDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("court_id")
    val courtId: String,
    @SerializedName("court_name")
    val courtName: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_phone")
    val userPhone: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("total_price")
    val totalPrice: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("cancellation_reason")
    val cancellationReason: String?,
    @SerializedName("qr_code")
    val qrCode: String?,
)

data class CreateBookingRequestDto(
    @SerializedName("venueId")
    val venueId: Long,

    // ✨ NEW: Dùng khi đặt nhiều sân (RECOMMENDED)
    @SerializedName("bookingItems")
    val bookingItems: List<BookingItemRequestDto>? = null,

    // 🔄 LEGACY: Dùng khi đặt 1 sân (backward compatible)
    @SerializedName("courtId")
    val courtId: Long? = null,
    @SerializedName("startTime")
    val startTime: String? = null,
    @SerializedName("endTime")
    val endTime: String? = null
) {
    companion object {
        /**
         * ✨ Factory method: Tạo request cho NHIỀU SÂN (NEW API)
         */
        fun forMultipleCourts(
            venueId: Long,
            items: List<BookingItemRequestDto>
        ) = CreateBookingRequestDto(
            venueId = venueId,
            bookingItems = items
        )

        /**
         * 🔄 Factory method: Tạo request cho 1 SÂN (LEGACY - backward compatible)
         */
        fun forSingleCourt(
            venueId: Long,
            courtId: Long,
            startTime: String,
            endTime: String
        ) = CreateBookingRequestDto(
            venueId = venueId,
            courtId = courtId,
            startTime = startTime,
            endTime = endTime
        )
    }
}

data class BookingItemRequestDto(
    @SerializedName("courtId")
    val courtId: Long,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String
)

data class BookingListResponseDto(
    @SerializedName("bookings")
    val bookings: List<BookingDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
)

data class TimeSlotDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("court_id")
    val courtId: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("price")
    val price: Long,
    @SerializedName("discount")
    val discount: Float?,
    @SerializedName("booking_id")
    val bookingId: String?,
)

data class BankInfoDto(
    @SerializedName("bankName")
    val bankName: String,
    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String,
    @SerializedName("bankAccountName")
    val bankAccountName: String
)

/**
 * Response DTO khi tạo booking mới - có thêm thông tin ngân hàng của chủ sân
 * Theo API thực tế: POST /bookings trả về ownerBankInfo và expireTime
 * Response có cấu trúc flat, không có nested objects
 */
data class CreateBookingResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("userName")
    val userName: String?,

    // ✨ NEW: Danh sách sân đã đặt
    @SerializedName("bookingItems")
    val bookingItems: List<BookingItemResponseDto>? = null,

    // 🔄 LEGACY: Thông tin sân đơn (backward compatible)
    @SerializedName("courtId")
    val courtId: Long? = null,
    @SerializedName("courtName")
    val courtName: String?,
    @SerializedName("venueId")
    val venueId: Long?,
    @SerializedName("venuesName")
    val venuesName: String?,
    @SerializedName("startTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val startTime: String?,
    @SerializedName("endTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val endTime: String?,
    @SerializedName("totalPrice")
    val totalPrice: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("expireTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val expireTime: String?,
    @SerializedName("ownerBankInfo")
    val ownerBankInfo: BankInfoDto,
    @SerializedName("paymentProofUploaded")
    val paymentProofUploaded: Boolean? = null,
    @SerializedName("paymentProofUrl")
    val paymentProofUrl: String? = null,
    @SerializedName("paymentProofUploadedAt")
    @JsonAdapter(TimeStringDeserializer::class)
    val paymentProofUploadedAt: String? = null,
    @SerializedName("rejectionReason")
    val rejectionReason: String? = null
)

// Giữ lại các DTO cũ cho tương thích
data class UserInfoDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fullname")
    val fullname: String,
    @SerializedName("phone")
    val phone: String
)

data class CourtInfoDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("description")
    val description: String
)

data class VenueInfoDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

/**
 * Request DTO cho confirm payment
 */
data class ConfirmPaymentRequestDto(
    @SerializedName("paymentProofUrl")
    val paymentProofUrl: String
)

/**
 * Request DTO cho reject booking
 */
data class RejectBookingRequestDto(
    @SerializedName("rejectionReason")
    val rejectionReason: String
)

/**
 * Response DTO chi tiết booking - dùng cho cả GET /bookings/{id} và các API payment
 * ✅ UPDATED: Hỗ trợ bookingItems array cho nhiều sân
 */
data class BookingDetailResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("userName")
    val userName: String?,
    @SerializedName("userPhone")
    val userPhone: String?,

    // ✨ NEW: Danh sách sân đã đặt (có thể nhiều sân)
    @SerializedName("bookingItems")
    val bookingItems: List<BookingItemResponseDto>? = null,

    // 🔄 LEGACY: Thông tin sân đơn (backward compatible)
    @SerializedName("courtId")
    val courtId: Long? = null,
    @SerializedName("courtName")
    val courtName: String?,

    @SerializedName("venueId")
    val venueId: Long?,
    @SerializedName("venuesName")
    val venuesName: String?,

    // ✅ FIX: Venue address - đổi từ String sang AddressDto
    @SerializedName("venueAddress")
    val venueAddress: AddressDto? = null,

    // 🔄 HOẶC: Backend có thể trả về venue object đầy đủ
    @SerializedName("venue")
    val venue: VenueDetailDto? = null,

    @SerializedName("startTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val startTime: String?, // Nullable và có thể là string hoặc array
    @SerializedName("endTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val endTime: String?, // Nullable và có thể là string hoặc array
    @SerializedName("totalPrice")
    val totalPrice: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("paymentProofUploaded")
    val paymentProofUploaded: Boolean,
    @SerializedName("paymentProofUrl")
    val paymentProofUrl: String?,
    @SerializedName("paymentProofUploadedAt")
    @JsonAdapter(TimeStringDeserializer::class)
    val paymentProofUploadedAt: String?,
    @SerializedName("rejectionReason")
    val rejectionReason: String?,
    @SerializedName("expireTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val expireTime: String?, // Nullable và có thể là string hoặc array
    @SerializedName("ownerBankInfo")
    val ownerBankInfo: BankInfoDto?
)

/**
 * ✨ NEW: Booking Item trong response (thông tin mỗi sân đã đặt)
 */
data class BookingItemResponseDto(
    @SerializedName("courtId")
    val courtId: Long,
    @SerializedName("courtName")
    val courtName: String?,
    @SerializedName("startTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val startTime: String?,
    @SerializedName("endTime")
    @JsonAdapter(TimeStringDeserializer::class)
    val endTime: String?,
    @SerializedName("price")
    val price: Double
)
