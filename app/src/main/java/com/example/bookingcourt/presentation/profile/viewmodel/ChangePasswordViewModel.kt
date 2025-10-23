package com.example.bookingcourt.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            authRepository.changePassword(currentPassword, newPassword).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _changePasswordState.value = ChangePasswordState.Loading
                    }
                    is Resource.Success -> {
                        _changePasswordState.value = ChangePasswordState.Success
                    }
                    is Resource.Error -> {
                        _changePasswordState.value = ChangePasswordState.Error(
                            resource.message ?: "Đã xảy ra lỗi"
                        )
                    }
                }
            }
        }
    }

    fun resetState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

