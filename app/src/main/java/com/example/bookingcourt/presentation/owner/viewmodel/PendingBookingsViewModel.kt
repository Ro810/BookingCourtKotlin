package com.example.bookingcourt.presentation.owner.viewmodel

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
        loadPendingBookings()
    }

    fun loadPendingBookings() {
        viewModelScope.launch {
            bookingRepository.getPendingBookings().collect { resource ->
                _pendingBookings.value = resource
            }
        }
    }

    fun refreshPendingBookings() {
        loadPendingBookings()
    }
}
