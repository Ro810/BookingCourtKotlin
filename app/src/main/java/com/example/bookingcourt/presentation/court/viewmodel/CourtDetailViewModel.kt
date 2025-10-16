package com.example.bookingcourt.presentation.court.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Court
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
    val court: Court? = null,
    val isBooked: Boolean = false,
    val error: String? = null,
    val todayRevenue: Long = 0,
    val availableSlots: List<String> = emptyList(),
)

sealed interface CourtDetailIntent {
    data class LoadCourt(val courtId: String) : CourtDetailIntent
    object NavigateToBooking : CourtDetailIntent
    object NavigateBack : CourtDetailIntent
    data class CheckIn(val bookingId: String) : CourtDetailIntent
    object Refresh : CourtDetailIntent
}

@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    // TODO: Inject repositories when ready
) : ViewModel() {

    private val courtId: String = savedStateHandle.get<String>("courtId") ?: ""

    private val _state = MutableStateFlow(CourtDetailState())
    val state: StateFlow<CourtDetailState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        if (courtId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadCourt(courtId))
        }
    }

    fun handleIntent(intent: CourtDetailIntent) {
        when (intent) {
            is CourtDetailIntent.LoadCourt -> loadCourt(intent.courtId)
            CourtDetailIntent.NavigateToBooking -> navigateToBooking()
            CourtDetailIntent.NavigateBack -> navigateBack()
            is CourtDetailIntent.CheckIn -> checkIn(intent.bookingId)
            CourtDetailIntent.Refresh -> refresh()
        }
    }

    private fun loadCourt(courtId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // TODO: Replace with actual repository call
            // val result = courtRepository.getCourtById(courtId)

            _state.value = _state.value.copy(
                isLoading = false,
                court = null, // Replace with actual data
                todayRevenue = 2500000, // Mock data
                availableSlots = generateAvailableSlots(),
            )
        }
    }

    private fun navigateToBooking() {
        viewModelScope.launch {
            _state.value.court?.let { court ->
                _uiEvent.emit(UiEvent.NavigateTo("booking/${court.id}"))
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
        if (courtId.isNotEmpty()) {
            handleIntent(CourtDetailIntent.LoadCourt(courtId))
        }
    }

    private fun generateAvailableSlots(): List<String> {
        // Mock available time slots
        return listOf("14:00", "15:00", "16:00", "17:00", "18:00", "19:00")
    }
}
