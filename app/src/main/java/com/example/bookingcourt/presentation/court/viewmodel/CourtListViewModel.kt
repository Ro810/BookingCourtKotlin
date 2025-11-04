package com.example.bookingcourt.presentation.court.viewmodel

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

data class CourtListState(
    val isLoading: Boolean = false,
    val venues: List<Venue> = emptyList(),
    val filteredVenues: List<Venue> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
)

sealed interface CourtListIntent {
    object LoadVenues : CourtListIntent
    data class SearchVenues(val query: String) : CourtListIntent
    data class NavigateToDetail(val venueId: Long) : CourtListIntent
    object Refresh : CourtListIntent
}

@HiltViewModel
class CourtListViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CourtListState())
    val state: StateFlow<CourtListState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        handleIntent(CourtListIntent.LoadVenues)
    }

    fun handleIntent(intent: CourtListIntent) {
        when (intent) {
            is CourtListIntent.LoadVenues -> loadVenues()
            is CourtListIntent.SearchVenues -> searchVenues(intent.query)
            is CourtListIntent.NavigateToDetail -> navigateToDetail(intent.venueId)
            CourtListIntent.Refresh -> refresh()
        }
    }

    private fun loadVenues() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            venueRepository.getVenues().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val venues = result.data ?: emptyList()
                        _state.value = _state.value.copy(
                            isLoading = false,
                            venues = venues,
                            filteredVenues = filterVenues(venues, _state.value.searchQuery),
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Không thể tải danh sách"
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun searchVenues(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredVenues = filterVenues(_state.value.venues, query),
        )
    }

    private fun filterVenues(venues: List<Venue>, query: String): List<Venue> {
        if (query.isBlank()) return venues

        return venues.filter { venue ->
            venue.name.contains(query, ignoreCase = true) ||
            venue.address.getFullAddress().contains(query, ignoreCase = true)
        }
    }

    private fun navigateToDetail(venueId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateTo("venue_detail/$venueId"))
        }
    }

    private fun refresh() {
        handleIntent(CourtListIntent.LoadVenues)
    }
}
