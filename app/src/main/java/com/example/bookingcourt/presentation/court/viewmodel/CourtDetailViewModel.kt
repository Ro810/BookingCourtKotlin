package com.example.bookingcourt.presentation.court.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.CourtDetail // <-- Lấy từ tệp bên trái
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.CourtRepository // <-- Lấy từ tệp bên trái
import com.example.bookingcourt.domain.repository.VenueRepository
import com.example.bookingcourt.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourtDetailState(
    val isLoading: Boolean = false,
    val venue: Venue? = null,
    val courts: List<CourtDetail> = emptyList(), // Lấy từ bên trái
    val error: String? = null,
    val todayRevenue: Long = 0, // Lấy từ bên phải
    val bookedSlots: List<com.example.bookingcourt.domain.model.BookedSlot> = emptyList(),
)

sealed interface CourtDetailIntent {
    data class LoadVenueDetail(val venueId: Long) : CourtDetailIntent // Giữ của bên trái (dùng Long)
    data class NavigateToBooking(val courtId: Long) : CourtDetailIntent // Giữ của bên trái
    object NavigateBack : CourtDetailIntent
    object Refresh : CourtDetailIntent
    data class CheckIn(val bookingId: String) : CourtDetailIntent // Thêm từ bên phải
}

@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val venueRepository: VenueRepository,
    private val courtRepository: CourtRepository, // Thêm dòng này từ bên trái
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val venueId: String = savedStateHandle.get<String>("venueId") ?: ""

    private val _state = MutableStateFlow(CourtDetailState())
    val state: StateFlow<CourtDetailState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        if (venueId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadVenueDetail(venueId.toLongOrNull() ?: 0))
        }
    }

    fun handleIntent(intent: CourtDetailIntent) {
        when (intent) {
            is CourtDetailIntent.LoadVenueDetail -> loadVenueDetail(intent.venueId)
            is CourtDetailIntent.NavigateToBooking -> navigateToBooking(intent.courtId)
            CourtDetailIntent.NavigateBack -> navigateBack()
            CourtDetailIntent.Refresh -> refresh()
            is CourtDetailIntent.CheckIn -> checkIn(intent.bookingId) // Thêm từ P2
        }
    }
    private fun loadVenueDetail(venueId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1. Tải Venue
            venueRepository.getVenueById(venueId).collect { venueResult ->
                when (venueResult) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(venue = venueResult.data)

                        // 2. Tải Courts (sân con)
                        courtRepository.getCourtsByVenueId(venueId).collect { courtsResult ->
                            when (courtsResult) {
                                is Resource.Success -> {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        courts = courtsResult.data ?: emptyList(),
                                        error = null
                                        // TODO: Thêm logic gọi repo lấy todayRevenue ở đây
                                        // todayRevenue = ...
                                    )
                                }
                                is Resource.Error -> {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        error = courtsResult.message
                                    )
                                }
                                is Resource.Loading -> {
                                    _state.value = _state.value.copy(isLoading = true)
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = venueResult.message
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }
    


    private fun navigateToBooking(courtId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("booking/$courtId"))
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateUp)
        }
    }
// Thêm hàm từ P2
    private fun checkIn(bookingId: String) {
        viewModelScope.launch {
            // TODO: Implement check-in logic
            _uiEvent.emit(UiEvent.ShowSnackbar("Check-in thành công"))
        }
    }
    private fun refresh() {
       if (venueId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadVenueDetail(venueId.toLongOrNull() ?: 0))
        }
    }

    /**
     * Lấy các time slots đã được đặt cho venue trong ngày cụ thể
     * @param venueId ID của venue
     * @param date Ngày cần kiểm tra (format: yyyy-MM-dd)
     */
    fun getBookedSlots(venueId: Long, date: String) {
        viewModelScope.launch {
            bookingRepository.getBookedSlots(venueId, date).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            bookedSlots = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        // Xử lý lỗi nếu cần
                        _state.value = _state.value.copy(
                            bookedSlots = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        // Không làm gì
                    }
                }
            }
        }
    }
}
