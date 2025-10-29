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

    // Validation error states for individual fields
    data class ValidationErrors(
        val fullNameError: String? = null,
        val emailError: String? = null,
        val phoneError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
    )

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _validationErrors = MutableStateFlow(ValidationErrors())
    val validationErrors: StateFlow<ValidationErrors> = _validationErrors.asStateFlow()

    private fun normalizePhone(raw: String): String {
        // Keep digits only; drop +, spaces, hyphens, etc.
        return raw.filter { it.isDigit() }
    }

    private fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Vui lòng nhập họ và tên"
            fullName.length < 2 -> "Họ và tên phải có ít nhất 2 ký tự"
            fullName.length > 100 -> "Họ và tên không được vượt quá 100 ký tự"
            !fullName.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$")) -> "Họ và tên chỉ được chứa chữ cái"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Vui lòng nhập email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không hợp lệ"
            email.length > 100 -> "Email không được vượt quá 100 ký tự"
            else -> null
        }
    }

    private fun validatePhone(phone: String): String? {
        val normalized = normalizePhone(phone)
        return when {
            phone.isBlank() -> "Vui lòng nhập số điện thoại"
            normalized.length < 8 -> "Số điện thoại phải có ít nhất 8 chữ số"
            normalized.length > 15 -> "Số điện thoại không được vượt quá 15 chữ số"
            !normalized.matches(Regex("^[0-9]+$")) -> "Số điện thoại chỉ được chứa chữ số"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Vui lòng nhập mật khẩu"
            password.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
            password.length > 50 -> "Mật khẩu không được vượt quá 50 ký tự"
            !password.any { it.isDigit() } -> "Mật khẩu phải chứa ít nhất 1 chữ số"
            !password.any { it.isLetter() } -> "Mật khẩu phải chứa ít nhất 1 chữ cái"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Vui lòng xác nhận mật khẩu"
            password != confirmPassword -> "Mật khẩu xác nhận không khớp"
            else -> null
        }
    }

    fun validateField(field: String, value: String, compareValue: String? = null) {
        val currentErrors = _validationErrors.value
        _validationErrors.value = when (field) {
            "fullName" -> currentErrors.copy(fullNameError = validateFullName(value))
            "email" -> currentErrors.copy(emailError = validateEmail(value))
            "phone" -> currentErrors.copy(phoneError = validatePhone(value))
            "password" -> currentErrors.copy(passwordError = validatePassword(value))
            "confirmPassword" -> currentErrors.copy(
                confirmPasswordError = validateConfirmPassword(compareValue ?: "", value)
            )
            else -> currentErrors
        }
    }

    fun clearFieldError(field: String) {
        val currentErrors = _validationErrors.value
        _validationErrors.value = when (field) {
            "fullName" -> currentErrors.copy(fullNameError = null)
            "email" -> currentErrors.copy(emailError = null)
            "phone" -> currentErrors.copy(phoneError = null)
            "password" -> currentErrors.copy(passwordError = null)
            "confirmPassword" -> currentErrors.copy(confirmPasswordError = null)
            else -> currentErrors
        }
    }

    private fun validateAllFields(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val fullNameError = validateFullName(fullName)
        val emailError = validateEmail(email)
        val phoneError = validatePhone(phoneNumber)
        val passwordError = validatePassword(password)
        val confirmPasswordError = validateConfirmPassword(password, confirmPassword)

        _validationErrors.value = ValidationErrors(
            fullNameError = fullNameError,
            emailError = emailError,
            phoneError = phoneError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )

        return fullNameError == null && emailError == null && phoneError == null &&
                passwordError == null && confirmPasswordError == null
    }

    fun register(fullName: String, email: String, phoneNumber: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Validate all fields first
            if (!validateAllFields(fullName, email, phoneNumber, password, confirmPassword)) {
                _registerState.value = RegisterState.Error("Vui lòng kiểm tra lại thông tin nhập vào")
                return@launch
            }

            _registerState.value = RegisterState.Loading

            val normalizedPhone = normalizePhone(phoneNumber)

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
                        // Parse error message to provide better user feedback
                        val errorMessage = parseErrorMessage(result.message ?: "Đăng ký thất bại")
                        _registerState.value = RegisterState.Error(errorMessage)
                    }
                    is Resource.Loading -> {
                        _registerState.value = RegisterState.Loading
                    }
                }
            }
        }
    }

    private fun parseErrorMessage(message: String): String {
        return when {
            // Backend-specific messages (already parsed from JSON) - check these FIRST
            message.contains("Số điện thoại đã tồn tại", ignoreCase = true) ||
                message.contains("Số điện thoại đã được sử dụng", ignoreCase = true) ||
                message.contains("Phone already exists", ignoreCase = true) ||
                message.contains("Phone number already exists", ignoreCase = true) ||
                message.contains("duplicate.*phone", ignoreCase = true) ->
                "Số điện thoại này đã được sử dụng"

            message.contains("Email đã tồn tại", ignoreCase = true) ||
                message.contains("Email đã được sử dụng", ignoreCase = true) ||
                message.contains("Email already exists", ignoreCase = true) ||
                message.contains("duplicate.*email", ignoreCase = true) ->
                "Email này đã được sử dụng"

            message.contains("Tên đăng nhập đã tồn tại", ignoreCase = true) ||
                message.contains("Username already exists", ignoreCase = true) ->
                "Tài khoản này đã tồn tại"

            // Validation errors
            message.contains("invalid email", ignoreCase = true) ||
                message.contains("email không hợp lệ", ignoreCase = true) ->
                "Email không hợp lệ"

            message.contains("invalid phone", ignoreCase = true) ||
                message.contains("số điện thoại không hợp lệ", ignoreCase = true) ->
                "Số điện thoại không hợp lệ"

            message.contains("password", ignoreCase = true) &&
                (message.contains("weak", ignoreCase = true) ||
                 message.contains("short", ignoreCase = true)) ->
                "Mật khẩu quá yếu, vui lòng chọn mật khẩu mạnh hơn"

            message.contains("password", ignoreCase = true) &&
                message.contains("match", ignoreCase = true) ->
                "Mật khẩu xác nhận không khớp"

            // Network/connection errors
            message.contains("network", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ||
                message.contains("unable to resolve host", ignoreCase = true) ->
                "Lỗi kết nối mạng, vui lòng thử lại"

            // If message is clean (from parseErrorResponse), use it directly
            // This handles cases where backend returns specific Vietnamese messages
            message.length in 10..100 &&
                !message.contains("Lỗi kết nối:", ignoreCase = true) &&
                !message.contains("500") &&
                !message.contains("400") &&
                !message.contains("401") &&
                !message.contains("403") ->
                message

            // Default fallback
            else -> "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin"
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
        _validationErrors.value = ValidationErrors()
    }
}
