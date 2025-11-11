package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case cho việc xác nhận thanh toán (sau khi upload ảnh)
 * Theo API: PUT /bookings/{id}/confirm-payment
 *
 * Flow:
 * 1. User upload ảnh → nhận paymentProofUrl
 * 2. User preview ảnh
 * 3. User nhấn "Xác nhận thanh toán" → gọi use case này
 * 4. Status chuyển từ PENDING_PAYMENT → PAYMENT_UPLOADED
 * 5. Chờ owner xác nhận
 */
class ConfirmPaymentUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        paymentProofUrl: String
    ): Flow<Resource<BookingDetail>> {
        return bookingRepository.confirmPayment(bookingId, paymentProofUrl)
    }
}

