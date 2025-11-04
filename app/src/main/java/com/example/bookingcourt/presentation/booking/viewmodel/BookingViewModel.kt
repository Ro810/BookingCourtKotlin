package com.example.bookingcourt.presentation.booking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Booking
import com.example.bookingcourt.domain.model.BookingWithBankInfo
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _createBookingState = MutableStateFlow<Resource<BookingWithBankInfo>?>(null)
    val createBookingState: StateFlow<Resource<BookingWithBankInfo>?> = _createBookingState.asStateFlow()

    private val _bookingsState = MutableStateFlow<Resource<List<Booking>>?>(null)
    val bookingsState: StateFlow<Resource<List<Booking>>?> = _bookingsState.asStateFlow()

    private val _bookingDetailState = MutableStateFlow<Resource<Booking>?>(null)
    val bookingDetailState: StateFlow<Resource<Booking>?> = _bookingDetailState.asStateFlow()

    /**
     * Tạo booking mới - trả về thông tin booking kèm thông tin ngân hàng của chủ sân
     * @param courtId: ID của court (CHÚ Ý: đây là courtId, không phải venueId)
     * @param startTime: Thời gian bắt đầu (format: "2025-10-28T10:00:00")
     * @param endTime: Thời gian kết thúc (format: "2025-10-28T11:00:00")
     * @param notes: Ghi chú (optional)
     * @param paymentMethod: Phương thức thanh toán (BANK_TRANSFER, CASH, etc.)
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
     * Lấy danh sách booking của user
     */
    fun getUserBookings(
        page: Int = 1,
        size: Int = 10,
        status: String? = null
    ) {
        viewModelScope.launch {
            bookingRepository.getUserBookings(page, size, status).collect { result ->
                _bookingsState.value = result
            }
        }
    }

    /**
     * Lấy chi tiết booking theo ID
     */
    fun getBookingById(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.getBookingById(bookingId).collect { result ->
                _bookingDetailState.value = result
            }
        }
    }

    /**
     * Hủy booking
     */
    fun cancelBooking(bookingId: String, reason: String) {
        viewModelScope.launch {
            bookingRepository.cancelBooking(bookingId, reason).collect { result ->
                // Sau khi hủy thành công, reload lại danh sách booking
                if (result is Resource.Success) {
                    getUserBookings()
                }
            }
        }
    }

    /**
     * Xác nhận booking (cho owner)
     */
    fun confirmBooking(bookingId: String) {
        viewModelScope.launch {
            bookingRepository.confirmBooking(bookingId).collect { result ->
                _bookingDetailState.value = result
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
