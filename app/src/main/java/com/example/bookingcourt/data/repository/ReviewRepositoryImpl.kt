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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
            isVerifiedBooking = true // Vì review được tạo từ booking
        )
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        return try {
            Log.d(TAG, "📅 Parsing datetime: '$dateTimeString'")

            // Xử lý các format phổ biến từ backend
            val cleanedString = dateTimeString.trim()

            // TimeZone Việt Nam (UTC+7)
            val vietnamTimeZone = TimeZone.of("Asia/Ho_Chi_Minh")

            // Thử parse với nhiều format khác nhau
            val result = when {
                // Format: "2025-11-13 14:30:00" (space separator, no T)
                cleanedString.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*")) -> {
                    val withT = cleanedString.replace(" ", "T")
                    val withoutMillis = withT.substringBefore(".")
                    // Parse as local time và assume là giờ Việt Nam
                    LocalDateTime.parse(withoutMillis)
                }
                // Format ISO với milliseconds: "2025-11-13T14:30:00.123Z"
                // Format ISO với Z: "2025-11-13T14:30:00Z"
                cleanedString.contains("T") && cleanedString.endsWith("Z") -> {
                    // Parse as UTC Instant rồi convert sang giờ Việt Nam
                    val withoutMillis = cleanedString.substringBefore(".").removeSuffix("Z")
                    val instant = Instant.parse("${withoutMillis}Z")
                    val vietnamTime = instant.toLocalDateTime(vietnamTimeZone)
                    Log.d(TAG, "   ✅ UTC -> Vietnam: ${withoutMillis}Z -> $vietnamTime")
                    vietnamTime
                }
                // Format ISO với timezone offset: "2025-11-13T14:30:00+07:00"
                cleanedString.contains("T") && cleanedString.contains("+") -> {
                    val withoutMillis = cleanedString.substringBefore(".")
                    // Parse với timezone info
                    val instant = Instant.parse(withoutMillis)
                    instant.toLocalDateTime(vietnamTimeZone)
                }
                // Format ISO với timezone offset âm: "2025-11-13T14:30:00-05:00"
                cleanedString.contains("T") && cleanedString.lastIndexOf("-") > 10 -> {
                    val withoutMillis = cleanedString.substringBefore(".")
                    val instant = Instant.parse(withoutMillis)
                    instant.toLocalDateTime(vietnamTimeZone)
                }
                // Format ISO chuẩn: "2025-11-13T14:30:00" (assume giờ Việt Nam)
                cleanedString.contains("T") -> {
                    val withoutMillis = cleanedString.substringBefore(".")
                    LocalDateTime.parse(withoutMillis)
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

            Log.d(TAG, "✅ Parsed successfully: $result")
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
