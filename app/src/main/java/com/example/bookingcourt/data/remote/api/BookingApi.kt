package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.BaseResponseDto
import com.example.bookingcourt.data.remote.dto.BookingDetailResponseDto
import com.example.bookingcourt.data.remote.dto.BookingDto
import com.example.bookingcourt.data.remote.dto.BookingListResponseDto
import com.example.bookingcourt.data.remote.dto.ConfirmPaymentRequestDto
import com.example.bookingcourt.data.remote.dto.CreateBookingRequestDto
import com.example.bookingcourt.data.remote.dto.CreateBookingResponseDto
import com.example.bookingcourt.data.remote.dto.RejectBookingRequestDto
import com.example.bookingcourt.data.remote.dto.BookedSlotDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApi {
    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequestDto): ApiResponse<CreateBookingResponseDto>

    @GET("bookings")
    suspend fun getUserBookings(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("status") status: String? = null,
    ): BookingListResponseDto

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String,
        @Body reason: Map<String, String>,
    ): ApiResponse<BookingDto>

    @PUT("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: String): BookingDto

    @GET("bookings/upcoming")
    suspend fun getUpcomingBookings(): List<BookingDto>

    // Payment confirmation flow APIs
    @Multipart
    @POST("bookings/{id}/upload-payment-proof")
    suspend fun uploadPaymentProof(
        @Path("id") bookingId: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<BookingDetailResponseDto>

    @PUT("bookings/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") bookingId: String,
        @Body request: ConfirmPaymentRequestDto
    ): ApiResponse<BookingDetailResponseDto>

    @PUT("bookings/{id}/accept")
    suspend fun acceptBooking(
        @Path("id") bookingId: String
    ): ApiResponse<BookingDetailResponseDto>

    @PUT("bookings/{id}/reject")
    suspend fun rejectBooking(
        @Path("id") bookingId: String,
        @Body request: RejectBookingRequestDto
    ): ApiResponse<BookingDetailResponseDto>

    /**
     * Lấy danh sách booking chờ xác nhận theo venue ID (cho chủ sân)
     * @param venueId ID của venue
     * @return Danh sách bookings có status PAYMENT_UPLOADED của venue
     */
    @GET("bookings/venue/{venueId}/pending")
    suspend fun getVenuePendingBookings(
        @Path("venueId") venueId: Long
    ): ApiResponse<List<BookingDetailResponseDto>>

    @GET("bookings/pending")
    suspend fun getPendingBookings(): ApiResponse<List<BookingDetailResponseDto>>

    /**
     * Lấy danh sách bookings theo venue ID (cho chủ sân)
     * @param venueId ID của venue
     * @param status Filter theo status (optional): PENDING_PAYMENT, PAYMENT_UPLOADED, CONFIRMED, etc.
     * @return Danh sách bookings của venue
     */
    @GET("bookings/venue/{venueId}")
    suspend fun getBookingsByVenue(
        @Path("venueId") venueId: Long,
        @Query("status") status: String? = null
    ): ApiResponse<List<BookingDetailResponseDto>>

    // Get booking detail with full information (for payment flow)
    @GET("bookings/{id}")
    suspend fun getBookingDetail(
        @Path("id") bookingId: String
    ): ApiResponse<BookingDetailResponseDto>

    // Get my bookings list
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): ApiResponse<List<BookingDetailResponseDto>>

    /**
     * Lấy các time slots đã được đặt (booked/pending) cho một venue trong ngày cụ thể
     * @param venueId ID của venue
     * @param date Ngày cần kiểm tra (format: yyyy-MM-dd)
     * @return Danh sách các booking với thông tin court và time slots
     */
    @GET("bookings/venue/{venueId}/booked-slots")
    suspend fun getBookedSlots(
        @Path("venueId") venueId: Long,
        @Query("date") date: String
    ): ApiResponse<List<BookedSlotDto>>
}
