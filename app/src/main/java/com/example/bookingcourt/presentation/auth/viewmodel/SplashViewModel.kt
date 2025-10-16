package com.example.bookingcourt.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun checkAuthStatus() {
        viewModelScope.launch {
            val isLoggedIn = userPreferencesDataStore.isLoggedIn.first()
            delay(500)

            if (isLoggedIn) {
                _navigationEvent.emit(NavigationEvent.NavigateToHome)
            } else {
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }
        }
    }

    sealed class NavigationEvent {
        data object NavigateToHome : NavigationEvent()
        data object NavigateToLogin : NavigationEvent()
    }
}
