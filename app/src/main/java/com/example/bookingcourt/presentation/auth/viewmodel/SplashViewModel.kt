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
            // Kiểm tra cả isLoggedIn và accessToken
            val isLoggedIn = userPreferencesDataStore.isLoggedIn.first()
            val accessToken = userPreferencesDataStore.accessToken.first()

            delay(500)

            // Chỉ cho vào Home nếu CẢ isLoggedIn = true VÀ có token hợp lệ
            if (isLoggedIn && !accessToken.isNullOrEmpty()) {
                _navigationEvent.emit(NavigationEvent.NavigateToHome)
            } else {
                // Nếu không có token hoặc chưa đăng nhập → Clear data và yêu cầu login
                if (isLoggedIn && accessToken.isNullOrEmpty()) {
                    // Trường hợp token bị mất nhưng flag còn → Clear hết
                    userPreferencesDataStore.clearAuthData()
                }
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }
        }
    }

    sealed class NavigationEvent {
        data object NavigateToHome : NavigationEvent()
        data object NavigateToLogin : NavigationEvent()
    }
}
