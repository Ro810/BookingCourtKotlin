package com.example.bookingcourt.domain.usecase.booking

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

/**
 * Use case cho việc upload ảnh chuyển khoản
 * Theo API: POST /bookings/{id}/upload-payment-proof
 */
class UploadPaymentProofUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        imageFile: File
    ): Flow<Resource<BookingDetail>> {
        return bookingRepository.uploadPaymentProof(bookingId, imageFile)
    }
}

