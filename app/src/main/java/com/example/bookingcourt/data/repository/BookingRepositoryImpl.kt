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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    private val bookingApi: BookingApi,
    private val venueApi: VenueApi
) : BookingRepository {

    /**
     * ✅ Tạo booking nhiều sân - method mới
     */
    override suspend fun createBookingMultipleCourts(
        bookingItems: List<BookingItemData>
    ): Flow<Resource<BookingWithBankInfo>> = flow {
        emit(Resource.Loading())
        try {
            if (bookingItems.isEmpty()) {
                throw IllegalArgumentException("Booking items cannot be empty")
            }

            // Lấy venueId từ courtId đầu tiên (tất cả sân phải cùng venue)
            val firstCourtId = bookingItems.first().courtId
            val parts = firstCourtId.split("_")
            val venueIdLong = parts.getOrNull(0)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid venueId in courtId: $firstCourtId")

            // Chuyển đổi BookingItemData thành BookingItemRequestDto
            val requestItems = bookingItems.map { item ->
                val itemParts = item.courtId.split("_")
                val courtIdLong = itemParts.getOrNull(1)?.toLongOrNull()
                    ?: throw IllegalArgumentException("Invalid courtId format: ${item.courtId}")

                BookingItemRequestDto(
                    courtId = courtIdLong,
                    startTime = item.startTime,
                    endTime = item.endTime
                )
            }

            val request = CreateBookingRequestDto.forMultipleCourts(
                venueId = venueIdLong,
                items = requestItems
            )

            Log.d("BookingRepo", "Creating booking: venue=$venueIdLong, items=${requestItems.size}")
            requestItems.forEachIndexed { index, item ->
                Log.d("BookingRepo", "  [$index] Court ${item.courtId}: ${item.startTime} - ${item.endTime}")
            }

            val apiResponse = bookingApi.createBooking(request)
            val response = apiResponse.data ?: throw IllegalStateException("Response data is null")

            // Sử dụng startTime/endTime từ item đầu tiên làm fallback
            val mapped = response.toBookingWithBankInfo(
                fallbackStartTime = bookingItems.first().startTime,
                fallbackEndTime = bookingItems.first().endTime
            )

            Log.d("BookingRepo", "✅ Booking created successfully:")
            Log.d("BookingRepo", "   Booking ID: ${mapped.id}")
            Log.d("BookingRepo", "   Total Price: ${mapped.totalPrice} VNĐ")

            emit(Resource.Success(mapped))
        } catch (e: retrofit2.HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("BookingRepo", "HTTP error creating booking: ${e.code()} - $body")
            val message = when (e.code()) {
                400 -> "Thông tin đặt sân không hợp lệ. Vui lòng kiểm tra lại."
                401 -> "Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy sân. Vui lòng thử lại."
                409 -> "Một hoặc nhiều sân đã được đặt trong khung giờ này. Vui lòng chọn giờ khác."
                500 -> "Lỗi server: ${body ?: "Server đang gặp sự cố."}"
                else -> "Lỗi: ${e.message()}"
            }
            emit(Resource.Error(message))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error creating booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi tạo booking"))
        }
    }

    override suspend fun createBooking(
        courtId: String,
        startTime: String,
        endTime: String,
        notes: String?,
        paymentMethod: String
    ): Flow<Resource<BookingWithBankInfo>> = flow {
        emit(Resource.Loading())
        try {
            val parts = courtId.split("_")
            val venueIdLong = parts.getOrNull(0)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid venueId in courtId: $courtId")
            val courtIdLong = parts.getOrNull(1)?.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid courtId format: $courtId. Expected format: venueId_courtId")

            val bookingItem = BookingItemRequestDto(
                courtId = courtIdLong,
                startTime = startTime,
                endTime = endTime
            )

            val request = CreateBookingRequestDto.forMultipleCourts(
                venueId = venueIdLong,
                items = listOf(bookingItem)
            )

            Log.d("BookingRepo", "Creating booking: venue=$venueIdLong, items=${request.bookingItems?.size}")

            val apiResponse = bookingApi.createBooking(request)
            val response = apiResponse.data ?: throw IllegalStateException("Response data is null")

            val mapped = response.toBookingWithBankInfo(fallbackStartTime = startTime, fallbackEndTime = endTime)
            emit(Resource.Success(mapped))
        } catch (e: retrofit2.HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("BookingRepo", "HTTP error creating booking: ${e.code()} - $body")
            val message = when (e.code()) {
                400 -> "Thông tin đặt sân không hợp lệ. Vui lòng kiểm tra lại."
                401 -> "Vui lòng đăng nhập lại"
                404 -> "Không tìm thấy sân. Vui lòng thử lại."
                409 -> "Sân đã được đặt trong khung giờ này. Vui lòng chọn giờ khác."
                500 -> "Lỗi server: ${body ?: "Server đang gặp sự cố."}"
                else -> "Lỗi: ${e.message()}"
            }
            emit(Resource.Error(message))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error creating booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi tạo booking"))
        }
    }

    override suspend fun getUserBookings(page: Int, size: Int, status: String?): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getUserBookings(page, size, status)
            val bookings = response.bookings.map { it.toBooking() }
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting user bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách booking"))
        }
    }

    override suspend fun getMyBookings(): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("BookingRepo", "Calling getMyBookings API...")
            val response = bookingApi.getMyBookings()

            if (response.success && response.data != null) {
                val bookings = response.data.map { it.toBookingDetail(venueApi).toBooking() }
                Log.d("BookingRepo", "✅ Got ${bookings.size} bookings from getMyBookings")
                emit(Resource.Success(bookings))
            } else {
                Log.e("BookingRepo", "❌ API returned success=false: ${response.message}")
                emit(Resource.Error(response.message ?: "Không thể lấy danh sách booking"))
            }
        } catch (e: Exception) {
            Log.e("BookingRepo", "❌ Error getting my bookings: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách booking"))
        }
    }

    override suspend fun getBookingById(bookingId: String): Flow<Resource<Booking>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.getBookingDetail(bookingId)
            val bookingDetail = response.data.toBookingDetail(venueApi)
            emit(Resource.Success(bookingDetail.toBooking()))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting booking by id", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy chi tiết booking"))
        }
    }

    override suspend fun cancelBooking(bookingId: String, reason: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            bookingApi.cancelBooking(bookingId, mapOf("reason" to reason))
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error cancelling booking", e)
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
            Log.e("BookingRepo", "Error confirming booking", e)
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
            Log.e("BookingRepo", "Error getting upcoming bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy booking sắp tới"))
        }
    }

    override suspend fun uploadPaymentProof(bookingId: String, imageFile: File): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            if (!imageFile.exists() || imageFile.length() == 0L) {
                emit(Resource.Error("File không tồn tại hoặc rỗng"))
                return@flow
            }
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val response = bookingApi.uploadPaymentProof(bookingId, body)
            val bookingDetail = response.data.toBookingDetail(venueApi)
            emit(Resource.Success(bookingDetail))
        } catch (e: retrofit2.HttpException) {
            val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("BookingRepo", "HTTP error uploading payment proof: ${e.code()} - $errorBody")
            val message = when (e.code()) {
                400 -> "File không hợp lệ. Vui lòng chọn ảnh khác."
                401 -> "Vui lòng đăng nhập lại"
                413 -> "File quá lớn. Vui lòng chọn ảnh nhỏ hơn."
                500 -> "Lỗi server. Vui lòng thử lại sau."
                else -> "Lỗi upload: ${e.message()}"
            }
            emit(Resource.Error(message))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error uploading payment proof", e)
            emit(Resource.Error(e.message ?: "Lỗi khi upload ảnh"))
        }
    }

    override suspend fun confirmPayment(bookingId: String, paymentProofUrl: String): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val request = ConfirmPaymentRequestDto(paymentProofUrl)
            val response = bookingApi.confirmPayment(bookingId, request)
            val bookingDetail = response.data.toBookingDetail(venueApi)
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error confirming payment", e)
            emit(Resource.Error(e.message ?: "Lỗi khi xác nhận thanh toán"))
        }
    }

    override suspend fun acceptBooking(bookingId: String): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = bookingApi.acceptBooking(bookingId)
            val bookingDetail = response.data.toBookingDetail(venueApi)
            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error accepting booking", e)
            emit(Resource.Error(e.message ?: "Lỗi khi chấp nhận booking"))
        }
    }

    override suspend fun rejectBooking(bookingId: String, reason: String): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            val request = RejectBookingRequestDto(reason)
            val response = bookingApi.rejectBooking(bookingId, request)
            val bookingDetail = response.data.toBookingDetail(venueApi)
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
            val bookings = response.data.map { it.toBookingDetail(venueApi) }
            emit(Resource.Success(bookings))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting pending bookings", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy danh sách chờ xác nhận"))
        }
    }

    override suspend fun getBookingDetail(bookingId: String): Flow<Resource<BookingDetail>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("BookingRepo", "🔍 Getting booking detail for ID: $bookingId")
            val response = bookingApi.getBookingDetail(bookingId)

            // ✅ LOG RAW API RESPONSE
            Log.d("BookingRepo", "========== API RESPONSE ==========")
            Log.d("BookingRepo", "  Booking ID: ${response.data.id}")
            Log.d("BookingRepo", "  Court ID: ${response.data.courtId}")
            Log.d("BookingRepo", "  Court Name: ${response.data.courtName}")
            Log.d("BookingRepo", "  Total Price: ${response.data.totalPrice}")

            // ✅ CHECK BOOKING ITEMS
            if (response.data.bookingItems != null) {
                Log.d("BookingRepo", "  ✅ HAS BOOKING ITEMS: ${response.data.bookingItems.size} items")
                response.data.bookingItems.forEachIndexed { index, item ->
                    Log.d("BookingRepo", "    [$index] Court ${item.courtId}: ${item.courtName}")
                    Log.d("BookingRepo", "         Time: ${item.startTime} - ${item.endTime}")
                    Log.d("BookingRepo", "         Price: ${item.price}")
                }
            } else {
                Log.w("BookingRepo", "  ⚠️ NO BOOKING ITEMS in response - using legacy court data")
            }
            Log.d("BookingRepo", "==================================")

            val bookingDetail = response.data.toBookingDetail(venueApi)

            // ✅ LOG MAPPED DATA
            Log.d("BookingRepo", "========== MAPPED BOOKING DETAIL ==========")
            Log.d("BookingRepo", "  Booking ID: ${bookingDetail.id}")
            if (bookingDetail.bookingItems != null) {
                Log.d("BookingRepo", "  ✅ Mapped ${bookingDetail.bookingItems.size} booking items")
            } else {
                Log.w("BookingRepo", "  ⚠️ NO booking items after mapping")
                Log.d("BookingRepo", "  Legacy court: ${bookingDetail.court?.description}")
            }
            Log.d("BookingRepo", "  Total Price: ${bookingDetail.totalPrice}")
            Log.d("BookingRepo", "==========================================")

            emit(Resource.Success(bookingDetail))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting booking detail", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy chi tiết booking"))
        }
    }

    override suspend fun getBookedSlots(venueId: Long, date: String): Flow<Resource<List<BookedSlot>>> = flow {
        emit(Resource.Loading())
        try {
            val startTime = "${date}T00:00:00"
            val endTime = "${date}T23:59:59"
            val response = venueApi.getCourtsAvailability(venueId, startTime, endTime)
            if (!response.isSuccessful || response.body() == null) {
                emit(Resource.Error("API error: ${response.code()} - ${response.message()}"))
                return@flow
            }
            val apiResponse = response.body()!!
            if (!apiResponse.success || apiResponse.data == null) {
                val errorMsg = apiResponse.message ?: "No data returned"
                Log.e("BookingRepo", "❌ API returned error: $errorMsg")
                emit(Resource.Error(errorMsg))
                return@flow
            }

            // apiResponse.data is CourtAvailabilityResponseDto -> extract courts list
            val courts = apiResponse.data.courts
            val bookedSlots = mutableListOf<BookedSlot>()

            courts.forEach { court ->
                // ✅ FIX: Sử dụng court.id thực tế thay vì index
                // Backend trả về court.id có thể không liên tiếp (1, 2, 5, 10...)
                val courtNumber = court.id.toInt()
                Log.d("BookingRepo", "  Court ${court.id} (${court.description}): ${court.bookedSlots?.size ?: 0} booked slots")

                court.bookedSlots?.forEach { slot ->
                    // slot.startTime / slot.endTime are arrays [year, month, day, hour, minute]
                    val s = slot.startTime
                    val e = slot.endTime
                    val startIso = if (s.size >= 5) String.format("%04d-%02d-%02dT%02d:%02d:00", s[0], s[1], s[2], s[3], s[4]) else "0000-01-01T00:00:00"
                    val endIso = if (e.size >= 5) String.format("%04d-%02d-%02dT%02d:%02d:00", e[0], e[1], e[2], e[3], e[4]) else "0000-01-01T00:00:00"

                    bookedSlots.add(
                        BookedSlot(
                            courtId = court.id,
                            courtNumber = courtNumber,
                            startTime = startIso,
                            endTime = endIso,
                            status = BookingStatus.CONFIRMED,
                            bookingId = slot.bookingId.toString()
                        )
                    )
                }
            }
            emit(Resource.Success(bookedSlots))
        } catch (e: Exception) {
            Log.e("BookingRepo", "Error getting booked slots", e)
            emit(Resource.Error(e.message ?: "Lỗi khi lấy thông tin slots đã đặt"))
        }
    }
}

// ---------------- Mapper helpers ----------------

private fun CreateBookingResponseDto.toBookingWithBankInfo(
    fallbackStartTime: String? = null,
    fallbackEndTime: String? = null
): BookingWithBankInfo {

    fun parseDateTimeSafe(s: String?, fallback: String? = null): LocalDateTime? {
        val str = s ?: fallback
        if (str.isNullOrBlank()) return null
        return try {
            val cleaned = if (str.contains('.')) str.substringBefore('.') else str
            LocalDateTime.parse(cleaned)
        } catch (e: Exception) {
            Log.e("BookingMapper", "Error parsing time: $str", e)
            null
        }
    }

    val mappedItems = this.bookingItems?.map { item ->
        BookingItem(
            courtId = item.courtId.toString(),
            courtName = item.courtName ?: "Sân ${item.courtId}",
            startTime = parseDateTimeSafe(item.startTime, fallbackStartTime) ?: parseDateTimeSafe(fallbackStartTime)
                ?: throw IllegalArgumentException("Missing start time"),
            endTime = parseDateTimeSafe(item.endTime, fallbackEndTime) ?: parseDateTimeSafe(fallbackEndTime)
                ?: throw IllegalArgumentException("Missing end time"),
            price = item.price.toLong()
        )
    }

    val courtInfo = this.courtId?.let {
        BookingCourtInfo(id = it.toString(), description = this.courtName ?: "Sân")
    }

    val start = parseDateTimeSafe(this.startTime, fallbackStartTime) ?: mappedItems?.firstOrNull()?.startTime
        ?: throw IllegalArgumentException("Start time missing")
    val end = parseDateTimeSafe(this.endTime, fallbackEndTime) ?: mappedItems?.firstOrNull()?.endTime
        ?: throw IllegalArgumentException("End time missing")

    val expire = parseDateTimeSafe(this.expireTime) ?: run {
        val instant = start.toInstant(TimeZone.currentSystemDefault())
        (instant + 5.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    return BookingWithBankInfo(
        id = this.id.toString(),
        user = BookingUserInfo(id = this.userId.toString(), fullname = this.userName ?: "Người dùng", phone = null),
        court = courtInfo ?: BookingCourtInfo(id = this.courtId?.toString() ?: "0", description = this.courtName ?: "Sân"),
        venue = BookingVenueInfo(id = this.venueId?.toString() ?: "0", name = this.venuesName ?: "Venue"),
        startTime = start,
        endTime = end,
        totalPrice = this.totalPrice.toLong(),
        status = when {
            this.status.equals("PENDING_PAYMENT", ignoreCase = true) -> BookingStatus.PENDING
            this.status.equals("CONFIRMED", ignoreCase = true) -> BookingStatus.CONFIRMED
            this.status.equals("CANCELLED", ignoreCase = true) -> BookingStatus.CANCELLED
            this.status.equals("COMPLETED", ignoreCase = true) -> BookingStatus.COMPLETED
            this.status.equals("NO_SHOW", ignoreCase = true) -> BookingStatus.NO_SHOW
            else -> BookingStatus.PENDING
        },
        expireTime = expire,
        ownerBankInfo = this.ownerBankInfo?.toBankInfo() ?: BankInfo(
            bankName = "Chưa có thông tin",
            bankAccountNumber = "",
            bankAccountName = ""
        ),
        notes = null,
        bookingItems = mappedItems
    )
}

private fun BankInfoDto.toBankInfo(): BankInfo = BankInfo(
    bankName = this.bankName,
    bankAccountNumber = this.bankAccountNumber,
    bankAccountName = this.bankAccountName
)

private fun BookingDto.toBooking(): Booking {
    fun cleanParse(s: String): LocalDateTime = LocalDateTime.parse(if (s.contains('.')) s.substringBefore('.') else s)
    return Booking(
        id = this.id,
        courtId = this.courtId,
        courtName = this.courtName,
        userId = this.userId,
        userName = this.userName,
        userPhone = this.userPhone,
        startTime = cleanParse(this.startTime),
        endTime = cleanParse(this.endTime),
        totalPrice = this.totalPrice,
        status = BookingStatus.valueOf(this.status.uppercase()),
        paymentStatus = PaymentStatus.valueOf(this.paymentStatus.uppercase()),
        paymentMethod = this.paymentMethod?.let { PaymentMethod.valueOf(it.uppercase()) },
        notes = this.notes,
        createdAt = cleanParse(this.createdAt),
        updatedAt = cleanParse(this.updatedAt),
        cancellationReason = this.cancellationReason,
        qrCode = this.qrCode
    )
}

private suspend fun BookingDetailResponseDto.toBookingDetail(venueApi: VenueApi): BookingDetail {
    fun parse(s: String?): LocalDateTime? {
        if (s.isNullOrBlank()) return null
        return try { LocalDateTime.parse(if (s.contains('.')) s.substringBefore('.') else s) } catch (e: Exception) { Log.e("BookingMapper","Error parsing: $s", e); null }
    }

    val start = parse(this.startTime) ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val end = parse(this.endTime) ?: start
    val expire = parse(this.expireTime)

    // ✅ Map bookingItems nếu có
    val items = this.bookingItems?.map { item ->
        BookingItem(
            courtId = item.courtId.toString(),
            courtName = item.courtName ?: "Sân ${item.courtId}",
            startTime = parse(item.startTime) ?: start,
            endTime = parse(item.endTime) ?: end,
            price = item.price.toLong()
        )
    }

    // ✅ Lấy thông tin venue address theo thứ tự ưu tiên:
    // 1. Nếu backend trả về venueAddress -> dùng luôn
    // 2. Nếu backend trả về venue object -> dùng venue.address.getFullAddress()
    // 3. Gọi API venueApi.getVenueById() để lấy
    // 4. Fallback: dùng venuesName
    val venueAddress = when {
        // Ưu tiên 1: Backend trả về sẵn venueAddress string
        !this.venueAddress.isNullOrBlank() -> {
            Log.d("BookingMapper", "✅ Using venueAddress from backend: ${this.venueAddress}")
            this.venueAddress
        }
        // Ưu tiên 2: Backend trả về venue object với address
        this.venue?.address != null -> {
            val addr = this.venue.address.getFullAddress()
            Log.d("BookingMapper", "✅ Using venue.address from backend: $addr")
            addr
        }
        // Ưu tiên 3: Gọi API để lấy venue detail
        this.venueId != null -> {
            try {
                val venueResponse = venueApi.getVenueById(this.venueId)
                if (venueResponse.isSuccessful && venueResponse.body()?.data != null) {
                    val venueDetail = venueResponse.body()!!.data!!
                    val fullAddress = venueDetail.address.getFullAddress()
                    Log.d("BookingMapper", "✅ Fetched venue address from API: $fullAddress")
                    fullAddress
                } else {
                    Log.w("BookingMapper", "⚠️ Failed to fetch venue, using fallback")
                    this.venuesName ?: "Chưa cập nhật"
                }
            } catch (e: Exception) {
                Log.e("BookingMapper", "❌ Error fetching venue address", e)
                this.venuesName ?: "Chưa cập nhật"
            }
        }
        // Fallback: chỉ có tên venue
        else -> {
            Log.w("BookingMapper", "⚠️ No venue info, using fallback")
            this.venuesName ?: "Chưa cập nhật"
        }
    }

    return BookingDetail(
        id = this.id.toString(),
        user = BookingUserInfo(id = this.userId.toString(), fullname = this.userName ?: "Người dùng", phone = this.userPhone),

        // ✅ Hỗ trợ cả bookingItems và court legacy
        bookingItems = items,
        court = if (this.courtId != null) BookingCourtInfo(id = this.courtId.toString(), description = this.courtName ?: "Sân") else null,

        venue = BookingVenueInfo(id = this.venueId?.toString() ?: "0", name = this.venuesName ?: "Venue"),
        venueAddress = venueAddress,
        startTime = start,
        endTime = end,
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
        expireTime = expire,
        ownerBankInfo = this.ownerBankInfo?.let { BankInfo(bankName = it.bankName, bankAccountNumber = it.bankAccountNumber, bankAccountName = it.bankAccountName) }
    )
}

private fun BookingDetail.toBooking(): Booking {
    return Booking(
        id = this.id,
        courtId = this.bookingItems?.firstOrNull()?.courtId ?: this.court?.id ?: "0",
        courtName = this.bookingItems?.firstOrNull()?.courtName ?: this.court?.description ?: "Sân",
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
        createdAt = this.startTime,
        updatedAt = this.startTime,
        cancellationReason = this.rejectionReason,
        qrCode = null
    )
}
