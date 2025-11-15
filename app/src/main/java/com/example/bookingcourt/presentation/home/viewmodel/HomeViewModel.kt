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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * HomeState - State cho HomeScreen
 * selectedVenue: Venue được fetch riêng khi navigate vào BookingScreen để đảm bảo data mới nhất
 */
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
    val selectedVenue: Venue? = null, // ✅ Venue được fetch riêng (cho BookingScreen)
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

    private var searchJob: Job? = null

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
     * Tìm kiếm theo tên sân hoặc địa chỉ với debounce
     */
    private fun searchVenues(query: String) {
        // Cancel previous search job
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                searchQuery = query,
                isSearching = true
            )

            // Nếu query rỗng, clear kết quả
            if (query.trim().isEmpty()) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    searchResults = emptyList(),
                    searchQuery = ""
                )
                return@launch
            }

            // Debounce: Đợi 300ms trước khi tìm kiếm
            delay(300)

            val searchTerm = query.trim().lowercase()

            // Bước 1: Tìm kiếm local từ dữ liệu đã có (nhanh, hiển thị ngay)
            val allVenues = (state.value.featuredVenues +
                            state.value.recommendedVenues +
                            state.value.nearbyVenues).distinctBy { it.id }

            val localResults = allVenues.filter { venue ->
                matchesSearchQuery(venue, searchTerm)
            }

            // Hiển thị kết quả local ngay lập tức
            _state.value = _state.value.copy(
                searchResults = localResults,
                isSearching = true // Vẫn đang tìm từ API
            )

            // Bước 2: Tìm kiếm từ API (đầy đủ hơn)
            try {
                venueRepository.searchVenues(
                    name = query,
                    province = query,
                    district = query,
                    detail = query
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val apiResults = result.data ?: emptyList()

                            // Filter API results với cùng logic
                            val filteredApiResults = apiResults.filter { venue ->
                                matchesSearchQuery(venue, searchTerm)
                            }

                            // Merge và loại bỏ duplicate, ưu tiên kết quả có độ tương đồng cao hơn
                            val mergedResults = (localResults + filteredApiResults)
                                .distinctBy { it.id }
                                .sortedByDescending { venue ->
                                    calculateRelevanceScore(venue, searchTerm)
                                }

                            _state.value = _state.value.copy(
                                searchResults = mergedResults,
                                isSearching = false
                            )
                        }
                        is Resource.Error -> {
                            // Giữ kết quả local nếu API fail
                            _state.value = _state.value.copy(
                                isSearching = false,
                                error = null // Không hiển thị lỗi khi search fail
                            )
                        }
                        is Resource.Loading -> {
                            // Đang tìm kiếm từ API
                        }
                    }
                }
            } catch (_: Exception) {
                // Giữ kết quả local nếu có exception
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = null
                )
            }
        }
    }

    /**
     * Kiểm tra venue có khớp với query tìm kiếm không
     */
    private fun matchesSearchQuery(venue: Venue, searchTerm: String): Boolean {
        return venue.name.lowercase().contains(searchTerm) ||
               venue.address.getFullAddress().lowercase().contains(searchTerm) ||
               venue.address.provinceOrCity.lowercase().contains(searchTerm) ||
               venue.address.district.lowercase().contains(searchTerm) ||
               venue.address.detailAddress.lowercase().contains(searchTerm) ||
               (venue.description?.lowercase()?.contains(searchTerm) ?: false)
    }

    /**
     * Tính điểm độ liên quan để sắp xếp kết quả tìm kiếm
     * Điểm cao hơn = liên quan hơn
     */
    private fun calculateRelevanceScore(venue: Venue, searchTerm: String): Int {
        var score = 0

        // Tên sân khớp chính xác = điểm cao nhất
        if (venue.name.lowercase() == searchTerm) {
            score += 100
        } else if (venue.name.lowercase().startsWith(searchTerm)) {
            score += 50
        } else if (venue.name.lowercase().contains(searchTerm)) {
            score += 25
        }

        // Địa chỉ chi tiết khớp
        if (venue.address.detailAddress.lowercase().contains(searchTerm)) {
            score += 15
        }

        // Quận/huyện khớp
        if (venue.address.district.lowercase().contains(searchTerm)) {
            score += 10
        }

        // Tỉnh/thành phố khớp
        if (venue.address.provinceOrCity.lowercase().contains(searchTerm)) {
            score += 5
        }

        // Description khớp
        if (venue.description?.lowercase()?.contains(searchTerm) == true) {
            score += 3
        }

        // Bonus điểm cho rating cao
        score += (venue.averageRating * 2).toInt()

        return score
    }

    private fun clearSearch() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            searchQuery = "",
            isSearching = false,
            searchResults = emptyList()
        )
    }

    private fun refresh() {
        loadHomeData()
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

    /**
     * Fetch venue by ID - dùng để lấy dữ liệu mới nhất khi mở BookingScreen
     * Quan trọng: Sau khi owner update venue, cần fetch lại để có dữ liệu mới
     */
    fun fetchVenueById(venueId: Long) {
        viewModelScope.launch {
            venueRepository.getVenueById(venueId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            selectedVenue = result.data
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {
                        // Loading
                    }
                }
            }
        }
    }
}
