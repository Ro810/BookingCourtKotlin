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

                // Debug log
                if (resource is Resource.Success) {
                    val booking = resource.data
                    Log.d("BookingApprovalVM", "========== BOOKING DETAIL LOADED ==========")
                    Log.d("BookingApprovalVM", "Booking ID: ${booking?.id}")
                    Log.d("BookingApprovalVM", "User: ${booking?.user?.fullname}")
                    Log.d("BookingApprovalVM", "Phone: ${booking?.user?.phone ?: "NULL"}")
                    Log.d("BookingApprovalVM", "Payment Proof Uploaded: ${booking?.paymentProofUploaded}")
                    Log.d("BookingApprovalVM", "Payment Proof URL: ${booking?.paymentProofUrl ?: "NULL"}")
                    Log.d("BookingApprovalVM", "Payment Proof Uploaded At: ${booking?.paymentProofUploadedAt ?: "NULL"}")
                    Log.d("BookingApprovalVM", "==========================================")
                }
            }
        }
    }

    fun acceptBooking() {
        Log.d("BookingApprovalVM", "🔄 acceptBooking() called for booking: $bookingId")
        viewModelScope.launch {
            bookingRepository.acceptBooking(bookingId).collect { resource ->
                Log.d("BookingApprovalVM", "Accept state changed: ${resource::class.simpleName}")
                _acceptState.value = resource
                when (resource) {
                    is Resource.Success -> {
                        Log.d("BookingApprovalVM", "✅ Accept SUCCESS")
                    }
                    is Resource.Error -> {
                        Log.e("BookingApprovalVM", "❌ Accept ERROR: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        Log.d("BookingApprovalVM", "⏳ Accept LOADING")
                    }
                }
            }
        }
    }

    fun rejectBooking(reason: String) {
        Log.d("BookingApprovalVM", "🔄 rejectBooking() called for booking: $bookingId, reason: $reason")
        viewModelScope.launch {
            bookingRepository.rejectBooking(bookingId, reason).collect { resource ->
                Log.d("BookingApprovalVM", "Reject state changed: ${resource::class.simpleName}")
                _rejectState.value = resource
                when (resource) {
                    is Resource.Success -> {
                        Log.d("BookingApprovalVM", "✅ Reject SUCCESS")
                    }
                    is Resource.Error -> {
                        Log.e("BookingApprovalVM", "❌ Reject ERROR: ${resource.message}")
                    }
                    is Resource.Loading -> {
                        Log.d("BookingApprovalVM", "⏳ Reject LOADING")
                    }
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

