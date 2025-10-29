package com.example.bookingcourt.presentation.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.BecomeOwnerRequest
import com.example.bookingcourt.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BecomeOwnerState(
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val bankNameError: String? = null,
    val accountNumberError: String? = null,
    val accountHolderNameError: String? = null,
    val hasBankInfo: Boolean? = null, // null means checking, true/false means checked
)

sealed class BecomeOwnerEvent {
    data object NavigateToLogin : BecomeOwnerEvent() // Changed from NavigateToOwnerHome
    data class ShowError(val message: String) : BecomeOwnerEvent()
}

@HiltViewModel
class BecomeOwnerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BecomeOwnerState())
    val state: StateFlow<BecomeOwnerState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<BecomeOwnerEvent>()
    val event = _event.asSharedFlow()

    init {
        checkBankInfo()
    }

    /**
     * Check if user already has bank information filled
     */
    private fun checkBankInfo() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        val hasBankInfo = user?.bankName != null &&
                                          user.bankAccountNumber != null &&
                                          user.bankAccountName != null
                        _state.value = _state.value.copy(hasBankInfo = hasBankInfo)
                    }
                    is Resource.Error -> {
                        // If we can't get user info, assume no bank info
                        _state.value = _state.value.copy(hasBankInfo = false)
                    }
                    is Resource.Loading -> {
                        // Keep hasBankInfo as null while loading
                    }
                }
            }
        }
    }

    fun onBankNameChange(value: String) {
        _state.value = _state.value.copy(
            bankName = value,
            bankNameError = null
        )
    }

    fun onAccountNumberChange(value: String) {
        _state.value = _state.value.copy(
            accountNumber = value,
            accountNumberError = null
        )
    }

    fun onAccountHolderNameChange(value: String) {
        _state.value = _state.value.copy(
            accountHolderName = value,
            accountHolderNameError = null
        )
    }

    /**
     * Request owner role directly without updating bank info
     * Used when user already has bank information filled
     */
    fun requestOwnerRoleDirectly() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            authRepository.requestOwnerRole().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false)
                        _event.emit(
                            BecomeOwnerEvent.ShowError(
                                result.data ?: "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại."
                            )
                        )
                        // Wait a bit then navigate
                        kotlinx.coroutines.delay(2000)
                        _event.emit(BecomeOwnerEvent.NavigateToLogin) // Changed to NavigateToLogin
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                        _event.emit(
                            BecomeOwnerEvent.ShowError(
                                result.message ?: "Không thể nâng cấp lên chủ sân"
                            )
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun onSubmit() {
        // Validate
        val currentState = _state.value
        var hasError = false

        if (currentState.bankName.isBlank()) {
            _state.value = currentState.copy(bankNameError = "Vui lòng nhập tên ngân hàng")
            hasError = true
        }

        if (currentState.accountNumber.isBlank()) {
            _state.value = currentState.copy(accountNumberError = "Vui lòng nhập số tài khoản")
            hasError = true
        }

        if (currentState.accountHolderName.isBlank()) {
            _state.value = currentState.copy(accountHolderNameError = "Vui lòng nhập tên chủ tài khoản")
            hasError = true
        }

        if (hasError) return

        // Submit to API - 2 bước:
        // Bước 1: Cập nhật thông tin ngân hàng
        // Bước 2: Yêu cầu nâng cấp lên chủ sân
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Bước 1: Cập nhật thông tin ngân hàng
            authRepository.updateBankInfo(
                bankName = currentState.bankName,
                accountNumber = currentState.accountNumber,
                accountHolderName = currentState.accountHolderName
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Thông tin ngân hàng đã được cập nhật thành công
                        // Tiếp tục Bước 2: Yêu cầu nâng cấp lên chủ sân
                        authRepository.requestOwnerRole().collect { ownerRoleResult ->
                            when (ownerRoleResult) {
                                is Resource.Success -> {
                                    _state.value = _state.value.copy(isLoading = false)
                                    // Hiển thị thông báo thành công và yêu cầu đăng nhập lại
                                    _event.emit(
                                        BecomeOwnerEvent.ShowError(
                                            ownerRoleResult.data ?: "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại."
                                        )
                                    )
                                    // Wait then navigate to login
                                    kotlinx.coroutines.delay(2000)
                                    _event.emit(BecomeOwnerEvent.NavigateToLogin)
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        error = ownerRoleResult.message
                                    )
                                    _event.emit(
                                        BecomeOwnerEvent.ShowError(
                                            ownerRoleResult.message ?: "Không thể nâng cấp lên chủ sân"
                                        )
                                    )
                                }
                                is Resource.Loading -> {
                                    // Vẫn đang loading
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                        _event.emit(
                            BecomeOwnerEvent.ShowError(
                                result.message ?: "Không thể cập nhật thông tin ngân hàng"
                            )
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
}
