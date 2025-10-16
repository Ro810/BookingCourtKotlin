package com.example.bookingcourt.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.User
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
    val featuredCourts: List<Court> = emptyList(),
    val nearbyCourts: List<Court> = emptyList(),
    val popularSports: List<SportType> = listOf(
        SportType.BADMINTON,
        SportType.TENNIS,
        SportType.FOOTBALL,
        SportType.BASKETBALL,
    ),
    val recentBookings: List<Any> = emptyList(), // TODO: Add Booking model
    val error: String? = null,
)

sealed interface HomeIntent {
    object LoadHomeData : HomeIntent
    data class NavigateToSport(val sportType: SportType) : HomeIntent
    data class NavigateToCourt(val courtId: String) : HomeIntent
    object NavigateToSearch : HomeIntent
    object NavigateToProfile : HomeIntent
    object NavigateToBookings : HomeIntent
    object Refresh : HomeIntent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject repositories when ready
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
            is HomeIntent.NavigateToSport -> navigateToSport(intent.sportType)
            is HomeIntent.NavigateToCourt -> navigateToCourt(intent.courtId)
            HomeIntent.NavigateToSearch -> navigateToSearch()
            HomeIntent.NavigateToProfile -> navigateToProfile()
            HomeIntent.NavigateToBookings -> navigateToBookings()
            HomeIntent.Refresh -> refresh()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // TODO: Replace with actual repository calls
            // val user = authRepository.getCurrentUser()
            // val featuredCourts = courtRepository.getFeaturedCourts()
            // val nearbyCourts = courtRepository.getNearbyCourts()
            // val recentBookings = bookingRepository.getRecentBookings()

            _state.value = _state.value.copy(
                isLoading = false,
                user = null, // Replace with actual user
                featuredCourts = emptyList(), // Replace with actual data
                nearbyCourts = emptyList(), // Replace with actual data
                recentBookings = emptyList(), // Replace with actual data
            )
        }
    }

    private fun navigateToSport(sportType: SportType) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("court_list?sportType=${sportType.name}"))
        }
    }

    private fun navigateToCourt(courtId: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("court_detail/$courtId"))
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
