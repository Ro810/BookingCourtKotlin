package com.example.bookingcourt.presentation.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateVenueState(
    val isLoading: Boolean = false,
    val createdVenue: Venue? = null,
    val error: String? = null,
    val validationErrors: ValidationErrors = ValidationErrors(),
)

data class ValidationErrors(
    val nameError: String? = null,
    val phoneNumberError: String? = null,
    val emailError: String? = null,
    val provinceError: String? = null,
    val districtError: String? = null,
    val detailAddressError: String? = null,
)

@HiltViewModel
class CreateVenueViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateVenueState())
    val state: StateFlow<CreateVenueState> = _state.asStateFlow()

    fun createVenue(
        name: String,
        description: String,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
    ) {
        // Validate input
        val validationErrors = validateInput(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            provinceOrCity = provinceOrCity,
            district = district,
            detailAddress = detailAddress
        )

        if (validationErrors.hasErrors()) {
            _state.value = _state.value.copy(
                validationErrors = validationErrors
            )
            return
        }

        viewModelScope.launch {
            venueRepository.createVenue(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                phoneNumber = phoneNumber.trim(),
                email = email.trim(),
                provinceOrCity = provinceOrCity.trim(),
                district = district.trim(),
                detailAddress = detailAddress.trim()
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            createdVenue = result.data,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Đã xảy ra lỗi khi tạo sân"
                        )
                    }
                }
            }
        }
    }

    private fun validateInput(
        name: String,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
    ): ValidationErrors {
        var nameError: String? = null
        var phoneNumberError: String? = null
        var emailError: String? = null
        var provinceError: String? = null
        var districtError: String? = null
        var detailAddressError: String? = null

        // Validate name
        if (name.isBlank()) {
            nameError = "Tên sân không được để trống"
        }

        // Validate phone number
        if (phoneNumber.isBlank()) {
            phoneNumberError = "Số điện thoại không được để trống"
        } else if (!phoneNumber.matches(Regex("^0\\d{9,10}$"))) {
            phoneNumberError = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và có 10-11 chữ số)"
        }

        // Validate email
        if (email.isBlank()) {
            emailError = "Email không được để trống"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email không hợp lệ"
        }

        // Validate address
        if (provinceOrCity.isBlank()) {
            provinceError = "Tỉnh/Thành phố không được để trống"
        }

        if (district.isBlank()) {
            districtError = "Quận/Huyện không được để trống"
        }

        if (detailAddress.isBlank()) {
            detailAddressError = "Địa chỉ chi tiết không được để trống"
        }

        return ValidationErrors(
            nameError = nameError,
            phoneNumberError = phoneNumberError,
            emailError = emailError,
            provinceError = provinceError,
            districtError = districtError,
            detailAddressError = detailAddressError
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearValidationErrors() {
        _state.value = _state.value.copy(validationErrors = ValidationErrors())
    }

    fun reset() {
        _state.value = CreateVenueState()
    }
}

private fun ValidationErrors.hasErrors(): Boolean {
    return nameError != null ||
            phoneNumberError != null ||
            emailError != null ||
            provinceError != null ||
            districtError != null ||
            detailAddressError != null
}

