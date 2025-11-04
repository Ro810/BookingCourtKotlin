package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.BookingResponseDto
import com.example.bookingcourt.data.remote.dto.CreateBookingRequestDto
<<<<<<< Updated upstream
import com.example.bookingcourt.data.remote.dto.PaymentProofRequestDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
=======
import com.example.bookingcourt.data.remote.dto.CreateBookingResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
>>>>>>> Stashed changes

/**
 * Booking API - Match backend BookingController endpoints
 * Backend: /api/bookings
 */
interface BookingApi {
    /**
     * Tạo booking mới
     * POST /api/bookings
     * Backend: BookingController.createBooking()
     */
    @POST("bookings")
    suspend fun createBooking(
        @Body request: CreateBookingRequestDto
    ): Response<ApiResponse<BookingResponseDto>>

<<<<<<< Updated upstream
    /**
     * Lấy danh sách booking của user hiện tại
     * GET /api/bookings/my-bookings
     * Backend: BookingController.getMyBookings()
     */
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): Response<ApiResponse<List<BookingResponseDto>>>
=======
    // Existing paged user bookings (keep for compatibility)
    @GET("bookings")
    suspend fun getUserBookings(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("status") status: String? = null,
    ): BookingListResponseDto
>>>>>>> Stashed changes

    /**
     * Lấy chi tiết booking theo ID
     * GET /api/bookings/{id}
     * Backend: BookingController.getBookingById()
     */
    @GET("bookings/{id}")
    suspend fun getBookingById(
        @Path("id") bookingId: Long
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Xác nhận đã thanh toán (gửi ảnh chuyển khoản)
     * PUT /api/bookings/{id}/confirm-payment
     * Backend: BookingController.confirmPayment()
     */
    @PUT("bookings/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") bookingId: Long,
        @Body request: PaymentProofRequestDto
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Upload ảnh chứng minh thanh toán
     * POST /api/bookings/{id}/upload-payment-proof
     * Backend: BookingController.uploadPaymentProof()
     */
    @Multipart
    @POST("bookings/{id}/upload-payment-proof")
    suspend fun uploadPaymentProof(
        @Path("id") bookingId: Long,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Hủy booking
     * PUT /api/bookings/{id}/cancel
     * Backend: BookingController.cancelBooking()
     */
    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: Long
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Owner chấp nhận booking
     * PUT /api/bookings/{id}/accept
     * Backend: BookingController.acceptBooking()
     */
    @PUT("bookings/{id}/accept")
    suspend fun acceptBooking(
        @Path("id") bookingId: Long
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Owner từ chối booking
     * PUT /api/bookings/{id}/reject
     * Backend: BookingController.rejectBooking()
     */
    @PUT("bookings/{id}/reject")
    suspend fun rejectBooking(
        @Path("id") bookingId: Long,
        @Body request: Map<String, String>  // { "rejectionReason": "..." }
    ): Response<ApiResponse<BookingResponseDto>>

    /**
     * Lấy danh sách booking của một venue (Owner only)
     * GET /api/bookings/venue/{venueId}
     * Backend: BookingController.getVenueBookings()
     */
    @GET("bookings/venue/{venueId}")
    suspend fun getVenueBookings(
        @Path("venueId") venueId: Long
    ): Response<ApiResponse<List<BookingResponseDto>>>

<<<<<<< Updated upstream
    /**
     * Lấy danh sách booking chờ xác nhận (Owner only)
     * GET /api/bookings/pending
     * Backend: BookingController.getPendingBookings()
     */
    @GET("bookings/pending")
    suspend fun getPendingBookings(): Response<ApiResponse<List<BookingResponseDto>>>
=======
    // Backend: POST /bookings/{id}/upload-payment-proof (multipart)
    @Multipart
    @POST("bookings/{id}/upload-payment-proof")
    suspend fun uploadPaymentProof(
        @Path("id") bookingId: Long,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<CreateBookingResponseDto>>

    // Backend: PUT /bookings/{id}/confirm-payment
    @PUT("bookings/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") bookingId: Long,
        @Body body: Map<String, String>
    ): Response<ApiResponse<CreateBookingResponseDto>>

    // Owner actions
    @PUT("bookings/{id}/accept")
    suspend fun acceptBooking(@Path("id") bookingId: Long): Response<ApiResponse<CreateBookingResponseDto>>

    @PUT("bookings/{id}/reject")
    suspend fun rejectBooking(
        @Path("id") bookingId: Long,
        @Body body: Map<String, String>
    ): Response<ApiResponse<CreateBookingResponseDto>>

    // Backend: GET /bookings/my-bookings
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): Response<ApiResponse<List<CreateBookingResponseDto>>>

    // Backend: GET /bookings/pending (owner)
    @GET("bookings/pending")
    suspend fun getPendingBookings(): Response<ApiResponse<List<CreateBookingResponseDto>>>

    // Backend: GET /bookings/venue/{venueId} (owner)
    @GET("bookings/venue/{venueId}")
    suspend fun getVenueBookings(
        @Path("venueId") venueId: Long
    ): Response<ApiResponse<List<CreateBookingResponseDto>>>

    // Legacy confirm (keep to avoid breaking existing implementation)
    @PUT("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: String): BookingDto
>>>>>>> Stashed changes

    /**
     * Lấy tất cả booking của owner
     * GET /api/bookings/owner/all-bookings
     * Backend: BookingController.getAllBookingsForOwner()
     */
    @GET("bookings/owner/all-bookings")
    suspend fun getAllBookingsForOwner(): Response<ApiResponse<List<BookingResponseDto>>>
    fun getUserBookings(page: Int, size: Int, status: String?)
}
