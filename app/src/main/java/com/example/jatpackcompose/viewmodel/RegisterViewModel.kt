package com.example.jatpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jatpackcompose.data.model.RegisterResponse
import com.example.jatpackcompose.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val response: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(fullName: String, email: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val result = repository.register(fullName, email, phoneNumber, password)
                _registerState.value = if (result.isSuccess) {
                    RegisterState.Success(result.getOrNull()!!)
                } else {
                    RegisterState.Error(result.exceptionOrNull()?.message ?: "Đăng ký thất bại")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

