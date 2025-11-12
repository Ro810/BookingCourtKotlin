package com.example.bookingcourt.presentation.booking.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle["bookingId"] ?: ""

    private val _bookingDetail = MutableStateFlow<Resource<BookingDetail>>(Resource.Loading())
    val bookingDetail: StateFlow<Resource<BookingDetail>> = _bookingDetail.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<String>?>(null)
    val uploadState: StateFlow<Resource<String>?> = _uploadState.asStateFlow()

    private val _confirmState = MutableStateFlow<Resource<BookingDetail>?>(null)
    val confirmState: StateFlow<Resource<BookingDetail>?> = _confirmState.asStateFlow()

    private val _timeRemaining = MutableStateFlow<Long>(0L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    init {
        loadBookingDetail()
    }

    fun loadBookingDetail() {
        viewModelScope.launch {
            bookingRepository.getBookingDetail(bookingId).collect { resource ->
                _bookingDetail.value = resource
                if (resource is Resource.Success) {
                    resource.data?.expireTime?.let { expireTime ->
                        startCountdown(expireTime)
                    }
                }
            }
        }
    }

    private fun startCountdown(expireTime: kotlinx.datetime.LocalDateTime) {
        viewModelScope.launch {
            while (true) {
                val now = Clock.System.now()
                val expire = expireTime.toInstant(TimeZone.currentSystemDefault())
                val remaining = (expire - now).inWholeMilliseconds

                if (remaining <= 0) {
                    _timeRemaining.value = 0
                    break
                }

                _timeRemaining.value = remaining
                delay(1000)
            }
        }
    }

    fun uploadPaymentProof(imageFile: File) {
        viewModelScope.launch {
            bookingRepository.uploadPaymentProof(bookingId, imageFile).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uploadState.value = Resource.Success(resource.data?.paymentProofUrl ?: "")
                        loadBookingDetail() // Refresh booking detail
                    }
                    is Resource.Error -> {
                        _uploadState.value = Resource.Error(resource.message ?: "Lỗi upload")
                    }
                    is Resource.Loading -> {
                        _uploadState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun confirmPayment(paymentProofUrl: String) {
        viewModelScope.launch {
            bookingRepository.confirmPayment(bookingId, paymentProofUrl).collect { resource ->
                _confirmState.value = resource
            }
        }
    }

    fun resetConfirmState() {
        _confirmState.value = null
    }

    fun resetUploadState() {
        _uploadState.value = null
    }
}
