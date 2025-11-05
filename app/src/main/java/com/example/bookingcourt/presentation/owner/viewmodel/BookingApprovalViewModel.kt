package com.example.bookingcourt.presentation.owner.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingApprovalViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle["bookingId"] ?: ""

    private val _bookingDetail = MutableStateFlow<Resource<BookingDetail>>(Resource.Loading())
    val bookingDetail: StateFlow<Resource<BookingDetail>> = _bookingDetail.asStateFlow()

    private val _acceptState = MutableStateFlow<Resource<BookingDetail>?>(null)
    val acceptState: StateFlow<Resource<BookingDetail>?> = _acceptState.asStateFlow()

    private val _rejectState = MutableStateFlow<Resource<BookingDetail>?>(null)
    val rejectState: StateFlow<Resource<BookingDetail>?> = _rejectState.asStateFlow()

    init {
        loadBookingDetail()
    }

    fun loadBookingDetail() {
        viewModelScope.launch {
            bookingRepository.getBookingDetail(bookingId).collect { resource ->
                _bookingDetail.value = resource
            }
        }
    }

    fun acceptBooking() {
        viewModelScope.launch {
            bookingRepository.acceptBooking(bookingId).collect { resource ->
                _acceptState.value = resource
                if (resource is Resource.Error) {
                    Log.e("BookingApprovalVM", "Accept error: ${resource.message}")
                }
            }
        }
    }

    fun rejectBooking(reason: String) {
        viewModelScope.launch {
            bookingRepository.rejectBooking(bookingId, reason).collect { resource ->
                _rejectState.value = resource
                if (resource is Resource.Error) {
                    Log.e("BookingApprovalVM", "Reject error: ${resource.message}")
                }
            }
        }
    }

    fun resetAcceptState() {
        _acceptState.value = null
    }

    fun resetRejectState() {
        _rejectState.value = null
    }
}

