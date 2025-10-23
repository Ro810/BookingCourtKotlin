package com.example.bookingcourt.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.model.User
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
            // Dữ liệu mẫu cho sân nổi bật
            val sampleFeaturedCourts = listOf(
                Court(
                    id = "featured1",
                    name = "Star Club Badminton",
                    description = "Sân cầu lông chất lượng cao với thiết bị hiện đại",
                    address = "Số 181 P. Cầu Cốc, Tây Mỗ, Nam Từ Liêm, Hà Nội",
                    latitude = 21.0159,
                    longitude = 105.7447,
                    images = emptyList(),
                    sportType = SportType.BADMINTON,
                    courtType = CourtType.INDOOR,
                    pricePerHour = 150,
                    openTime = LocalTime(5, 0),
                    closeTime = LocalTime(23, 0),
                    amenities = emptyList(),
                    rules = "Không hút thuốc trong sân",
                    ownerId = "owner1",
                    rating = 4.5f,
                    totalReviews = 128,
                    isActive = true,
                    maxPlayers = 4,
                ),
                Court(
                    id = "featured2",
                    name = "MVP Fitness Badminton",
                    description = "Sân cầu lông hiện đại với không gian thoáng mát",
                    address = "Tầng 10, Toà F.Zone 4, Vinsmart Tây Mỗ",
                    latitude = 21.0200,
                    longitude = 105.7500,
                    images = emptyList(),
                    sportType = SportType.BADMINTON,
                    courtType = CourtType.INDOOR,
                    pricePerHour = 120,
                    openTime = LocalTime(5, 30),
                    closeTime = LocalTime(21, 30),
                    amenities = emptyList(),
                    rules = "Giữ gìn vệ sinh chung",
                    ownerId = "owner2",
                    rating = 4.2f,
                    totalReviews = 89,
                    isActive = true,
                    maxPlayers = 4,
                ),
                Court(
                    id = "featured3",
                    name = "Tennis Pro Center",
                    description = "Sân tennis chuyên nghiệp với mặt sân chuẩn quốc tế",
                    address = "456 Đường ABC, Quận Cầu Giấy, Hà Nội",
                    latitude = 21.0285,
                    longitude = 105.8542,
                    images = emptyList(),
                    sportType = SportType.TENNIS,
                    courtType = CourtType.OUTDOOR,
                    pricePerHour = 200,
                    openTime = LocalTime(6, 0),
                    closeTime = LocalTime(22, 0),
                    amenities = emptyList(),
                    rules = "Mang giày thể thao chuyên dụng",
                    ownerId = "owner3",
                    rating = 4.7f,
                    totalReviews = 156,
                    isActive = true,
                    maxPlayers = 4,
                )
            )

            val sampleRecommendedCourts = listOf(
                Court(
                    id = "rec1",
                    name = "Sân cầu lông Thăng Long",
                    description = "Sân cầu lông giá rẻ, phù hợp sinh viên",
                    address = "123 Đường Giải Phóng, Hoàng Mai, Hà Nội",
                    latitude = 20.9735,
                    longitude = 105.8426,
                    images = emptyList(),
                    sportType = SportType.BADMINTON,
                    courtType = CourtType.INDOOR,
                    pricePerHour = 80,
                    openTime = LocalTime(6, 0),
                    closeTime = LocalTime(22, 0),
                    amenities = emptyList(),
                    rules = null,
                    ownerId = "owner4",
                    rating = 4.0f,
                    totalReviews = 67,
                    isActive = true,
                    maxPlayers = 4,
                ),
                Court(
                    id = "rec2",
                    name = "Football Club 5vs5",
                    description = "Sân bóng đá mini chất lượng cao",
                    address = "789 Đường XYZ, Quận Đống Đa, Hà Nội",
                    latitude = 21.0167,
                    longitude = 105.8262,
                    images = emptyList(),
                    sportType = SportType.FOOTBALL,
                    courtType = CourtType.OUTDOOR,
                    pricePerHour = 300,
                    openTime = LocalTime(6, 0),
                    closeTime = LocalTime(23, 0),
                    amenities = emptyList(),
                    rules = "Không được đá bóng quá mạnh",
                    ownerId = "owner5",
                    rating = 4.3f,
                    totalReviews = 94,
                    isActive = true,
                    maxPlayers = 10,
                )
            )

            _state.value = _state.value.copy(
                isLoading = false,
                user = null, // Replace with actual user
                featuredCourts = sampleFeaturedCourts,
                nearbyCourts = emptyList(), // Replace with actual data
                recommendedCourts = sampleRecommendedCourts,
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
