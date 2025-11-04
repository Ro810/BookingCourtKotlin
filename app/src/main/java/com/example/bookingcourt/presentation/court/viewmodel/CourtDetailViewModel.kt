package com.example.bookingcourt.presentation.court.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.VenueRepository
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
    val error: String? = null,
    val todayRevenue: Long = 0,
)

sealed interface CourtDetailIntent {
    data class LoadVenue(val venueId: String) : CourtDetailIntent
    object NavigateToBooking : CourtDetailIntent
    object NavigateBack : CourtDetailIntent
    data class CheckIn(val bookingId: String) : CourtDetailIntent
    object Refresh : CourtDetailIntent
}

@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val venueRepository: VenueRepository,
) : ViewModel() {

    private val venueId: String = savedStateHandle.get<String>("courtId") ?: ""

    private val _state = MutableStateFlow(CourtDetailState())
    val state: StateFlow<CourtDetailState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        if (venueId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadVenue(venueId))
        }
    }

    fun handleIntent(intent: CourtDetailIntent) {
        when (intent) {
            is CourtDetailIntent.LoadVenue -> loadVenue(intent.venueId)
            CourtDetailIntent.NavigateToBooking -> navigateToBooking()
            CourtDetailIntent.NavigateBack -> navigateBack()
            is CourtDetailIntent.CheckIn -> checkIn(intent.bookingId)
            CourtDetailIntent.Refresh -> refresh()
        }
    }

    private fun loadVenue(venueId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                venueRepository.getVenueById(venueId.toLongOrNull() ?: 0).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                venue = result.data,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Đã xảy ra lỗi"
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

    private fun navigateToBooking() {
        viewModelScope.launch {
            _state.value.venue?.let { venue ->
                _uiEvent.emit(UiEvent.NavigateTo("booking/${venue.id}"))
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateUp)
        }
    }

    private fun checkIn(bookingId: String) {
        viewModelScope.launch {
            // TODO: Implement check-in logic
            _uiEvent.emit(UiEvent.ShowSnackbar("Check-in thành công"))
        }
    }

    private fun refresh() {
        if (venueId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadVenue(venueId))
        }
    }
}
