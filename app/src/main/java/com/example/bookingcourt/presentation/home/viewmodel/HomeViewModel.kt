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
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<Venue> = emptyList(),
)

sealed interface HomeIntent {
    object LoadHomeData : HomeIntent
    data class NavigateToVenue(val venueId: Long) : HomeIntent
    object NavigateToSearch : HomeIntent
    object NavigateToProfile : HomeIntent
    object NavigateToBookings : HomeIntent
    object Refresh : HomeIntent
    data class Search(val query: String) : HomeIntent
    object ClearSearch : HomeIntent
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
            is HomeIntent.Search -> searchVenues(intent.query)
            HomeIntent.ClearSearch -> clearSearch()
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
     * Search venues by query (name or address)
     */
    private fun searchVenues(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                searchQuery = query,
                isSearching = true
            )

            if (query.isEmpty()) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    searchResults = emptyList()
                )
                return@launch
            }

            // Tìm kiếm local trước từ dữ liệu đã có
            val allVenues = (state.value.featuredVenues +
                            state.value.recommendedVenues +
                            state.value.nearbyVenues).distinctBy { it.id }

            val searchTerm = query.trim().lowercase()

            val localResults = allVenues.filter { venue ->
                // Tìm kiếm không phân biệt hoa thường
                venue.name.lowercase().contains(searchTerm) ||
                venue.address.getFullAddress().lowercase().contains(searchTerm) ||
                venue.address.provinceOrCity.lowercase().contains(searchTerm) ||
                venue.address.district.lowercase().contains(searchTerm) ||
                venue.address.detailAddress.lowercase().contains(searchTerm) ||
                (venue.description?.lowercase()?.contains(searchTerm) ?: false)
            }

            _state.value = _state.value.copy(
                isSearching = false,
                searchResults = localResults
            )

            // Tìm kiếm từ API - gửi query vào tất cả các trường để tăng khả năng tìm thấy
            venueRepository.searchVenues(
                name = query,
                province = query,
                district = query,
                detail = query
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val venues = result.data ?: emptyList()

                        // Merge kết quả từ API với kết quả local, loại bỏ duplicate
                        val mergedResults = (localResults + venues).distinctBy { it.id }

                        _state.value = _state.value.copy(
                            searchResults = mergedResults,
                            isSearching = false
                        )
                    }
                    is Resource.Error -> {
                        // Giữ kết quả local nếu API fail
                        _state.value = _state.value.copy(
                            isSearching = false
                        )
                    }
                    is Resource.Loading -> {
                        // Đang tìm kiếm từ API
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

    private fun clearSearch() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                searchQuery = "",
                isSearching = false,
                searchResults = emptyList()
            )
        }
    }
}
