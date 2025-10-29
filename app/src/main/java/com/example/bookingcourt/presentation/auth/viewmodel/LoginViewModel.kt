package com.example.bookingcourt.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.PlayingLevel
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.example.bookingcourt.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Login state definitions matching original UI
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    // Validation error states for individual fields
    data class ValidationErrors(
        val usernameError: String? = null,
        val passwordError: String? = null,
    )

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _validationErrors = MutableStateFlow(ValidationErrors())
    val validationErrors: StateFlow<ValidationErrors> = _validationErrors.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Vui lòng nhập tên đăng nhập"
            username.length < 3 -> "Tên đăng nhập phải có ít nhất 3 ký tự"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Vui lòng nhập mật khẩu"
            password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
            else -> null
        }
    }

    fun validateField(field: String, value: String) {
        val currentErrors = _validationErrors.value
        _validationErrors.value = when (field) {
            "username" -> currentErrors.copy(usernameError = validateUsername(value))
            "password" -> currentErrors.copy(passwordError = validatePassword(value))
            else -> currentErrors
        }
    }

    fun clearFieldError(field: String) {
        val currentErrors = _validationErrors.value
        _validationErrors.value = when (field) {
            "username" -> currentErrors.copy(usernameError = null)
            "password" -> currentErrors.copy(passwordError = null)
            else -> currentErrors
        }
    }

    private fun validateAllFields(username: String, password: String): Boolean {
        val usernameError = validateUsername(username)
        val passwordError = validatePassword(password)

        _validationErrors.value = ValidationErrors(
            usernameError = usernameError,
            passwordError = passwordError
        )

        return usernameError == null && passwordError == null
    }

    fun login(username: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            // Validate fields first
            if (!validateAllFields(username, password)) {
                _loginState.value = LoginState.Error("Vui lòng kiểm tra lại thông tin đăng nhập")
                return@launch
            }

            _loginState.value = LoginState.Loading

            // API call - THẬT
            authRepository.login(username, password).collect { result ->
                when (result) {
                    is com.example.bookingcourt.core.common.Resource.Success -> {
                        _loginState.value = LoginState.Success(result.data!!)
                        if (rememberMe) {
                            // Save credentials for remember me functionality
                            // This would typically save to SharedPreferences or DataStore
                        }
                        _uiEvent.emit(UiEvent.NavigateTo("home"))
                    }
                    is com.example.bookingcourt.core.common.Resource.Error -> {
                        val errorMessage = parseErrorMessage(result.message ?: "Đăng nhập thất bại")
                        _loginState.value = LoginState.Error(errorMessage)
                    }
                    is com.example.bookingcourt.core.common.Resource.Loading -> {
                        _loginState.value = LoginState.Loading
                    }
                }
            }
        }
    }

    private fun parseErrorMessage(message: String): String {
        return when {
            message.contains("invalid credentials", ignoreCase = true) ||
                message.contains("wrong password", ignoreCase = true) ||
                message.contains("incorrect password", ignoreCase = true) ->
                "Tên đăng nhập hoặc mật khẩu không đúng"
            message.contains("user not found", ignoreCase = true) ||
                message.contains("account not found", ignoreCase = true) ->
                "Tài khoản không tồn tại"
            message.contains("account locked", ignoreCase = true) ||
                message.contains("account disabled", ignoreCase = true) ->
                "Tài khoản đã bị khóa"
            message.contains("not verified", ignoreCase = true) ->
                "Tài khoản chưa được xác thực, vui lòng kiểm tra email"
            message.contains("network", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ->
                "Lỗi kết nối mạng, vui lòng thử lại"
            message.contains("500") ->
                "Lỗi máy chủ, vui lòng thử lại sau"
            message.contains("400") ->
                "Thông tin đăng nhập không hợp lệ"
            message.contains("401") || message.contains("403") ->
                "Tên đăng nhập hoặc mật khẩu không đúng"
            else -> message
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
        _validationErrors.value = ValidationErrors()
    }
}
