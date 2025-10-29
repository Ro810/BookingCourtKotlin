package com.example.bookingcourt.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
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
import kotlinx.datetime.LocalTime
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val featuredCourts: List<Court> = emptyList(),
    val nearbyCourts: List<Court> = emptyList(),
    val recommendedCourts: List<Court> = emptyList(),
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
                            // Nếu lỗi khi lấy user, có thể token hết hạn
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
                    return@collect // Take only the first emission
                }

                // Cập nhật user vào state ngay lập tức
                _state.value = _state.value.copy(user = userData)

                if (userData == null) {
                    // Chưa có user - yêu cầu đăng nhập
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        featuredCourts = emptyList(),
                        recommendedCourts = emptyList()
                    )
                    return@launch
                }

                // Đã đăng nhập - lấy tất cả venues từ backend /api/venues
                venueRepository.getVenues().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val allVenues = result.data ?: emptyList()

                            // Convert Venues to Courts for UI compatibility
                            val courts = allVenues.mapIndexed { index, venue ->
                                venue.toCourt(index)
                            }

                            // Featured courts: Venues có nhiều sân nhất
                            val featuredCourts = courts
                                .sortedByDescending { it.description.contains("sân") }
                                .take(5)

                            // Recommended courts: Các venues còn lại
                            val recommendedCourts = courts
                                .drop(5)
                                .take(5)

                            _state.value = _state.value.copy(
                                isLoading = false,
                                featuredCourts = featuredCourts,
                                recommendedCourts = recommendedCourts,
                                nearbyCourts = emptyList(),
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
                        val courts = venues.mapIndexed { index, venue -> venue.toCourt(index) }

                        _state.value = _state.value.copy(
                            isLoading = false,
                            featuredCourts = courts,
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

    /**
     * Convert Venue to Court for UI compatibility
     *
     * VENUE (Địa điểm sân): Sân bóng ABC - có 5 sân
     *   → Hiển thị như 1 Court card trên UI
     *   → Khi click vào sẽ mở danh sách các Courts bên trong
     */
    private fun Venue.toCourt(index: Int): Court {
        return Court(
            id = id.toString(),
            name = name, // Tên venue: "Sân bóng ABC"
            description = "Có $numberOfCourt sân - $courtsCount sân đang hoạt động", // Mô tả số lượng sân
            address = address.getFullAddress(), // Địa chỉ đầy đủ
            latitude = 21.0 + (index * 0.01), // Mock coordinates - TODO: thêm vào backend
            longitude = 105.8 + (index * 0.01),
            images = emptyList(), // TODO: thêm images vào backend
            sportType = SportType.BADMINTON, // Default
            courtType = CourtType.INDOOR, // Default
            pricePerHour = 100000, // Default - sẽ lấy từ PriceRules sau
            openTime = LocalTime(6, 0),
            closeTime = LocalTime(22, 0),
            amenities = emptyList(),
            rules = null,
            ownerId = id.toString(),
            rating = 4.0f + (index % 5) * 0.2f, // Mock rating
            totalReviews = numberOfCourt * 10, // Mock: nhiều sân = nhiều reviews
            isActive = numberOfCourt > 0, // Active nếu có ít nhất 1 sân
            maxPlayers = 4,
        )
    }

    private fun navigateToSport(sportType: SportType) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("court_list?sportType=${sportType.name}"))
        }
    }

    private fun navigateToCourt(courtId: String) {
        viewModelScope.launch {
            // courtId ở đây là venueId
            // Navigate đến màn hình chi tiết venue, hiển thị danh sách courts bên trong
            _uiEvent.emit(UiEvent.NavigateTo("venue_detail/$courtId"))
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
