package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.BaseResponseDto
import com.example.bookingcourt.data.remote.dto.BookingDto
import com.example.bookingcourt.data.remote.dto.BookingListResponseDto
import com.example.bookingcourt.data.remote.dto.CreateBookingRequestDto
import com.example.bookingcourt.data.remote.dto.CreateBookingResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): BookingDto

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String,
        @Body reason: Map<String, String>,
    ): BaseResponseDto

    @PUT("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: String): BookingDto

    @GET("bookings/upcoming")
    suspend fun getUpcomingBookings(): List<BookingDto>
}
