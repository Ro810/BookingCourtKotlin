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

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun login(username: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // Mock login success - comment out when API is ready
            // Simulate network delay
            delay(1000)

            // Create mock user with proper data
            val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val mockUser = User(
                id = "mock_user_1",
                email = if (username.contains("@")) username else "$username@example.com",
                fullName = "Test User",
                phoneNumber = "0123456789",
                avatar = null,
                role = UserRole.USER, // Đã sửa từ CUSTOMER
                isVerified = true,
                createdAt = currentTime,
                updatedAt = currentTime,
                favoriteCourtIds = emptyList(),
                playingLevel = PlayingLevel.INTERMEDIATE,
                preferredSports = listOf(SportType.BADMINTON),
            )

            _loginState.value = LoginState.Success(mockUser)
            if (rememberMe) {
                // Save credentials for remember me functionality
                // This would typically save to SharedPreferences or DataStore
            }
            _uiEvent.emit(UiEvent.NavigateTo("home"))

            // Original API call code - uncomment when API is ready
            /*
            authRepository.login(username, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _loginState.value = LoginState.Success(result.data!!)
                        if (rememberMe) {
                            // Save credentials for remember me functionality
                            // This would typically save to SharedPreferences or DataStore
                        }
                        _uiEvent.emit(UiEvent.NavigateTo("home"))
                    }
                    is Resource.Error -> {
                        _loginState.value = LoginState.Error(result.message ?: "Đăng nhập thất bại")
                    }
                    is Resource.Loading -> {
                        _loginState.value = LoginState.Loading
                    }
                }
            }
             */
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
