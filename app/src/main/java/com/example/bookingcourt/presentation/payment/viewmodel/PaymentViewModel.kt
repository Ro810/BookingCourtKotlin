package com.example.bookingcourt.presentation.payment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingWithBankInfo
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _createBookingState = MutableStateFlow<Resource<BookingWithBankInfo>?>(null)
    val createBookingState: StateFlow<Resource<BookingWithBankInfo>?> = _createBookingState.asStateFlow()

    /**
     * Tạo booking mới - được gọi từ PaymentScreen khi user xác nhận thanh toán
     */
    fun createBooking(
        courtId: String,
        startTime: String,
        endTime: String,
        notes: String? = null,
        paymentMethod: String = "BANK_TRANSFER"
    ) {
        viewModelScope.launch {
            bookingRepository.createBooking(
                courtId = courtId,
                startTime = startTime,
                endTime = endTime,
                notes = notes,
                paymentMethod = paymentMethod
            ).collect { result ->
                _createBookingState.value = result
            }
        }
    }

    /**
     * Reset state về null
     */
    fun resetCreateBookingState() {
        _createBookingState.value = null
    }
}

