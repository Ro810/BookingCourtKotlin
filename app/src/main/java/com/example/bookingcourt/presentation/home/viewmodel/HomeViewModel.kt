package com.example.bookingcourt.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.AuthRepository
import com.example.bookingcourt.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val featuredVenues: List<Venue> = emptyList(),
    val nearbyVenues: List<Venue> = emptyList(),
    val recommendedVenues: List<Venue> = emptyList(),
    val recentBookings: List<Any> = emptyList(), // TODO: Add Booking model
    val error: String? = null,
)

sealed interface HomeIntent {
    object LoadHomeData : HomeIntent
    data class NavigateToVenue(val venueId: Long) : HomeIntent
    object NavigateToSearch : HomeIntent
    object NavigateToProfile : HomeIntent
    object NavigateToBookings : HomeIntent
    object Refresh : HomeIntent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        handleIntent(HomeIntent.LoadHomeData)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadHomeData -> loadHomeData()
            is HomeIntent.NavigateToVenue -> navigateToVenue(intent.venueId)
            HomeIntent.NavigateToSearch -> navigateToSearch()
            HomeIntent.NavigateToProfile -> navigateToProfile()
            HomeIntent.NavigateToBookings -> navigateToBookings()
            HomeIntent.Refresh -> refresh()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Lấy thông tin user hiện tại
                var userData: User? = null
                authRepository.getCurrentUser().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            userData = resource.data
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = resource.message,
                                user = null
                            )
                            return@collect
                        }
                        is Resource.Loading -> {
                            // Đang tải
                        }
                    }
                    return@collect
                }

                _state.value = _state.value.copy(user = userData)

                if (userData == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        featuredVenues = emptyList(),
                        recommendedVenues = emptyList()
                    )
                    return@launch
                }

                // Lấy tất cả venues từ backend
                venueRepository.getVenues().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val allVenues = result.data ?: emptyList()

                            // Featured venues: Venues có rating cao
                            val featuredVenues = allVenues
                                .sortedByDescending { it.averageRating }
                                .take(5)

                            // Recommended venues: Các venues có nhiều sân
                            val recommendedVenues = allVenues
                                .sortedByDescending { it.courtsCount }
                                .take(5)

                            _state.value = _state.value.copy(
                                isLoading = false,
                                featuredVenues = featuredVenues,
                                recommendedVenues = recommendedVenues,
                                nearbyVenues = emptyList(),
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Không thể tải danh sách sân"
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

    /**
     * Search venues by name or location
     */
    fun searchVenues(
        name: String? = null,
        province: String? = null,
        district: String? = null,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            venueRepository.searchVenues(name, province, district).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val venues = result.data ?: emptyList()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            featuredVenues = venues,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Không tìm thấy kết quả"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun navigateToVenue(venueId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("venue_detail/$venueId"))
        }
    }

    private fun navigateToSearch() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("search"))
        }
    }

    private fun navigateToProfile() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("profile"))
        }
    }

    private fun navigateToBookings() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("bookings"))
        }
    }

    private fun refresh() {
        handleIntent(HomeIntent.LoadHomeData)
    }
}
