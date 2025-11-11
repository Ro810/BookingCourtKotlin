package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case cho owner từ chối booking
 * Theo API: PUT /bookings/{id}/reject
 *
 * Flow:
 * 1. Owner xem chi tiết booking
 * 2. Owner nhấn "Từ chối" → hiển thị dialog nhập lý do
 * 3. Owner nhập lý do → gọi use case này
 * 4. Status chuyển từ PAYMENT_UPLOADED → REJECTED
 * 5. User nhận thông báo "Bị từ chối" + rejectionReason
 */
class RejectBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        rejectionReason: String
    ): Flow<Resource<BookingDetail>> {
        return bookingRepository.rejectBooking(bookingId, rejectionReason)
    }
}

