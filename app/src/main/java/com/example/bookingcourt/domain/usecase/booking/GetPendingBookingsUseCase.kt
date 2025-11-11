package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy danh sách booking chờ xác nhận (cho owner)
 * Theo API: GET /bookings/pending
 *
 * Response: Danh sách booking có status = PAYMENT_UPLOADED
 * (User đã upload ảnh, chờ owner xác nhận)
 */
class GetPendingBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<BookingDetail>>> {
        return bookingRepository.getPendingBookings()
    }
}

