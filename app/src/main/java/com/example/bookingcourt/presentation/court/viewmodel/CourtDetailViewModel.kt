package com.example.bookingcourt.presentation.court.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.CourtDetail
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.CourtRepository
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
    val courts: List<CourtDetail> = emptyList(),
    val error: String? = null,
)

sealed interface CourtDetailIntent {
    data class LoadVenueDetail(val venueId: Long) : CourtDetailIntent
    data class NavigateToBooking(val courtId: Long) : CourtDetailIntent
    object NavigateBack : CourtDetailIntent
    object Refresh : CourtDetailIntent
}

@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val venueRepository: VenueRepository,
    private val courtRepository: CourtRepository,
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
        }
    }

    private fun loadVenueDetail(venueId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Load venue details
            venueRepository.getVenueById(venueId).collect { venueResult ->
                when (venueResult) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(venue = venueResult.data)

                        // Load courts for this venue
                        courtRepository.getCourtsByVenueId(venueId).collect { courtsResult ->
                            when (courtsResult) {
                                is Resource.Success -> {
                                    _state.value = _state.value.copy(
                                        isLoading = false,
                                        courts = courtsResult.data ?: emptyList(),
                                        error = null
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

    private fun refresh() {
        if (venueId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadVenueDetail(venueId.toLongOrNull() ?: 0))
        }
    }
}
