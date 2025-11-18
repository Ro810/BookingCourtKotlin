package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.ReviewApi
import com.example.bookingcourt.data.remote.dto.CreateReviewRequest
import com.example.bookingcourt.data.remote.dto.ReviewDto
import com.example.bookingcourt.domain.model.Review
import com.example.bookingcourt.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewApi: ReviewApi
) : ReviewRepository {

    companion object {
        private const val TAG = "ReviewRepository"
    }

    override suspend fun createReview(
        bookingId: Long,
        rating: Int,
        comment: String?
    ): Flow<Resource<Review>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== CREATE REVIEW ==========")
            Log.d(TAG, "Booking ID: $bookingId")
            Log.d(TAG, "Rating: $rating")
            Log.d(TAG, "Comment: $comment")

            val request = CreateReviewRequest(rating = rating, comment = comment)
            val response = reviewApi.createReview(bookingId, request)

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val review = apiResponse.data.toDomain()
                    Log.d(TAG, "✓ Review created successfully - ID: ${review.id}")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(review))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tạo đánh giá"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Đánh giá không hợp lệ (rating phải từ 1-5)"
                    403 -> "Bạn không có quyền đánh giá booking này"
                    404 -> "Không tìm thấy booking"
                    else -> "Lỗi khi tạo đánh giá: ${response.code()}"
                }
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    override suspend fun getVenueReviews(venueId: Long): Flow<Resource<List<Review>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== GET VENUE REVIEWS ==========")
            Log.d(TAG, "Venue ID: $venueId")

            val response = reviewApi.getVenueReviews(venueId)

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val reviews = apiResponse.data.map { it.toDomain() }
                    Log.d(TAG, "✓ Fetched ${reviews.size} reviews for venue $venueId")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(reviews))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tải đánh giá"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Lỗi khi tải đánh giá: ${response.code()}"
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    override suspend fun getMyReviews(): Flow<Resource<List<Review>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== GET MY REVIEWS ==========")

            val response = reviewApi.getMyReviews()

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val reviews = apiResponse.data.map { it.toDomain() }
                    Log.d(TAG, "✓ Fetched ${reviews.size} reviews")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(reviews))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tải đánh giá"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = "Lỗi khi tải đánh giá: ${response.code()}"
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    override suspend fun getBookingReview(bookingId: Long): Flow<Resource<Review>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== GET BOOKING REVIEW ==========")
            Log.d(TAG, "Booking ID: $bookingId")

            val response = reviewApi.getBookingReview(bookingId)

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    val review = apiResponse.data.toDomain()
                    Log.d(TAG, "✓ Review found for booking $bookingId")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(review))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không tìm thấy đánh giá"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Chưa có đánh giá cho booking này"
                    else -> "Lỗi khi tải đánh giá: ${response.code()}"
                }
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    override suspend fun deleteReview(reviewId: Long): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== DELETE REVIEW ==========")
            Log.d(TAG, "Review ID: $reviewId")

            val response = reviewApi.deleteReview(reviewId)

            Log.d(TAG, "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    Log.d(TAG, "✓ Review deleted successfully")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(Unit))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể xóa đánh giá"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = when (response.code()) {
                    403 -> "Bạn không có quyền xóa đánh giá này"
                    404 -> "Không tìm thấy đánh giá"
                    else -> "Lỗi khi xóa đánh giá: ${response.code()}"
                }
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    override suspend fun updateReview(
        reviewId: Long,
        rating: Int,
        comment: String?
    ): Flow<Resource<Review>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATE REVIEW (DELETE + CREATE) ==========")
            Log.d(TAG, "Review ID: $reviewId")
            Log.d(TAG, "Rating: $rating")
            Log.d(TAG, "Comment: $comment")

            // BƯỚC 1: Lấy thông tin review hiện tại để có bookingId
            Log.d(TAG, "Step 1: Getting current review to extract bookingId...")
            val getResponse = reviewApi.getMyReviews()

            if (!getResponse.isSuccessful || getResponse.body()?.success != true) {
                Log.e(TAG, "❌ Failed to get current reviews")
                emit(Resource.Error("Không thể tải thông tin đánh giá"))
                return@flow
            }

            val currentReview = getResponse.body()?.data?.find { it.id == reviewId }
            if (currentReview == null) {
                Log.e(TAG, "❌ Review not found in user's reviews")
                emit(Resource.Error("Không tìm thấy đánh giá"))
                return@flow
            }

            val bookingId = currentReview.bookingId
            Log.d(TAG, "✓ Found review with bookingId: $bookingId")

            // BƯỚC 2: Xóa review cũ
            Log.d(TAG, "Step 2: Deleting old review...")
            val deleteResponse = reviewApi.deleteReview(reviewId)

            if (!deleteResponse.isSuccessful || deleteResponse.body()?.success != true) {
                Log.e(TAG, "❌ Failed to delete old review: ${deleteResponse.code()}")
                emit(Resource.Error("Không thể xóa đánh giá cũ"))
                return@flow
            }

            Log.d(TAG, "✓ Old review deleted successfully")

            // BƯỚC 3: Tạo review mới với rating và comment mới
            Log.d(TAG, "Step 3: Creating new review...")
            val request = CreateReviewRequest(rating = rating, comment = comment)
            val createResponse = reviewApi.createReview(bookingId, request)

            Log.d(TAG, "Create Response Code: ${createResponse.code()}")

            if (createResponse.isSuccessful) {
                val apiResponse = createResponse.body()
                if (apiResponse != null && apiResponse.success) {
                    val review = apiResponse.data.toDomain()
                    Log.d(TAG, "✓ Review updated successfully (recreated) - ID: ${review.id}")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(review))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tạo đánh giá mới"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorMsg = when (createResponse.code()) {
                    400 -> "Đánh giá không hợp lệ (rating phải từ 1-5)"
                    403 -> "Bạn không có quyền đánh giá booking này"
                    404 -> "Không tìm thấy booking"
                    else -> "Lỗi khi tạo đánh giá mới: ${createResponse.code()}"
                }
                Log.e(TAG, "⚠ $errorMsg")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception during update: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Lỗi kết nối"))
        }
    }

    // Mapper function
    private fun ReviewDto.toDomain(): Review {
        return Review(
            id = this.id.toString(),
            courtId = this.venueId.toString(),
            userId = this.userId.toString(),
            userName = this.userFullname,
            userAvatar = null, // API không trả về avatar
            rating = this.rating,
            comment = this.comment ?: "",
            images = emptyList(), // API không trả về images
            createdAt = parseDateTime(this.createdAt),
            updatedAt = parseDateTime(this.updatedAt ?: this.createdAt),
            isVerifiedBooking = true, // Vì review được tạo từ booking
            bookingId = this.bookingId.toString() // ✅ Thêm bookingId
        )
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        return try {
            Log.d(TAG, "📅 Parsing datetime: '$dateTimeString'")

            val trimmedString = dateTimeString.trim()

            // Kiểm tra xem có phải Unix timestamp không (chỉ toàn số)
            val isUnixTimestamp = trimmedString.matches(Regex("^\\d+(\\.\\d+)?$"))

            if (isUnixTimestamp) {
                // Parse Unix timestamp (seconds since epoch)
                val epochSeconds = trimmedString.substringBefore(".").toLong()
                Log.d(TAG, "   Detected Unix timestamp: $epochSeconds seconds")

                // Manual conversion to Vietnam time (UTC+7)
                // Add 7 hours (7 * 3600 seconds = 25200 seconds)
                val vietnamInstant = kotlinx.datetime.Instant.fromEpochSeconds(epochSeconds + 25200)

                // Convert to LocalDateTime (this will be Vietnam time since we added offset)
                val utcDateTime = vietnamInstant.toString() // Format: 2025-11-18T16:30:20Z
                val cleanedDateTime = utcDateTime.replace("Z", "").substringBefore(".")
                val vietnamDateTime = LocalDateTime.parse(cleanedDateTime)

                Log.d(TAG, "   ✅ Converted Unix timestamp to Vietnam time: $vietnamDateTime")
                return vietnamDateTime
            }

            // Kiểm tra xem có phải UTC time (có Z) không
            val isUtcTime = trimmedString.endsWith("Z")

            // Xử lý các format phổ biến từ backend
            val cleanedString = trimmedString
                .replace("Z", "") // Loại bỏ Z (UTC indicator)
                .substringBefore(".") // Loại bỏ milliseconds (.789)

            Log.d(TAG, "   Cleaned string: '$cleanedString'")
            Log.d(TAG, "   Is UTC time: $isUtcTime")

            // Parse datetime
            val parsed = when {
                // Format: "2025-11-13 14:30:00" (space separator, no T)
                cleanedString.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) -> {
                    val withT = cleanedString.replace(" ", "T")
                    LocalDateTime.parse(withT)
                }

                // Format chuẩn từ backend: "2025-11-07T14:00:00" (ISO 8601)
                cleanedString.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) -> {
                    LocalDateTime.parse(cleanedString)
                }

                // Format ngày giờ Việt Nam: "13/11/2025 14:30:00"
                cleanedString.matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) -> {
                    val parts = cleanedString.split(" ")
                    val dateParts = parts[0].split("/")
                    val isoFormat = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${parts[1]}"
                    LocalDateTime.parse(isoFormat)
                }

                // Thử parse trực tiếp
                else -> {
                    LocalDateTime.parse(cleanedString)
                }
            }

            // Nếu là UTC time, cần cộng thêm 7 giờ để convert sang giờ Việt Nam
            val result = if (isUtcTime) {
                // Convert UTC to Vietnam time (UTC+7)
                val year = parsed.year
                val month = parsed.monthNumber
                val day = parsed.dayOfMonth
                val hour = parsed.hour + 7 // Cộng 7 giờ
                val minute = parsed.minute
                val second = parsed.second

                // Xử lý trường hợp giờ >= 24 (sang ngày hôm sau)
                if (hour >= 24) {
                    // Tạo datetime mới với giờ đã điều chỉnh
                    val adjustedHour = hour - 24
                    val baseDate = LocalDateTime(year, month, day, 0, 0, 0)
                    // Cộng 1 ngày
                    val nextDay = LocalDateTime(
                        year = baseDate.year,
                        monthNumber = baseDate.monthNumber,
                        dayOfMonth = baseDate.dayOfMonth + 1,
                        hour = adjustedHour,
                        minute = minute,
                        second = second
                    )
                    Log.d(TAG, "   ✅ Converted UTC to Vietnam time (next day): $nextDay")
                    nextDay
                } else {
                    val vietnamTime = LocalDateTime(year, month, day, hour, minute, second)
                    Log.d(TAG, "   ✅ Converted UTC to Vietnam time: $vietnamTime")
                    vietnamTime
                }
            } else {
                // Đã là giờ Việt Nam, không cần convert
                Log.d(TAG, "   ✅ Parsed Vietnam time: $parsed")
                parsed
            }

            Log.d(TAG, "✅ Final result: $result")
            result

        } catch (e: Exception) {
            Log.e(TAG, "❌❌❌ CRITICAL: Failed to parse datetime ❌❌❌")
            Log.e(TAG, "   Input: '$dateTimeString'")
            Log.e(TAG, "   Error: ${e.message}")
            Log.e(TAG, "   Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Stack trace:", e)

            // ⚠️ KHÔNG dùng thời gian hiện tại làm fallback
            // Thay vào đó dùng giá trị mặc định để dễ phát hiện lỗi
            val fallbackTime = LocalDateTime.parse("2000-01-01T00:00:00")
            Log.e(TAG, "   ⚠️ Using FALLBACK time (NOT current time): $fallbackTime")
            Log.e(TAG, "   ⚠️ If you see year 2000, it means datetime parsing FAILED!")

            fallbackTime
        }
    }
}
