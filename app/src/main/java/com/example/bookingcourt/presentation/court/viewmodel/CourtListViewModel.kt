package com.example.bookingcourt.presentation.court.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.UiEvent
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.SportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourtListState(
    val isLoading: Boolean = false,
    val courts: List<Court> = emptyList(),
    val filteredCourts: List<Court> = emptyList(),
    val searchQuery: String = "",
    val selectedSportType: SportType? = null,
    val error: String? = null,
)

sealed interface CourtListIntent {
    data class LoadCourts(val sportType: SportType? = null) : CourtListIntent
    data class SearchCourts(val query: String) : CourtListIntent
    data class FilterBySportType(val sportType: SportType?) : CourtListIntent
    data class NavigateToDetail(val courtId: String) : CourtListIntent
    object Refresh : CourtListIntent
}

@HiltViewModel
class CourtListViewModel @Inject constructor(
    // TODO: Inject repositories when ready
) : ViewModel() {

    private val _state = MutableStateFlow(CourtListState())
    val state: StateFlow<CourtListState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        handleIntent(CourtListIntent.LoadCourts())
    }

    fun handleIntent(intent: CourtListIntent) {
        when (intent) {
            is CourtListIntent.LoadCourts -> loadCourts(intent.sportType)
            is CourtListIntent.SearchCourts -> searchCourts(intent.query)
            is CourtListIntent.FilterBySportType -> filterBySportType(intent.sportType)
            is CourtListIntent.NavigateToDetail -> navigateToDetail(intent.courtId)
            CourtListIntent.Refresh -> refresh()
        }
    }

    private fun loadCourts(sportType: SportType?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                selectedSportType = sportType,
            )

            // TODO: Replace with actual repository call
            val mockCourts = getMockCourts()

            _state.value = _state.value.copy(
                isLoading = false,
                courts = mockCourts,
                filteredCourts = filterCourts(mockCourts, _state.value.searchQuery, sportType),
            )
        }
    }

    private fun searchCourts(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredCourts = filterCourts(
                _state.value.courts,
                query,
                _state.value.selectedSportType,
            ),
        )
    }

    private fun filterBySportType(sportType: SportType?) {
        _state.value = _state.value.copy(
            selectedSportType = sportType,
            filteredCourts = filterCourts(
                _state.value.courts,
                _state.value.searchQuery,
                sportType,
            ),
        )
    }

    private fun filterCourts(
        courts: List<Court>,
        query: String,
        sportType: SportType?,
    ): List<Court> {
        return courts.filter { court ->
            (
                query.isEmpty() || court.name.contains(query, ignoreCase = true) ||
                    court.address.contains(query, ignoreCase = true)
                ) &&
                (sportType == null || court.sportType == sportType)
        }
    }

    private fun navigateToDetail(courtId: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("court_detail/$courtId"))
        }
    }

    private fun refresh() {
        handleIntent(CourtListIntent.LoadCourts(_state.value.selectedSportType))
    }

    private fun getMockCourts(): List<Court> {
        // TODO: Remove when repository is ready
        return emptyList()
    }
}
