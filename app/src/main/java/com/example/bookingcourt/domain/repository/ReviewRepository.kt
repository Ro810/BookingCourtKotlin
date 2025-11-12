package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    /**
     * Tạo đánh giá cho booking
     * Chỉ có thể review booking có status = CONFIRMED hoặc COMPLETED
     */
    suspend fun createReview(
        bookingId: Long,
        rating: Int,
        comment: String?
    ): Flow<Resource<Review>>

    /**
     * Lấy tất cả đánh giá của một venue
     * Public API - không cần authentication
     */
    suspend fun getVenueReviews(venueId: Long): Flow<Resource<List<Review>>>

    /**
     * Lấy tất cả đánh giá của user hiện tại
     * Yêu cầu authentication
     */
    suspend fun getMyReviews(): Flow<Resource<List<Review>>>

    /**
     * Lấy đánh giá của một booking cụ thể
     * Yêu cầu authentication
     */
    suspend fun getBookingReview(bookingId: Long): Flow<Resource<Review>>

    /**
     * Xóa đánh giá
     * Chỉ người tạo review mới có thể xóa
     */
    suspend fun deleteReview(reviewId: Long): Flow<Resource<Unit>>

    /**
     * Cập nhật đánh giá
     * Chỉ người tạo review mới có thể cập nhật
     */
    suspend fun updateReview(
        reviewId: Long,
        rating: Int,
        comment: String?
    ): Flow<Resource<Review>>
}
