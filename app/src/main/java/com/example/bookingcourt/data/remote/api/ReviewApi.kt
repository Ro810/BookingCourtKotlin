package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.ReviewDto
import com.example.bookingcourt.data.remote.dto.ReviewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Review API - Đánh giá và xếp hạng venues
 * Backend: ReviewController.java
 */
interface ReviewApi {
    /**
     * Tạo review mới cho booking đã hoàn thành
     * POST /api/bookings/{bookingId}/review
     * Requires: ROLE_USER
     * Backend: ReviewController.createReview()
     */
    @POST("bookings/{bookingId}/review")
    suspend fun createReview(
        @Path("bookingId") bookingId: Long,
        @Body request: ReviewRequest
    ): Response<ApiResponse<ReviewDto>>

    /**
     * Lấy tất cả review của một venue (công khai)
     * GET /api/venues/{venueId}/reviews
     * Backend: ReviewController.getVenueReviews()
     */
    @GET("venues/{venueId}/reviews")
    suspend fun getVenueReviews(
        @Path("venueId") venueId: Long
    ): Response<ApiResponse<List<ReviewDto>>>

    /**
     * Lấy tất cả review của user hiện tại
     * GET /api/my-reviews
     * Requires: ROLE_USER
     * Backend: ReviewController.getMyReviews()
     */
    @GET("my-reviews")
    suspend fun getMyReviews(): Response<ApiResponse<List<ReviewDto>>>

    /**
     * Lấy review của một booking cụ thể
     * GET /api/bookings/{bookingId}/review
     * Backend: ReviewController.getBookingReview()
     */
    @GET("bookings/{bookingId}/review")
    suspend fun getBookingReview(
        @Path("bookingId") bookingId: Long
    ): Response<ApiResponse<ReviewDto>>
}

