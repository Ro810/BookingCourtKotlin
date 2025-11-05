package com.example.bookingcourt.presentation.booking.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BookingDetail
import com.example.bookingcourt.domain.model.BookingStatus
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentWaitingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = savedStateHandle["bookingId"] ?: ""

    private val _bookingDetail = MutableStateFlow<Resource<BookingDetail>?>(null)
    val bookingDetail: StateFlow<Resource<BookingDetail>?> = _bookingDetail.asStateFlow()

    private val _bookingStatus = MutableStateFlow<BookingStatus?>(null)
    val bookingStatus: StateFlow<BookingStatus?> = _bookingStatus.asStateFlow()

    private val _rejectionReason = MutableStateFlow<String?>(null)
    val rejectionReason: StateFlow<String?> = _rejectionReason.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            Log.d("PaymentWaiting", "========== START POLLING BOOKING DETAIL ==========")
            Log.d("PaymentWaiting", "  📋 Booking ID from navigation: $bookingId")
            Log.d("PaymentWaiting", "====================================================")

            var shouldContinuePolling = true

            while (shouldContinuePolling) {
                try {
                    bookingRepository.getBookingDetail(bookingId).collect { resource ->
                        _bookingDetail.value = resource

                        if (resource is Resource.Success) {
                            val detail = resource.data

                            // ✅ LOG CHI TIẾT để so sánh với thông tin lúc tạo booking
                            Log.d("PaymentWaiting", "========== BOOKING DETAIL LOADED ==========")
                            Log.d("PaymentWaiting", "  📋 Booking ID: ${detail?.id}")
                            Log.d("PaymentWaiting", "  🏟️ Court ID: ${detail?.court?.id}")
                            Log.d("PaymentWaiting", "  🏟️ Court Name: ${detail?.court?.description}")
                            Log.d("PaymentWaiting", "  🏢 Venue ID: ${detail?.venue?.id}")
                            Log.d("PaymentWaiting", "  🏢 Venue Name: ${detail?.venue?.name}")
                            Log.d("PaymentWaiting", "  💰 Total Price: ${detail?.totalPrice}")
                            Log.d("PaymentWaiting", "  🏦 Bank Name: ${detail?.ownerBankInfo?.bankName}")
                            Log.d("PaymentWaiting", "  🏦 Account Number: ${detail?.ownerBankInfo?.bankAccountNumber}")
                            Log.d("PaymentWaiting", "  🏦 Account Name: ${detail?.ownerBankInfo?.bankAccountName}")
                            Log.d("PaymentWaiting", "  ⏰ Start Time: ${detail?.startTime}")
                            Log.d("PaymentWaiting", "  ⏰ End Time: ${detail?.endTime}")
                            Log.d("PaymentWaiting", "  📊 Status: ${detail?.status}")
                            Log.d("PaymentWaiting", "===========================================")

                            val status = resource.data?.status
                            _bookingStatus.value = status

                            // Stop polling if booking is confirmed or rejected
                            if (status == BookingStatus.CONFIRMED || status == BookingStatus.REJECTED) {
                                _rejectionReason.value = resource.data?.rejectionReason
                                Log.d("PaymentWaiting", "⏹️ Stopped polling - Status: $status")
                                shouldContinuePolling = false
                            }
                        } else if (resource is Resource.Error) {
                            Log.e("PaymentWaiting", "❌ Error loading booking detail: ${resource.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PaymentWaiting", "❌ Exception during polling: ${e.message}")
                }

                // Only delay if we should continue polling
                if (shouldContinuePolling) {
                    Log.d("PaymentWaiting", "⏳ Waiting 3 seconds before next poll...")
                    delay(3000)
                }
            }

            Log.d("PaymentWaiting", "✅ Polling stopped")
        }
    }

    fun refreshBookingStatus() {
        viewModelScope.launch {
            Log.d("PaymentWaiting", "🔄 Manual refresh triggered")
            bookingRepository.getBookingDetail(bookingId).collect { resource ->
                _bookingDetail.value = resource
                if (resource is Resource.Success) {
                    _bookingStatus.value = resource.data?.status
                    _rejectionReason.value = resource.data?.rejectionReason
                    Log.d("PaymentWaiting", "✅ Manual refresh completed - Status: ${resource.data?.status}")
                }
            }
        }
    }
}
