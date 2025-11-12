package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.CreateReviewRequest
import com.example.bookingcourt.data.remote.dto.ReviewDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Review API Interface
 * Based on API Documentation - Review APIs section
 */
interface ReviewApi {
    /**
     * 40. Create Review (Đánh giá sau khi hoàn thành)
     * POST /api/bookings/{bookingId}/review
     * Authentication Required: ✅ Yes (ROLE_USER - chỉ người đặt sân)
     *
     * Note: Chỉ có thể review booking có status = CONFIRMED hoặc COMPLETED
     */
    @POST("bookings/{bookingId}/review")
    suspend fun createReview(
        @Path("bookingId") bookingId: Long,
        @Body request: CreateReviewRequest
    ): Response<ApiResponse<ReviewDto>>

    /**
     * 41. Get Venue Reviews (Xem đánh giá của venue)
     * GET /api/venues/{venueId}/reviews
     * Authentication Required: ❌ No (Public)
     */
    @GET("venues/{venueId}/reviews")
    suspend fun getVenueReviews(
        @Path("venueId") venueId: Long
    ): Response<ApiResponse<List<ReviewDto>>>

    /**
     * 42. Get My Reviews
     * GET /api/my-reviews
     * Authentication Required: ✅ Yes (ROLE_USER)
     *
     * Response: Danh sách tất cả review của user đang đăng nhập
     */
    @GET("my-reviews")
    suspend fun getMyReviews(): Response<ApiResponse<List<ReviewDto>>>

    /**
     * 43. Get Booking Review
     * GET /api/bookings/{bookingId}/review
     * Authentication Required: ✅ Yes (ROLE_USER)
     *
     * Response: Review của booking cụ thể
     */
    @GET("bookings/{bookingId}/review")
    suspend fun getBookingReview(
        @Path("bookingId") bookingId: Long
    ): Response<ApiResponse<ReviewDto>>

    /**
     * 44. Delete Review
     * DELETE /api/reviews/{reviewId}
     * Authentication Required: ✅ Yes (ROLE_USER - chỉ người tạo review)
     */
    @DELETE("reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long
    ): Response<ApiResponse<Any>>
}
