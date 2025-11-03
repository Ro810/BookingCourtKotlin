package com.example.bookingcourt.presentation.owner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.AuthRepository
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
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerHomeState())
    val state: StateFlow<OwnerHomeState> = _state.asStateFlow()

    init {
        loadUserInfo()
        loadVenues()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val userResult = authRepository.getCurrentUser().first()

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

    fun refresh() {
        loadUserInfo()
        loadVenues()
    }

    fun updateVenue(
        venueId: Long,
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String
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
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isUpdatingVenue = false,
                                updateSuccess = true,
                                error = null
                            )
                            // Refresh the venues list
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
}
