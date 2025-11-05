package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.BookingApi
import com.example.bookingcourt.data.remote.api.VenueApi
import com.example.bookingcourt.data.remote.dto.*
import com.example.bookingcourt.domain.model.*
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val bookingApi: BookingApi,
    private val venueApi: VenueApi // ✅ Inject VenueApi để gọi availability API
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
            // ✅ Parse courtId format: "venueId_courtId"
            // VD: "14_4" -> venueId=14, courtId=4
            // Backend CẦN CẢ HAI để validate và tính giá
            val parts = courtId.split("_")
            val venueIdLong = parts.getOrNull(0)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid venueId in courtId: $courtId")
            val courtIdLong = parts.getOrNull(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid courtId format: $courtId. Expected format: venueId_courtId")

            // ✅ Backend yêu cầu cả venueId và courtId
            // venueId: để validate court thuộc venue + tính giá
            // courtId: để lưu vào bookings table
            val request = CreateBookingRequestDto(
                venueId = venueIdLong,
                courtId = courtIdLong,
                startTime = startTime,
                endTime = endTime
            )

            // Log request để debug
            Log.d("BookingRepo", "========== CREATE BOOKING REQUEST ==========")
            Log.d("BookingRepo", "  Original courtId param: $courtId")
            Log.d("BookingRepo", "  Parsed venueId: $venueIdLong")
            Log.d("BookingRepo", "  Parsed courtId: $courtIdLong")
            Log.d("BookingRepo", "  startTime: $startTime")
            Log.d("BookingRepo", "  endTime: $endTime")
            Log.d("BookingRepo", "==========================================")

            val apiResponse = bookingApi.createBooking(request)

            // Lấy data từ wrapper response
            val response = apiResponse.data

            Log.d("BookingRepo", "✅ Booking created successfully!")
            Log.d("BookingRepo", "  Booking ID: ${response.id}")
            Log.d("BookingRepo", "  Court ID: ${response.courtId}")
            Log.d("BookingRepo", "  Venue Name: ${response.venuesName}")
            Log.d("BookingRepo", "  Total Price: ${response.totalPrice}")
            Log.d("BookingRepo", "  API message: ${apiResponse.message}")

            val bookingWithBankInfo = response.toBookingWithBankInfo()
            emit(Resource.Success(bookingWithBankInfo))
        } catch (e: IllegalArgumentException) {
            // Lỗi parse courtId
            Log.e("BookingRepo", "❌ Invalid courtId format", e)
            emit(Resource.Error("Lỗi: ${e.message}"))
        } catch (e: retrofit2.HttpException) {
            // Lỗi HTTP từ server
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                null
            }

            Log.e("BookingRepo", "❌ HTTP Error creating booking")
            Log.e("BookingRepo", "  HTTP Code: ${e.code()}")
            Log.e("BookingRepo", "  Error message: ${e.message()}")
            Log.e("BookingRepo", "  Error body: $errorBody")

            val errorMessage = when (e.code()) {
                400 -> "Thông tin đặt sân không hợp lệ. Vui lòng kiểm tra lại."
                401 -> "Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy sân. Vui lòng thử lại."
                409 -> "Sân đã được đặt trong khung giờ này. Vui lòng chọn giờ khác."
                500 -> "Lỗi server: ${errorBody ?: "Server đang gặp sự cố. Vui lòng thử lại sau."}"
                else -> "Lỗi: ${e.message()}"
            }

            emit(Resource.Error(errorMessage))
        } catch (e: Exception) {
            // Lỗi khác
            Log.e("BookingRepo", "❌ Error creating booking", e)
            Log.e("BookingRepo", "  Error type: ${e.javaClass.simpleName}")
            Log.e("BookingRepo", "  Error message: ${e.message}")
            Log.e("BookingRepo", "  Error cause: ${e.cause}")

            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> "Kết nối tới server bị timeout. Vui lòng kiểm tra mạng."
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> "Không thể kết nối tới server. Vui lòng kiểm tra kết nối mạng."
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
            val response = bookingApi.getBookingDetail(bookingId)
            val bookingDetail = response.data.toBookingDetail()
            // Convert BookingDetail to Booking for backward compatibility
            val booking = bookingDetail.toBooking()
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

    // Payment confirmation flow implementations

    override suspend fun uploadPaymentProof(
        bookingId: String,
        imageFile: File
    ): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = bookingApi.uploadPaymentProof(bookingId, body)
            val bookingDetail = response.data.toBookingDetail()
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error uploading payment proof", e)
            emit(Resource.Error(e.message ?: "Lỗi khi upload ảnh"))
        }
    }

    override suspend fun confirmPayment(
        bookingId: String,
        paymentProofUrl: String
    ): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val request = ConfirmPaymentRequestDto(paymentProofUrl)
            val response = bookingApi.confirmPayment(bookingId, request)
            val bookingDetail = response.data.toBookingDetail()
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error confirming payment", e)
            emit(Resource.Error(e.message ?: "Lỗi khi xác nhận thanh toán"))
        }
    }

    override suspend fun acceptBooking(
        bookingId: String
    ): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.acceptBooking(bookingId)
            val bookingDetail = response.data.toBookingDetail()
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error accepting booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi chấp nhận booking"))
        }
    }

    override suspend fun rejectBooking(
        bookingId: String,
        reason: String
    ): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val request = RejectBookingRequestDto(reason)
            val response = bookingApi.rejectBooking(bookingId, request)
            val bookingDetail = response.data.toBookingDetail()
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error rejecting booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi từ chối booking"))
        }
    }

    override suspend fun getPendingBookings(): Flow<Resource<List<BookingDetail>>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getPendingBookings()
            val bookings = response.data.map { it.toBookingDetail() }
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting pending bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách chờ xác nhận"))
        }
    }

    override suspend fun getBookingDetail(
        bookingId: String
    ): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getBookingDetail(bookingId)
            val bookingDetail = response.data.toBookingDetail()
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting booking detail", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy chi tiết booking"))
        }
    }

    override suspend fun getBookedSlots(
        venueId: Long,
        date: String
    ): Flow<Resource<List<com.example.bookingcourt.domain.model.BookedSlot>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("BookingRepo", "📥 Fetching court availability for venue $venueId on $date")

            // Chuyển date từ "yyyy-MM-dd" thành ISO DateTime range cho API
            // VD: "2025-11-05" → startTime: "2025-11-05T00:00:00", endTime: "2025-11-05T23:59:59"
            val startTime = "${date}T00:00:00"
            val endTime = "${date}T23:59:59"

            Log.d("BookingRepo", "  Query range: $startTime to $endTime")

            // ✅ Gọi API availability có sẵn từ backend
            val response = venueApi.getCourtsAvailability(venueId, startTime, endTime)

            if (!response.isSuccessful || response.body() == null) {
                val errorMsg = "API error: ${response.code()} - ${response.message()}"
                Log.e("BookingRepo", "❌ $errorMsg")
                emit(Resource.Error(errorMsg))
                return@flow
            }

            val apiResponse = response.body()!!
            if (!apiResponse.success || apiResponse.data == null) {
                val errorMsg = apiResponse.message ?: "No data returned"
                Log.e("BookingRepo", "❌ API returned error: $errorMsg")
                emit(Resource.Error(errorMsg))
                return@flow
            }

            val courts = apiResponse.data
            Log.d("BookingRepo", "  ✅ Received ${courts.size} courts from API")

            // Chuyển đổi từ CourtAvailabilityDto sang BookedSlot domain model
            val bookedSlots = mutableListOf<com.example.bookingcourt.domain.model.BookedSlot>()

            courts.forEachIndexed { index, court ->
                val courtNumber = index + 1 // Court number theo thứ tự (1, 2, 3, ...)

                Log.d("BookingRepo", "  Court ${court.id} (${court.description}): ${court.bookedSlots?.size ?: 0} booked slots")

                // Nếu court có booked slots, thêm vào danh sách
                court.bookedSlots?.forEach { slot ->
                    bookedSlots.add(
                        com.example.bookingcourt.domain.model.BookedSlot(
                            courtId = court.id,
                            courtNumber = courtNumber,
                            startTime = slot.startTime,
                            endTime = slot.endTime,
                            status = BookingStatus.CONFIRMED, // Assume confirmed nếu đã booked
                            bookingId = slot.bookingId
                        )
                    )

                    Log.d("BookingRepo", "    🔒 Blocked: $courtNumber - ${slot.startTime} to ${slot.endTime}")
                }
            }

            Log.d("BookingRepo", "  ✅ Total ${bookedSlots.size} booked slots generated")
            emit(Resource.Success(bookedSlots))

        } catch (e: retrofit2.HttpException) {
            val errorBody = try { e.response()?.errorBody()?.string() } catch (ex: Exception) { null }
            Log.e("BookingRepo", "❌ HTTP Error getting booked slots: ${e.code()}")
            Log.e("BookingRepo", "  Error body: $errorBody")
            emit(Resource.Error("Lỗi HTTP ${e.code()}: ${e.message()}"))
        } catch (e: Exception) {
            Log.e("BookingRepo", "❌ Error getting booked slots", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy thông tin slots đã đặt"))
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
            id = this.venueId?.toString() ?: "0",  // ✅ Dùng venueId từ API thay vì hardcode
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

private fun BookingDetailResponseDto.toBookingDetail(): BookingDetail {
    // Helper function để parse time với xử lý lỗi
    fun parseDateTime(timeString: String?): LocalDateTime? {
        if (timeString.isNullOrBlank()) return null
        return try {
            val cleanedTime = if (timeString.contains('.')) {
                timeString.substringBefore('.')
            } else {
                timeString
            }
            LocalDateTime.parse(cleanedTime)
        } catch (e: Exception) {
            Log.e("BookingMapper", "Error parsing time: $timeString", e)
            null
        }
    }

    return BookingDetail(
        id = this.id.toString(),
        user = BookingUserInfo(
            id = this.userId.toString(),
            fullname = this.userName ?: "Người dùng",
            phone = this.userPhone
        ),
        court = BookingCourtInfo(
            id = this.courtId.toString(),
            description = this.courtName ?: "Sân"
        ),
        venue = BookingVenueInfo(
            id = this.venueId?.toString() ?: "0",
            name = this.venuesName ?: "Venue"
        ),
        venueAddress = this.venueAddress,
        startTime = parseDateTime(this.startTime) ?: LocalDateTime.parse("2025-01-01T00:00:00"),
        endTime = parseDateTime(this.endTime) ?: LocalDateTime.parse("2025-01-01T00:00:00"),
        totalPrice = this.totalPrice.toLong(),
        status = when (this.status.uppercase()) {
            "PENDING_PAYMENT" -> BookingStatus.PENDING_PAYMENT
            "PAYMENT_UPLOADED" -> BookingStatus.PAYMENT_UPLOADED
            "CONFIRMED" -> BookingStatus.CONFIRMED
            "REJECTED" -> BookingStatus.REJECTED
            "CANCELLED" -> BookingStatus.CANCELLED
            "COMPLETED" -> BookingStatus.COMPLETED
            "NO_SHOW" -> BookingStatus.NO_SHOW
            else -> BookingStatus.PENDING
        },
        paymentProofUploaded = this.paymentProofUploaded,
        paymentProofUrl = this.paymentProofUrl,
        paymentProofUploadedAt = this.paymentProofUploadedAt,
        rejectionReason = this.rejectionReason,
        expireTime = parseDateTime(this.expireTime),
        ownerBankInfo = this.ownerBankInfo?.let {
            BankInfo(
                bankName = it.bankName,
                bankAccountNumber = it.bankAccountNumber,
                bankAccountName = it.bankAccountName
            )
        }
    )
}

// Convert BookingDetail to Booking for backward compatibility
private fun BookingDetail.toBooking(): Booking {
    return Booking(
        id = this.id,
        courtId = this.court.id,
        courtName = this.court.description,
        userId = this.user.id,
        userName = this.user.fullname,
        userPhone = this.user.phone ?: "",
        startTime = this.startTime,
        endTime = this.endTime,
        totalPrice = this.totalPrice,
        status = this.status,
        paymentStatus = when {
            this.paymentProofUploaded && this.status == BookingStatus.PAYMENT_UPLOADED -> PaymentStatus.PENDING
            this.status == BookingStatus.CONFIRMED -> PaymentStatus.PAID
            else -> PaymentStatus.PENDING
        },
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        notes = null,
        createdAt = this.startTime, // fallback
        updatedAt = this.startTime, // fallback
        cancellationReason = this.rejectionReason,
        qrCode = null
    )
}
