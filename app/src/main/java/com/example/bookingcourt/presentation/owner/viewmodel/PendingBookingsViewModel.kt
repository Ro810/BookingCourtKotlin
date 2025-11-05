package com.example.bookingcourt.presentation.owner.viewmodel

import android.util.Log
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
class PendingBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _pendingBookings = MutableStateFlow<Resource<List<BookingDetail>>>(Resource.Loading())
    val pendingBookings: StateFlow<Resource<List<BookingDetail>>> = _pendingBookings.asStateFlow()

    init {
        Log.d("PendingBookingsVM", "🚀 ViewModel initialized - Loading pending bookings...")
        loadPendingBookings()
    }

    fun loadPendingBookings() {
        Log.d("PendingBookingsVM", "📥 Loading pending bookings from repository...")
        viewModelScope.launch {
            bookingRepository.getPendingBookings().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        Log.d("PendingBookingsVM", "⏳ Loading state...")
                    }
                    is Resource.Success -> {
                        val count = resource.data?.size ?: 0
                        Log.d("PendingBookingsVM", "✅ Success! Found $count pending bookings")
                        resource.data?.forEachIndexed { index, booking ->
                            Log.d("PendingBookingsVM", "  📋 Booking ${index + 1}: ID=${booking.id}, Court=${booking.court.description}, User=${booking.user.fullname}, Status=${booking.status}")
                        }
                    }
                    is Resource.Error -> {
                        Log.e("PendingBookingsVM", "❌ Error loading pending bookings: ${resource.message}")
                    }
                }
                _pendingBookings.value = resource
            }
        }
    }

    fun refreshPendingBookings() {
        Log.d("PendingBookingsVM", "🔄 Refreshing pending bookings...")
        loadPendingBookings()
    }
}
