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
    val selectedImages: List<java.io.File> = emptyList(), // Danh sách ảnh được chọn
    val isUploadingImages: Boolean = false, // Trạng thái upload ảnh
)

data class ValidationErrors(
    val nameError: String? = null,
    val phoneNumberError: String? = null,
    val emailError: String? = null,
    val provinceError: String? = null,
    val districtError: String? = null,
    val detailAddressError: String? = null,
    val numberOfCourtsError: String? = null,
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
        numberOfCourts: String,
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
            numberOfCourts = numberOfCourts,
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
                numberOfCourt = numberOfCourts.trim().toIntOrNull(),
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
                        val createdVenue = result.data

                        // Kiểm tra nếu có ảnh cần upload
                        val hasImagesToUpload = createdVenue != null && _state.value.selectedImages.isNotEmpty()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            createdVenue = createdVenue,
                            error = null,
                            isUploadingImages = hasImagesToUpload // Set true nếu có ảnh cần upload
                        )

                        // Upload ảnh sau khi tạo venue thành công
                        if (hasImagesToUpload) {
                            uploadImages(createdVenue!!.id)
                        }
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

    /**
     * Thêm ảnh vào danh sách ảnh được chọn
     */
    fun addImage(imageFile: java.io.File) {
        val currentImages = _state.value.selectedImages.toMutableList()
        currentImages.add(imageFile)
        _state.value = _state.value.copy(selectedImages = currentImages)
    }

    /**
     * Xóa ảnh khỏi danh sách
     */
    fun removeImage(index: Int) {
        val currentImages = _state.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _state.value = _state.value.copy(selectedImages = currentImages)
        }
    }

    /**
     * Upload ảnh cho venue đã tạo
     */
    private fun uploadImages(venueId: Long) {
        val images = _state.value.selectedImages
        if (images.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingImages = true)

            venueRepository.uploadVenueImages(venueId, images).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Cập nhật venue với ảnh đã upload
                        _state.value = _state.value.copy(
                            isUploadingImages = false,
                            createdVenue = result.data
                        )
                    }
                    is Resource.Error -> {
                        // Upload ảnh thất bại nhưng venue đã được tạo
                        // Chỉ hiển thị warning, không block user
                        _state.value = _state.value.copy(
                            isUploadingImages = false,
                            error = "Tạo sân thành công nhưng upload ảnh thất bại: ${result.message}"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isUploadingImages = true)
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
        numberOfCourts: String,
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
        var numberOfCourtsError: String? = null
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

        // Validate number of courts
        if (numberOfCourts.isBlank()) {
            numberOfCourtsError = "Số lượng sân không được để trống"
        } else {
            val courts = numberOfCourts.toIntOrNull()
            if (courts == null) {
                numberOfCourtsError = "Số lượng sân không hợp lệ"
            } else if (courts <= 0) {
                numberOfCourtsError = "Số lượng sân phải lớn hơn 0"
            }
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
            numberOfCourtsError = numberOfCourtsError,
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
            numberOfCourtsError != null ||
            priceError != null ||
            openingTimeError != null ||
            closingTimeError != null
}

