package com.example.bookingcourt.presentation.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.BookingApi
import com.example.bookingcourt.data.remote.dto.CreateBookingResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingBookingsViewModel @Inject constructor(
    private val bookingApi: BookingApi
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<CreateBookingResponseDto>>>(Resource.Loading())
    val state: StateFlow<Resource<List<CreateBookingResponseDto>>> = _state.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>?>(null)
    val actionState: StateFlow<Resource<Unit>?> = _actionState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            try {
                val response = bookingApi.getPendingBookings()
                if (response.isSuccessful) {
                    _state.value = Resource.Success(response.body()?.data ?: emptyList())
                } else {
                    _state.value = Resource.Error("Lỗi tải danh sách chờ: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = Resource.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun accept(id: Long) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            try {
                val response = bookingApi.acceptBooking(id)
                if (response.isSuccessful) {
                    _actionState.value = Resource.Success(Unit)
                    load()
                } else {
                    _actionState.value = Resource.Error("Lỗi xác nhận: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun reject(id: Long, reason: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            try {
                val response = bookingApi.rejectBooking(id, mapOf("rejectionReason" to reason))
                if (response.isSuccessful) {
                    _actionState.value = Resource.Success(Unit)
                    load()
                } else {
                    _actionState.value = Resource.Error("Lỗi từ chối: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun resetAction() { _actionState.value = null }
}

