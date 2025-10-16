package com.example.bookingcourt.data.remote.dto

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
    @SerializedName("court_id")
    val courtId: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("payment_method")
    val paymentMethod: String,
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
