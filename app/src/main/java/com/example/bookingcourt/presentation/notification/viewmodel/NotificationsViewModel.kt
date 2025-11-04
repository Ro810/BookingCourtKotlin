package com.example.bookingcourt.presentation.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.NotificationApi
import com.example.bookingcourt.data.remote.dto.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationApi: NotificationApi
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<NotificationDto>>>(Resource.Loading())
    val state: StateFlow<Resource<List<NotificationDto>>> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            try {
                val response = notificationApi.getMyNotifications()
                if (response.isSuccessful) {
                    _state.value = Resource.Success(response.body()?.data ?: emptyList())
                } else {
                    _state.value = Resource.Error("Lỗi tải thông báo: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = Resource.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationApi.markAllAsRead()
                load()
            } catch (_: Exception) { }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            try {
                notificationApi.markAsRead(id)
                load()
            } catch (_: Exception) { }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                notificationApi.delete(id)
                load()
            } catch (_: Exception) { }
        }
    }
}

