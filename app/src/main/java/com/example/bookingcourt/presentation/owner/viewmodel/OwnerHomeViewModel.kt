package com.example.bookingcourt.presentation.owner.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.AuthRepository
import com.example.bookingcourt.domain.repository.BookingRepository
import com.example.bookingcourt.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerHomeState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val venues: List<Venue> = emptyList(),
    val isLoadingVenues: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdatingVenue: Boolean = false,
    val updateSuccess: Boolean = false,
    val isDeletingVenue: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val venueRepository: VenueRepository,
    private val bookingRepository: BookingRepository, // ✅ Inject BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerHomeState())
    val state: StateFlow<OwnerHomeState> = _state.asStateFlow()

    // ✅ State cho số lượng pending bookings
    private val _pendingBookingsCount = MutableStateFlow(0)
    val pendingBookingsCount: StateFlow<Int> = _pendingBookingsCount.asStateFlow()

    init {
        loadUserInfo()
        loadVenues()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                authRepository.getCurrentUser().collect { userResult ->
                    when (userResult) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                currentUser = userResult.data,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = userResult.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đã xảy ra lỗi"
                )
            }
        }
    }

    private fun loadVenues() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingVenues = true)

                venueRepository.getMyVenues().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoadingVenues = false,
                                venues = result.data ?: emptyList(),
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoadingVenues = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoadingVenues = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingVenues = false,
                    error = e.message ?: "Đã xảy ra lỗi khi tải danh sách sân"
                )
            }
        }
    }

    // ✅ Load số lượng pending bookings
    fun loadPendingBookingsCount() {
        viewModelScope.launch {
            try {
                Log.d("OwnerHomeVM", "📊 Loading pending bookings count...")
                bookingRepository.getPendingBookings().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val count = result.data?.size ?: 0
                            _pendingBookingsCount.value = count
                            Log.d("OwnerHomeVM", "✅ Pending bookings count: $count")
                        }
                        is Resource.Error -> {
                            Log.e("OwnerHomeVM", "❌ Error loading pending bookings count: ${result.message}")
                            _pendingBookingsCount.value = 0
                        }
                        is Resource.Loading -> {
                            Log.d("OwnerHomeVM", "⏳ Loading pending bookings count...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OwnerHomeVM", "❌ Exception loading pending bookings count", e)
                _pendingBookingsCount.value = 0
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadUserInfo()
            loadVenues()
            loadPendingBookingsCount() // ✅ Refresh count khi refresh
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun resetUpdateSuccess() {
        _state.value = _state.value.copy(updateSuccess = false)
    }

    fun updateVenue(
        venueId: Long,
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        numberOfCourt: Int? = null,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double? = null,
        openingTime: String? = null,
        closingTime: String? = null,
        images: List<String>? = null
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isUpdatingVenue = true, updateSuccess = false)

                venueRepository.updateVenue(
                    venueId = venueId,
                    name = name,
                    description = description,
                    phoneNumber = phoneNumber,
                    email = email,
                    numberOfCourt = numberOfCourt,
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress,
                    pricePerHour = pricePerHour,
                    openingTime = openingTime,
                    closingTime = closingTime,
                    images = images
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // Update the venue in the current list immediately
                            val updatedVenue = result.data
                            if (updatedVenue != null) {
                                val updatedVenues = _state.value.venues.map { venue ->
                                    if (venue.id == updatedVenue.id) updatedVenue else venue
                                }
                                _state.value = _state.value.copy(
                                    isUpdatingVenue = false,
                                    updateSuccess = true,
                                    error = null,
                                    venues = updatedVenues
                                )
                            } else {
                                _state.value = _state.value.copy(
                                    isUpdatingVenue = false,
                                    updateSuccess = true,
                                    error = null
                                )
                            }
                            // Also refresh from API to ensure data consistency
                            loadVenues()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isUpdatingVenue = false,
                                updateSuccess = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isUpdatingVenue = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUpdatingVenue = false,
                    updateSuccess = false,
                    error = e.message ?: "Đã xảy ra lỗi khi cập nhật sân"
                )
            }
        }
    }

    fun deleteVenue(venueId: Long) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isDeletingVenue = true, deleteSuccess = false)

                venueRepository.deleteVenue(venueId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isDeletingVenue = false,
                                deleteSuccess = true,
                                error = null
                            )
                            // Refresh list after deletion
                            loadVenues()
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isDeletingVenue = false,
                                deleteSuccess = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isDeletingVenue = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDeletingVenue = false,
                    deleteSuccess = false,
                    error = e.message ?: "Đã xảy ra lỗi khi xóa sân"
                )
            }
        }
    }

    fun clearUpdateSuccess() {
        _state.value = _state.value.copy(updateSuccess = false)
    }

    fun clearDeleteSuccess() {
        _state.value = _state.value.copy(deleteSuccess = false)
    }

    /**
     * Upload images for a venue
     */
    fun uploadVenueImages(venueId: Long, imageFiles: List<java.io.File>) {
        if (imageFiles.isEmpty()) return

        viewModelScope.launch {
            try {
                Log.d("OwnerHomeVM", "Uploading ${imageFiles.size} images for venue $venueId")

                venueRepository.uploadVenueImages(venueId, imageFiles).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("OwnerHomeVM", "✓ Images uploaded successfully")
                            // Update the venue in the list with new images
                            val updatedVenue = result.data
                            if (updatedVenue != null) {
                                val updatedVenues = _state.value.venues.map { venue ->
                                    if (venue.id == updatedVenue.id) updatedVenue else venue
                                }
                                _state.value = _state.value.copy(venues = updatedVenues)
                            }
                            // Refresh to ensure consistency
                            loadVenues()
                        }
                        is Resource.Error -> {
                            Log.e("OwnerHomeVM", "✗ Error uploading images: ${result.message}")
                            _state.value = _state.value.copy(
                                error = "Lỗi upload ảnh: ${result.message}"
                            )
                        }
                        is Resource.Loading -> {
                            Log.d("OwnerHomeVM", "⏳ Uploading images...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OwnerHomeVM", "Exception uploading images", e)
                _state.value = _state.value.copy(
                    error = e.message ?: "Đã xảy ra lỗi khi upload ảnh"
                )
            }
        }
    }

    /**
     * Delete a venue image
     */
    fun deleteVenueImage(venueId: Long, imageUrl: String) {
        viewModelScope.launch {
            try {
                Log.d("OwnerHomeVM", "Deleting image for venue $venueId: $imageUrl")

                venueRepository.deleteVenueImage(venueId, imageUrl).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Log.d("OwnerHomeVM", "✓ Image deleted successfully")
                            // Update the venue in the list with updated images
                            val updatedVenue = result.data
                            if (updatedVenue != null) {
                                val updatedVenues = _state.value.venues.map { venue ->
                                    if (venue.id == updatedVenue.id) updatedVenue else venue
                                }
                                _state.value = _state.value.copy(venues = updatedVenues)
                            }
                            // Refresh to ensure consistency
                            loadVenues()
                        }
                        is Resource.Error -> {
                            Log.e("OwnerHomeVM", "✗ Error deleting image: ${result.message}")
                            _state.value = _state.value.copy(
                                error = "Lỗi xóa ảnh: ${result.message}"
                            )
                        }
                        is Resource.Loading -> {
                            Log.d("OwnerHomeVM", "⏳ Deleting image...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OwnerHomeVM", "Exception deleting image", e)
                _state.value = _state.value.copy(
                    error = e.message ?: "Đã xảy ra lỗi khi xóa ảnh"
                )
            }
        }
    }
}
