package com.example.bookingcourt.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Register state definitions matching original UI
    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private fun normalizePhone(raw: String): String {
        // Keep digits only; drop +, spaces, hyphens, etc.
        return raw.filter { it.isDigit() }
    }

    fun register(fullName: String, email: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            val normalizedPhone = normalizePhone(phoneNumber)
            // Validate 8-15 digits as backend requires
            if (normalizedPhone.length !in 8..15) {
                _registerState.value = RegisterState.Error("Số điện thoại phải gồm 8-15 chữ số")
                return@launch
            }

            // Match the repository interface order: username, email, password, fullName, phone
            authRepository.register(
                username = normalizedPhone, // Using phone (digits only) as username
                email = email,
                password = password,
                fullName = fullName,
                phone = normalizedPhone,
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _registerState.value = RegisterState.Success(result.data!!)
                    }
                    is Resource.Error -> {
                        _registerState.value = RegisterState.Error(result.message ?: "Đăng ký thất bại")
                    }
                    is Resource.Loading -> {
                        _registerState.value = RegisterState.Loading
                    }
                }
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}
