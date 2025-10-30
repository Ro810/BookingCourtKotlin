package com.example.bookingcourt.presentation.auth.viewmodel

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

sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    data class Success(val message: String) : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            try {
                authRepository.resetPassword(token, newPassword).collect { res ->
                    when (res) {
                        is Resource.Loading -> _uiState.value = ResetPasswordUiState.Loading
                        is Resource.Success -> _uiState.value = ResetPasswordUiState.Success(res.data ?: "Mật khẩu đã được đặt lại thành công")
                        is Resource.Error -> _uiState.value = ResetPasswordUiState.Error(res.message ?: "Đã xảy ra lỗi")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ResetPasswordUiState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUiState.Idle
    }
}
