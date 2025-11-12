package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Booking
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.model.BookingWithBankInfo
import com.example.bookingcourt.domain.model.BookingItemData
import kotlinx.coroutines.flow.Flow
import java.io.File

interface BookingRepository {

    /**
     * ✅ Tạo booking nhiều sân - method mới
     */
    suspend fun createBookingMultipleCourts(
        bookingItems: List<BookingItemData>
    ): Flow<Resource<BookingWithBankInfo>>

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

    /**
     * Lấy TẤT CẢ bookings của user hiện tại (dùng endpoint my-bookings)
     */
    suspend fun getMyBookings(): Flow<Resource<List<Booking>>>

    // Payment confirmation flow methods

    /**
     * Upload ảnh chuyển khoản
     */
    suspend fun uploadPaymentProof(
        bookingId: String,
        imageFile: File
    ): Flow<Resource<BookingDetail>>

    /**
     * Xác nhận đã chuyển khoản (sau khi upload ảnh)
     */
    suspend fun confirmPayment(
        bookingId: String,
        paymentProofUrl: String
    ): Flow<Resource<BookingDetail>>

    /**
     * Owner chấp nhận booking
     */
    suspend fun acceptBooking(
        bookingId: String
    ): Flow<Resource<BookingDetail>>

    /**
     * Owner từ chối booking
     */
    suspend fun rejectBooking(
        bookingId: String,
        reason: String
    ): Flow<Resource<BookingDetail>>

    /**
     * Lấy danh sách booking chờ xác nhận (cho owner)
     */
    suspend fun getPendingBookings(): Flow<Resource<List<BookingDetail>>>

    /**
     * Lấy chi tiết booking (phiên bản mới với đầy đủ thông tin)
     */
    suspend fun getBookingDetail(
        bookingId: String
    ): Flow<Resource<BookingDetail>>

    /**
     * Lấy các time slots đã được đặt cho một venue trong ngày cụ thể
     * @param venueId ID của venue
     * @param date Ngày cần kiểm tra (format: yyyy-MM-dd)
     * @return Flow với danh sách các booked slots
     */
    suspend fun getBookedSlots(
        venueId: Long,
        date: String
    ): Flow<Resource<List<com.example.bookingcourt.domain.model.BookedSlot>>>
}
