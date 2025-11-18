package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy tất cả bookings của owner (cho màn hình lịch sử)
 * Trả về tất cả booking thuộc các venue của owner
 * Bao gồm: CONFIRMED, PAYMENT_UPLOADED, REJECTED
 *
 * Phương pháp:
 * 1. Lấy danh sách venue của owner
 * 2. Với mỗi venue, gọi API /bookings/venue/{venueId} để lấy TẤT CẢ booking
 * 3. Merge và sắp xếp kết quả theo thời gian
 */
class GetOwnerBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<BookingDetail>>> {
        return bookingRepository.getAllOwnerBookings()
    }
}
