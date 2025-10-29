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

                venueRepository.getVenues().collect { result ->
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
}
