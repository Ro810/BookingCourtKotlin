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

            // Backend only accepts venueId, courtId, startTime, endTime
            val request = CreateBookingRequestDto(
                courtId = courtIdLong,
                venueId = venueIdLong,
                startTime = startTime,
                endTime = endTime
            )

            // Log request để debug
            Log.d("BookingRepo", "Creating booking with:")
            Log.d("BookingRepo", "  courtId: $courtIdLong (parsed from: $courtId)")
            Log.d("BookingRepo", "  venueId: $venueIdLong")
            Log.d("BookingRepo", "  startTime: $startTime")
            Log.d("BookingRepo", "  endTime: $endTime")

            val response = bookingApi.createBooking(request)

            // Check if API call was successful
            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi tạo booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            Log.d("BookingRepo", "Booking created successfully: ${apiResponse.data.id}")
            Log.d("BookingRepo", "API message: ${apiResponse.message}")

            val bookingWithBankInfo = apiResponse.data.toBookingWithBankInfo()
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
                e.message?.contains("404") == true -> "Không tìm thấy sân"
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
            Log.d("BookingRepo", "Getting user bookings - page: $page, size: $size, status: $status")
            val response = bookingApi.getMyBookings()

            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi lấy danh sách booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            val bookings = apiResponse.data.map { it.toBooking() }
            Log.d("BookingRepo", "Got ${bookings.size} bookings")
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting user bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách booking"))
        }
    }

    override suspend fun getBookingById(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())
        try {
            val bookingIdLong = bookingId.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid bookingId: $bookingId")

            Log.d("BookingRepo", "Getting booking by id: $bookingIdLong")
            val response = bookingApi.getBookingById(bookingIdLong)

            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi lấy chi tiết booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            val booking = apiResponse.data.toBooking()
            Log.d("BookingRepo", "Got booking: ${booking.id}")
            emit(Resource.Success(booking))
        } catch (e: IllegalArgumentException) {
            Log.e("BookingRepo", "Invalid bookingId format", e)
            emit(Resource.Error("Lỗi: ${e.message}"))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting booking by id", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy chi tiết booking"))
        }
    }

    override suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val bookingIdLong = bookingId.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid bookingId: $bookingId")

            Log.d("BookingRepo", "Cancelling booking: $bookingIdLong, reason: $reason")
            val response = bookingApi.cancelBooking(bookingIdLong)

            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi hủy booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            Log.d("BookingRepo", "Booking cancelled successfully")
            emit(Resource.Success(Unit))
        } catch (e: IllegalArgumentException) {
            Log.e("BookingRepo", "Invalid bookingId format", e)
            emit(Resource.Error("Lỗi: ${e.message}"))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error cancelling booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi hủy booking"))
        }
    }

    override suspend fun confirmBooking(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())
        try {
            val bookingIdLong = bookingId.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid bookingId: $bookingId")

            Log.d("BookingRepo", "Confirming booking: $bookingIdLong")
            val response = bookingApi.acceptBooking(bookingIdLong)

            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi xác nhận booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            val booking = apiResponse.data.toBooking()
            Log.d("BookingRepo", "Booking confirmed successfully")
            emit(Resource.Success(booking))
        } catch (e: IllegalArgumentException) {
            Log.e("BookingRepo", "Invalid bookingId format", e)
            emit(Resource.Error("Lỗi: ${e.message}"))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error confirming booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi xác nhận booking"))
        }
    }

    override suspend fun getUpcomingBookings(): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("BookingRepo", "Getting upcoming bookings")
            val response = bookingApi.getMyBookings()

            if (!response.isSuccessful) {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("BookingRepo", "API error: $errorMsg")
                emit(Resource.Error("Lỗi lấy danh sách booking: $errorMsg"))
                return@flow
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success || apiResponse.data == null) {
                val message = apiResponse?.message ?: "Response body is null"
                Log.e("BookingRepo", "Invalid response: $message")
                emit(Resource.Error("Lỗi: $message"))
                return@flow
            }

            // Filter for upcoming bookings (CONFIRMED status and future start time)
            val bookings = apiResponse.data
                .filter { it.status.uppercase() == "CONFIRMED" || it.status.uppercase() == "PENDING_CONFIRMATION" }
                .map { it.toBooking() }

            Log.d("BookingRepo", "Got ${bookings.size} upcoming bookings")
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting upcoming bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy booking sắp tới"))
        }
    }
}

// Mapper functions
private fun BookingResponseDto.toBookingWithBankInfo(): BookingWithBankInfo {
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
            "PENDING_CONFIRMATION" -> BookingStatus.PENDING
            "CONFIRMED" -> BookingStatus.CONFIRMED
            "CANCELLED" -> BookingStatus.CANCELLED
            "REJECTED" -> BookingStatus.CANCELLED
            "EXPIRED" -> BookingStatus.CANCELLED
            "COMPLETED" -> BookingStatus.COMPLETED
            "NO_SHOW" -> BookingStatus.NO_SHOW
            else -> BookingStatus.PENDING
        },
        expireTime = parseDateTime(this.expireTime ?: this.endTime),
        ownerBankInfo = this.ownerBankInfo?.toBankInfo() ?: BankInfo(
            bankName = "N/A",
            bankAccountNumber = "N/A",
            bankAccountName = "N/A"
        ),
        notes = null
    )
}

private fun OwnerBankInfoDto.toBankInfo(): BankInfo {
    return BankInfo(
        bankName = this.bankName ?: "N/A",
        bankAccountNumber = this.bankAccountNumber ?: "N/A",
        bankAccountName = this.bankAccountName ?: "N/A"
    )
}

private fun BookingResponseDto.toBooking(): Booking {
    // Helper function để parse time với xử lý lỗi
    fun parseDateTime(timeString: String?): LocalDateTime {
        if (timeString.isNullOrBlank()) {
            throw IllegalArgumentException("Time string is null or empty")
        }
        return try {
            // Xử lý format có microseconds: 2025-11-03T23:00:09.5733903
            val cleanedTime = if (timeString.contains('.')) {
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

    return Booking(
        id = this.id.toString(),
        courtId = this.courtId.toString(),
        courtName = this.courtName ?: "Sân",
        userId = this.userId.toString(),
        userName = this.userName ?: "Người dùng",
        userPhone = "", // BookingResponseDto không có userPhone
        startTime = parseDateTime(this.startTime),
        endTime = parseDateTime(this.endTime),
        totalPrice = this.totalPrice.toLong(),
        status = when (this.status.uppercase()) {
            "PENDING_PAYMENT" -> BookingStatus.PENDING
            "PENDING_CONFIRMATION" -> BookingStatus.PENDING
            "CONFIRMED" -> BookingStatus.CONFIRMED
            "CANCELLED" -> BookingStatus.CANCELLED
            "REJECTED" -> BookingStatus.CANCELLED
            "EXPIRED" -> BookingStatus.CANCELLED
            "COMPLETED" -> BookingStatus.COMPLETED
            "NO_SHOW" -> BookingStatus.NO_SHOW
            else -> BookingStatus.PENDING
        },
        paymentStatus = if (this.paymentProofUploaded == true) {
            PaymentStatus.PAID
        } else {
            PaymentStatus.PENDING
        },
        paymentMethod = PaymentMethod.BANK_TRANSFER, // Default to bank transfer
        notes = this.rejectionReason,
        createdAt = LocalDateTime.parse("2025-01-01T00:00:00"), // BookingResponseDto không có createdAt
        updatedAt = LocalDateTime.parse("2025-01-01T00:00:00"), // BookingResponseDto không có updatedAt
        cancellationReason = this.rejectionReason,
        qrCode = null // BookingResponseDto không có qrCode
    )
}
