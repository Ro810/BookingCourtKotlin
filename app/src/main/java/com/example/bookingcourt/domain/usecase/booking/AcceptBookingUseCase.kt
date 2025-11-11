package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case cho owner chấp nhận booking
 * Theo API: PUT /bookings/{id}/accept
 *
 * Flow:
 * 1. Owner xem danh sách pending bookings
 * 2. Owner xem chi tiết booking + ảnh chuyển khoản
 * 3. Owner nhấn "Chấp nhận" → gọi use case này
 * 4. Status chuyển từ PAYMENT_UPLOADED → CONFIRMED
 * 5. User nhận thông báo "Đặt sân thành công"
 */
class AcceptBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String
    ): Flow<Resource<BookingDetail>> {
        return bookingRepository.acceptBooking(bookingId)
    }
}

