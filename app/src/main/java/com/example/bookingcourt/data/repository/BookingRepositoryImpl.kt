package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.BookingApi
import com.example.bookingcourt.data.remote.dto.*
import com.example.bookingcourt.domain.model.*
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val bookingApi: BookingApi
) : BookingRepository {

    override suspend fun createBooking(
        courtId: String,
        startTime: String,
        endTime: String,
        notes: String?,
        paymentMethod: String
    ): Flow<Resource<BookingWithBankInfo>> = flow {
        emit(Resource.Loading())
        try {
            // Parse courtId format: "venueId_courtNumber"
            // VD: "5_1" -> venueId=5, courtId=1
            val parts = courtId.split("_")
            val venueIdLong = parts.getOrNull(0)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid venueId in courtId: $courtId")
            val courtIdLong = parts.getOrNull(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid courtId format: $courtId. Expected format: venueId_courtNumber")

            // ✅ Theo API document, chỉ cần 4 fields: venueId, courtId, startTime, endTime
            val request = CreateBookingRequestDto(
                venueId = venueIdLong,
                courtId = courtIdLong,
                startTime = startTime,
                endTime = endTime
            )

            // Log request để debug
            Log.d("BookingRepo", "Creating booking with:")
            Log.d("BookingRepo", "  venueId: $venueIdLong")
            Log.d("BookingRepo", "  courtId: $courtIdLong (parsed from: $courtId)")
            Log.d("BookingRepo", "  startTime: $startTime")
            Log.d("BookingRepo", "  endTime: $endTime")

            val apiResponse = bookingApi.createBooking(request)

            // Lấy data từ wrapper response
            val response = apiResponse.data

            Log.d("BookingRepo", "Booking created successfully: ${response.id}")
            Log.d("BookingRepo", "API message: ${apiResponse.message}")

            val bookingWithBankInfo = response.toBookingWithBankInfo()
            emit(Resource.Success(bookingWithBankInfo))
        } catch (e: IllegalArgumentException) {
            // Lỗi parse courtId
            Log.e("BookingRepo", "Invalid courtId format", e)
            emit(Resource.Error("Lỗi: ${e.message}"))
        } catch (e: Exception) {
            // Log chi tiết lỗi
            Log.e("BookingRepo", "Error creating booking", e)
            Log.e("BookingRepo", "Error message: ${e.message}")
            Log.e("BookingRepo", "Error cause: ${e.cause}")

            val errorMessage = when {
                e.message?.contains("401") == true -> "Vui lòng đăng nhập lại"
                e.message?.contains("404") == true -> "Không tìm thấy sân. Vui lòng thử lại."
                e.message?.contains("400") == true -> "Thông tin đặt sân không hợp lệ"
                e.message?.contains("timeout") == true -> "Kết nối tới server bị timeout"
                else -> "Lỗi: ${e.message ?: "Không xác định"}"
            }

            emit(Resource.Error(errorMessage))
        }
    }

    override suspend fun getUserBookings(
        page: Int,
        size: Int,
        status: String?
    ): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getUserBookings(page, size, status)
            val bookings = response.bookings.map { it.toBooking() }
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách booking"))
        }
    }

    override suspend fun getBookingById(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getBookingById(bookingId)
            val booking = response.toBooking()
            emit(Resource.Success(booking))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi lấy chi tiết booking"))
        }
    }

    override suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            bookingApi.cancelBooking(bookingId, mapOf("reason" to reason))
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi hủy booking"))
        }
    }

    override suspend fun confirmBooking(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.confirmBooking(bookingId)
            val booking = response.toBooking()
            emit(Resource.Success(booking))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi xác nhận booking"))
        }
    }

    override suspend fun getUpcomingBookings(): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getUpcomingBookings()
            val bookings = response.map { it.toBooking() }
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi lấy booking sắp tới"))
        }
    }
}

// Mapper functions
private fun CreateBookingResponseDto.toBookingWithBankInfo(): BookingWithBankInfo {
    // Helper function để parse time với xử lý lỗi
    fun parseDateTime(timeString: String?): LocalDateTime {
        if (timeString.isNullOrBlank()) {
            throw IllegalArgumentException("Time string is null or empty")
        }
        return try {
            // Xử lý format có microseconds: 2025-11-03T23:00:09.5733903
            // LocalDateTime.parse chỉ hỗ trợ format chuẩn ISO-8601
            val cleanedTime = if (timeString.contains('.')) {
                // Cắt bỏ phần microseconds, chỉ giữ lại đến giây
                timeString.substringBefore('.')
            } else {
                timeString
            }
            LocalDateTime.parse(cleanedTime)
        } catch (e: Exception) {
            Log.e("BookingMapper", "Error parsing time: $timeString", e)
            throw IllegalArgumentException("Invalid time format: $timeString")
        }
    }

    return BookingWithBankInfo(
        id = this.id.toString(),
        user = BookingUserInfo(
            id = this.userId.toString(),
            fullname = this.userName?.takeIf { it.isNotBlank() } ?: "Người dùng",
            phone = null
        ),
        court = BookingCourtInfo(
            id = this.courtId.toString(),
            description = this.courtName?.takeIf { it.isNotBlank() } ?: "Sân"
        ),
        venue = BookingVenueInfo(
            id = "0",
            name = this.venuesName?.takeIf { it.isNotBlank() } ?: "Venue"
        ),
        startTime = parseDateTime(this.startTime),
        endTime = parseDateTime(this.endTime),
        totalPrice = this.totalPrice.toLong(),
        status = when (this.status.uppercase()) {
            "PENDING_PAYMENT" -> BookingStatus.PENDING
            "CONFIRMED" -> BookingStatus.CONFIRMED
            "CANCELLED" -> BookingStatus.CANCELLED
            "COMPLETED" -> BookingStatus.COMPLETED
            "NO_SHOW" -> BookingStatus.NO_SHOW
            else -> BookingStatus.PENDING
        },
        expireTime = parseDateTime(this.expireTime),
        ownerBankInfo = this.ownerBankInfo.toBankInfo(),
        notes = null
    )
}

private fun BankInfoDto.toBankInfo(): BankInfo {
    return BankInfo(
        bankName = this.bankName,
        bankAccountNumber = this.bankAccountNumber,
        bankAccountName = this.bankAccountName
    )
}

private fun BookingDto.toBooking(): Booking {
    return Booking(
        id = this.id,
        courtId = this.courtId,
        courtName = this.courtName,
        userId = this.userId,
        userName = this.userName,
        userPhone = this.userPhone,
        startTime = LocalDateTime.parse(this.startTime),
        endTime = LocalDateTime.parse(this.endTime),
        totalPrice = this.totalPrice,
        status = BookingStatus.valueOf(this.status.uppercase()),
        paymentStatus = PaymentStatus.valueOf(this.paymentStatus.uppercase()),
        paymentMethod = this.paymentMethod?.let { PaymentMethod.valueOf(it.uppercase()) },
        notes = this.notes,
        createdAt = LocalDateTime.parse(this.createdAt),
        updatedAt = LocalDateTime.parse(this.updatedAt),
        cancellationReason = this.cancellationReason,
        qrCode = this.qrCode
    )
}
