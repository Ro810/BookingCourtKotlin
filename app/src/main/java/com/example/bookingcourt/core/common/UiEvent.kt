package com.example.bookingcourt.core.common

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToast(val message: String) : UiEvent
    data object NavigateUp : UiEvent
    data class NavigateTo(val route: String) : UiEvent
}
