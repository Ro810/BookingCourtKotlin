package com.example.bookingcourt.presentation.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.example.bookingcourt.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val currentUserRole: UserRole = UserRole.USER,
    val error: String? = null,
    val isSwitchingRole: Boolean = false,
    // Thêm field để track viewing mode (chế độ xem hiện tại)
    val viewingMode: UserRole = UserRole.USER, // Chế độ đang xem (USER hoặc OWNER)
    // Thêm số liệu thống kê
    val bookingsCount: Int = 0,
    val reviewsCount: Int = 0,
)

sealed class ProfileEvent {
    data object NavigateToLogin : ProfileEvent()
    data object NavigateToHomeScreen : ProfileEvent()
    data object NavigateToOwnerHomeScreen : ProfileEvent()
    data class ShowMessage(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val bookingRepository: com.example.bookingcourt.domain.repository.BookingRepository,
    private val reviewRepository: com.example.bookingcourt.domain.repository.ReviewRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<ProfileEvent>()
    val event = _event.asSharedFlow()

    init {
        loadUserInfo()
        loadStats() // Thêm load stats
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                authRepository.getCurrentUser().collect { userResult ->
                    when (userResult) {
                        is Resource.Success -> {
                            val user = userResult.data
                            // Lưu viewingMode hiện tại trước khi update
                            val currentViewingMode = _state.value.viewingMode
                            val hasLoadedUserBefore = _state.value.currentUser != null

                            _state.value = _state.value.copy(
                                isLoading = false,
                                currentUser = user,
                                currentUserRole = user?.role ?: UserRole.USER,
                                // QUAN TRỌNG: Chỉ set viewingMode từ backend lần đầu tiên
                                // Sau đó luôn giữ nguyên viewingMode mà user đã chọn
                                viewingMode = if (hasLoadedUserBefore) {
                                    currentViewingMode // Đã load rồi -> giữ nguyên viewingMode hiện tại
                                } else {
                                    user?.role ?: UserRole.USER // Lần đầu load -> dùng role từ backend
                                },
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                currentUser = null,
                                currentUserRole = UserRole.USER,
                                // Không reset viewingMode khi lỗi
                                error = userResult.message
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
                    currentUser = null,
                    currentUserRole = UserRole.USER,
                    // Không reset viewingMode khi exception
                    error = e.message ?: "Đã xảy ra lỗi"
                )
            }
        }
    }

    private fun loadStats() {
        Log.d("ProfileViewModel", "========== LOAD STATS ==========")

        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading bookings count...")
                // ✅ SỬA: Dùng getMyBookings() thay vì getUserBookings()
                bookingRepository.getMyBookings().collect { bookingResult ->
                    when (bookingResult) {
                        is Resource.Success -> {
                            val bookingsCount = bookingResult.data?.size ?: 0
                            Log.d("ProfileViewModel", "✅ Bookings count: $bookingsCount")
                            _state.value = _state.value.copy(bookingsCount = bookingsCount)
                        }
                        is Resource.Error -> {
                            Log.e("ProfileViewModel", "❌ Error loading bookings: ${bookingResult.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("ProfileViewModel", "⏳ Loading bookings...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Exception loading bookings: ${e.message}", e)
            }
        }

        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading reviews count...")
                reviewRepository.getMyReviews().collect { reviewResult ->
                    when (reviewResult) {
                        is Resource.Success -> {
                            val reviewsCount = reviewResult.data?.size ?: 0
                            Log.d("ProfileViewModel", "✅ Reviews count: $reviewsCount")
                            _state.value = _state.value.copy(reviewsCount = reviewsCount)
                        }
                        is Resource.Error -> {
                            Log.e("ProfileViewModel", "❌ Error loading reviews: ${reviewResult.message}")
                        }
                        is Resource.Loading -> {
                            Log.d("ProfileViewModel", "⏳ Loading reviews...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Exception loading reviews: ${e.message}", e)
            }
        }

        Log.d("ProfileViewModel", "======================================")
    }

    fun switchToUserMode() {
        viewModelScope.launch {
            // Chỉ cần đổi viewingMode thành USER
            _state.value = _state.value.copy(viewingMode = UserRole.USER)
            // Navigate sang HomeScreen (chế độ khách đặt sân)
            _event.emit(ProfileEvent.NavigateToHomeScreen)
        }
    }

    fun switchToOwnerMode() {
        viewModelScope.launch {
            // Chỉ cần đổi viewingMode thành OWNER
            _state.value = _state.value.copy(viewingMode = UserRole.OWNER)
            // Navigate sang OwnerHomeScreen (chế độ quản lý sân)
            _event.emit(ProfileEvent.NavigateToOwnerHomeScreen)
        }
    }

    fun refresh() {
        loadUserInfo()
        loadStats() // Thêm reload stats
    }
}
