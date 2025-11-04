package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Booking
import com.example.bookingcourt.domain.model.BookingWithBankInfo
import kotlinx.coroutines.flow.Flow

interface BookingRepository {

    /**
     * Tạo booking mới - trả về thông tin booking kèm thông tin ngân hàng của chủ sân
     */
    suspend fun createBooking(
        courtId: String,
        startTime: String,
        endTime: String,
        notes: String?,
        paymentMethod: String
    ): Flow<Resource<BookingWithBankInfo>>

    /**
     * Lấy danh sách booking của user
     */
    suspend fun getUserBookings(
        page: Int,
        size: Int,
        status: String? = null
    ): Flow<Resource<List<Booking>>>

    /**
     * Lấy chi tiết booking theo ID
     */
    suspend fun getBookingById(bookingId: String): Flow<Resource<Booking>>

    /**
     * Hủy booking
     */
    suspend fun cancelBooking(
        bookingId: String,
        reason: String
    ): Flow<Resource<Unit>>

    /**
     * Xác nhận booking (cho owner)
     */
    suspend fun confirmBooking(bookingId: String): Flow<Resource<Booking>>

    /**
     * Lấy các booking sắp tới
     */
    suspend fun getUpcomingBookings(): Flow<Resource<List<Booking>>>
}
