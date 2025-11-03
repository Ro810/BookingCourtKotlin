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
    val priceError: String? = null,
    val openingTimeError: String? = null,
    val closingTimeError: String? = null,
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
        pricePerHour: String,
        openingTime: String,
        closingTime: String,
    ) {
        // Validate input
        val validationErrors = validateInput(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            provinceOrCity = provinceOrCity,
            district = district,
            detailAddress = detailAddress,
            pricePerHour = pricePerHour,
            openingTime = openingTime,
            closingTime = closingTime
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
                detailAddress = detailAddress.trim(),
                pricePerHour = pricePerHour.trim().toDoubleOrNull(),
                openingTime = openingTime.trim().takeIf { it.isNotEmpty() },
                closingTime = closingTime.trim().takeIf { it.isNotEmpty() }
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
        pricePerHour: String,
        openingTime: String,
        closingTime: String,
    ): ValidationErrors {
        var nameError: String? = null
        var phoneNumberError: String? = null
        var emailError: String? = null
        var provinceError: String? = null
        var districtError: String? = null
        var detailAddressError: String? = null
        var priceError: String? = null
        var openingTimeError: String? = null
        var closingTimeError: String? = null

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

        // Validate price per hour (optional but must be valid if provided)
        if (pricePerHour.isNotBlank()) {
            val price = pricePerHour.toDoubleOrNull()
            if (price == null) {
                priceError = "Giá không hợp lệ"
            } else if (price <= 0) {
                priceError = "Giá phải lớn hơn 0"
            }
        }

        // Validate opening time (optional but must be valid format if provided)
        if (openingTime.isNotBlank()) {
            if (!openingTime.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$"))) {
                openingTimeError = "Giờ mở cửa không hợp lệ (HH:mm hoặc HH:mm:ss)"
            }
        }

        // Validate closing time (optional but must be valid format if provided)
        if (closingTime.isNotBlank()) {
            if (!closingTime.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$"))) {
                closingTimeError = "Giờ đóng cửa không hợp lệ (HH:mm hoặc HH:mm:ss)"
            }
        }

        return ValidationErrors(
            nameError = nameError,
            phoneNumberError = phoneNumberError,
            emailError = emailError,
            provinceError = provinceError,
            districtError = districtError,
            detailAddressError = detailAddressError,
            priceError = priceError,
            openingTimeError = openingTimeError,
            closingTimeError = closingTimeError
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
            detailAddressError != null ||
            priceError != null ||
            openingTimeError != null ||
            closingTimeError != null
}

