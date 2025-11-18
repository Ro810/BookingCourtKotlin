package com.example.bookingcourt.presentation.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.domain.usecase.booking.GetOwnerBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình lịch sử booking của chủ sân
 * Quản lý 3 loại booking:
 * - Đã xác nhận (CONFIRMED + COMPLETED): Bao gồm cả booking đang chờ và đã hoàn thành
 * - Chờ xác nhận (PAYMENT_UPLOADED)
 * - Đã từ chối (REJECTED)
 */
@HiltViewModel
class OwnerBookingHistoryViewModel @Inject constructor(
    private val getOwnerBookingsUseCase: GetOwnerBookingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerBookingHistoryState())
    val state: StateFlow<OwnerBookingHistoryState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            getOwnerBookingsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        val allBookings = resource.data ?: emptyList()

                        // Phân loại booking theo status
                        // Tab "Đã xác nhận" bao gồm cả CONFIRMED và COMPLETED
                        val confirmed = allBookings.filter {
                            it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.COMPLETED
                        }
                        val pending = allBookings.filter { it.status == BookingStatus.PAYMENT_UPLOADED }
                        val rejected = allBookings.filter { it.status == BookingStatus.REJECTED }

                        _state.value = _state.value.copy(
                            isLoading = false,
                            confirmedBookings = confirmed,
                            pendingBookings = pending,
                            rejectedBookings = rejected,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadBookings()
    }
}

data class OwnerBookingHistoryState(
    val isLoading: Boolean = false,
    val confirmedBookings: List<BookingDetail> = emptyList(),
    val pendingBookings: List<BookingDetail> = emptyList(),
    val rejectedBookings: List<BookingDetail> = emptyList(),
    val error: String? = null
)
