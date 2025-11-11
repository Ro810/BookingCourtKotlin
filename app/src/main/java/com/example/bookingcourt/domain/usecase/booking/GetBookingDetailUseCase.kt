package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy chi tiết booking (phiên bản đầy đủ cho payment flow)
 * Theo API: GET /bookings/{id}
 *
 * Response bao gồm:
 * - Thông tin user (fullname, phone)
 * - Thông tin court & venue
 * - Payment proof (url, uploadedAt)
 * - Rejection reason (nếu bị từ chối)
 * - Owner bank info
 * - Expire time
 */
class GetBookingDetailUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String
    ): Flow<Resource<BookingDetail>> {
        return bookingRepository.getBookingDetail(bookingId)
    }
}

