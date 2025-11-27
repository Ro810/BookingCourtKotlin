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
    val selectedDateRevenue: Long = 0, // Doanh thu của ngày được chọn
    val courtsAvailability: List<com.example.bookingcourt.domain.model.CourtAvailability> = emptyList(), // Tình trạng sân theo API mới
    val pendingBookings: List<com.example.bookingcourt.domain.model.BookingDetail> = emptyList(), // Danh sách booking chờ xác nhận
    val confirmedBookings: List<com.example.bookingcourt.domain.model.BookingDetail> = emptyList(), // Danh sách booking đã xác nhận cho check-in schedule
)

sealed interface CourtDetailIntent {
    data class LoadVenueDetail(val venueId: Long) : CourtDetailIntent // Giữ của bên trái (dùng Long)
    data class NavigateToBooking(val courtId: Long) : CourtDetailIntent // Giữ của bên trái
    object NavigateBack : CourtDetailIntent
    object Refresh : CourtDetailIntent
    data class CheckIn(val bookingId: String) : CourtDetailIntent // Thêm từ bên phải
    data class UploadImage(val venueId: Long, val imageFile: java.io.File) : CourtDetailIntent
    data class ToggleCourtStatus(val courtId: Long) : CourtDetailIntent // Khóa/Mở khóa sân
}

@HiltViewModel
class CourtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val venueRepository: VenueRepository,
    private val courtRepository: CourtRepository, // Thêm dòng này từ bên trái
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val venueId: String = savedStateHandle.get<String>("courtId") ?: ""

    private val _state = MutableStateFlow(CourtDetailState())
    val state: StateFlow<CourtDetailState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        android.util.Log.d("CourtDetailVM", "🔧 Init - venueId from savedStateHandle: '$venueId'")
        if (venueId.isNotEmpty()) {
            android.util.Log.d("CourtDetailVM", "🔧 Loading venue detail for venueId: ${venueId.toLongOrNull()}")
            handleIntent(CourtDetailIntent.LoadVenueDetail(venueId.toLongOrNull() ?: 0))
        } else {
            android.util.Log.w("CourtDetailVM", "⚠️ VenueId is empty!")
        }
    }

    fun handleIntent(intent: CourtDetailIntent) {
        when (intent) {
            is CourtDetailIntent.LoadVenueDetail -> loadVenueDetail(intent.venueId)
            is CourtDetailIntent.NavigateToBooking -> navigateToBooking(intent.courtId)
            CourtDetailIntent.NavigateBack -> navigateBack()
            CourtDetailIntent.Refresh -> refresh()
            is CourtDetailIntent.CheckIn -> checkIn(intent.bookingId) // Thêm từ P2
            is CourtDetailIntent.UploadImage -> uploadVenueImage(intent.venueId, intent.imageFile)
            is CourtDetailIntent.ToggleCourtStatus -> toggleCourtStatus(intent.courtId)
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
                                        error = null,
                                        // TODO: Thêm logic gọi repo lấy todayRevenue ở đây
                                        // todayRevenue = ...
                                        // Khởi tạo selectedDateRevenue với todayRevenue
                                        selectedDateRevenue = _state.value.todayRevenue
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
     * Khóa/Mở khóa court
     * Toggle trạng thái hoạt động của court (isActive)
     * @param courtId ID của court cần toggle
     */
    private fun toggleCourtStatus(courtId: Long) {
        viewModelScope.launch {
            android.util.Log.d("CourtDetailVM", "🔄 Toggling court status for courtId: $courtId")

            courtRepository.toggleCourtStatus(courtId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Toggle success: ${result.data?.message}")

                        // Update courtsAvailability state với isActive mới
                        val updatedCourts = _state.value.courtsAvailability.map { court ->
                            if (court.courtId == courtId) {
                                court.copy(isActive = result.data?.isActive ?: court.isActive)
                            } else {
                                court
                            }
                        }
                        _state.value = _state.value.copy(courtsAvailability = updatedCourts)

                        // Hiển thị message
                        _uiEvent.emit(UiEvent.ShowSnackbar(result.data?.message ?: "Đã thay đổi trạng thái sân"))
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Toggle error: ${result.message}")
                        _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Lỗi thay đổi trạng thái sân"))
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Toggling court status...")
                    }
                }
            }
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

    /**
     * Cập nhật doanh thu cho ngày được chọn
     * Tạm thời: nếu ngày được chọn là hôm nay thì hiển thị todayRevenue,
     * nếu là ngày khác thì hiển thị 0 (cần API backend để lấy doanh thu theo ngày)
     * @param selectedDate Ngày được chọn (format: dd/MM/yyyy)
     */
    fun updateSelectedDateRevenue(selectedDate: String) {
        viewModelScope.launch {
            // So sánh ngày được chọn với ngày hôm nay
            val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

            val revenue = if (selectedDate == today) {
                _state.value.todayRevenue
            } else {
                // TODO: Gọi API để lấy doanh thu theo ngày khi backend có sẵn
                0L
            }

            _state.value = _state.value.copy(selectedDateRevenue = revenue)
        }
    }

    /**
     * Lấy thông tin tình trạng sân cho khoảng thời gian cụ thể
     * Sử dụng API mới: GET /venues/{venueId}/courts/availability
     * @param venueId ID của venue
     * @param date Ngày cần kiểm tra (format: yyyy-MM-dd)
     * @param startTime Giờ bắt đầu (format: HH:mm:ss)
     * @param endTime Giờ kết thúc (format: HH:mm:ss)
     */
    fun getCourtsAvailabilityForTimeRange(
        venueId: Long,
        date: String,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            // Format thời gian theo ISO 8601
            val startDateTime = "${date}T${startTime}"
            val endDateTime = "${date}T${endTime}"

            venueRepository.getCourtsAvailability(venueId, startDateTime, endDateTime).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            courtsAvailability = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            courtsAvailability = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        // Không làm gì
                    }
                }
            }
        }
    }

    /**
     * Lấy thông tin tình trạng sân cho cả ngày
     * @param venueId ID của venue
     * @param date Ngày cần kiểm tra (format: yyyy-MM-dd)
     */
    fun getCourtsAvailabilityForWholeDay(venueId: Long, date: String) {
        viewModelScope.launch {
            android.util.Log.d("CourtDetailVM", "📅 Getting courts availability for whole day - venueId: $venueId, date: $date")

            // Lấy availability cho cả ngày (00:00:00 đến 23:59:59)
            val startDateTime = "${date}T00:00:00"
            val endDateTime = "${date}T23:59:59"

            android.util.Log.d("CourtDetailVM", "🕐 Time range: $startDateTime to $endDateTime")

            venueRepository.getCourtsAvailability(venueId, startDateTime, endDateTime).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Courts availability loaded: ${result.data?.size} courts")
                        _state.value = _state.value.copy(
                            courtsAvailability = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Error loading courts availability: ${result.message}")
                        _state.value = _state.value.copy(
                            courtsAvailability = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Loading courts availability...")
                    }
                }
            }
        }
    }

    /**
     * Upload venue image
     * @param venueId ID của venue
     * @param imageFile File ảnh để upload
     */
    private fun uploadVenueImage(venueId: Long, imageFile: java.io.File) {
        viewModelScope.launch {
            android.util.Log.d("CourtDetailVM", "📤 Uploading image for venue $venueId")

            // Convert single file to list for new API
            venueRepository.uploadVenueImages(venueId, listOf(imageFile)).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Image uploaded successfully")
                        // Cập nhật state với venue mới (có ảnh)
                        _state.value = _state.value.copy(venue = result.data)
                        _uiEvent.emit(UiEvent.ShowSnackbar("Upload ảnh thành công"))
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Error uploading image: ${result.message}")
                        _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Lỗi upload ảnh"))
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Uploading image...")
                    }
                }
            }
        }
    }

    /**
     * Lấy danh sách booking chờ xác nhận (PAYMENT_UPLOADED) theo venue ID
     * Chỉ lấy pending bookings của venue hiện tại
     */
    fun getPendingBookings() {
        viewModelScope.launch {
            // Lấy venueId từ state hiện tại
            val currentVenueId = _state.value.venue?.id

            if (currentVenueId == null) {
                android.util.Log.w("CourtDetailVM", "⚠️ Cannot get pending bookings - venue not loaded yet")
                return@launch
            }

            android.util.Log.d("CourtDetailVM", "📋 Getting pending bookings for venue: $currentVenueId")
            bookingRepository.getVenuePendingBookings(currentVenueId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Pending bookings loaded: ${result.data?.size} bookings for venue $currentVenueId")
                        _state.value = _state.value.copy(
                            pendingBookings = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Error loading pending bookings: ${result.message}")
                        _state.value = _state.value.copy(
                            pendingBookings = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Loading pending bookings...")
                    }
                }
            }
        }
    }

    /**
     * Lấy danh sách booking đã xác nhận theo venue ID
     * Chỉ lấy confirmed bookings của venue hiện tại
     */
    fun getConfirmedBookings() {
        viewModelScope.launch {
            // Lấy venueId từ state hiện tại
            val currentVenueId = _state.value.venue?.id

            if (currentVenueId == null) {
                android.util.Log.w("CourtDetailVM", "⚠️ Cannot get confirmed bookings - venue not loaded yet")
                return@launch
            }

            android.util.Log.d("CourtDetailVM", "📋 Getting confirmed bookings for venue: $currentVenueId")
            bookingRepository.getVenueConfirmedBookings(currentVenueId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Confirmed bookings loaded: ${result.data?.size} bookings for venue $currentVenueId")
                        _state.value = _state.value.copy(
                            confirmedBookings = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Error loading confirmed bookings: ${result.message}")
                        _state.value = _state.value.copy(
                            confirmedBookings = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Loading confirmed bookings...")
                    }
                }
            }
        }
    }

    /**
     * Lấy danh sách bookings đã xác nhận sắp tới cho phần "Lịch check-in sắp tới"
     * Sử dụng endpoint mới: GET /bookings/venue/{venueId}/upcoming
     */
    fun getUpcomingBookings() {
        viewModelScope.launch {
            val currentVenueId = _state.value.venue?.id

            if (currentVenueId == null) {
                android.util.Log.w("CourtDetailVM", "⚠️ Cannot get upcoming bookings - venue not loaded yet")
                android.util.Log.w("CourtDetailVM", "   Current state.venue: ${_state.value.venue}")
                return@launch
            }

            android.util.Log.d("CourtDetailVM", "📋 Getting upcoming bookings for venue: $currentVenueId")
            android.util.Log.d("CourtDetailVM", "   Current confirmedBookings count: ${_state.value.confirmedBookings.size}")

            bookingRepository.getVenueUpcomingBookings(currentVenueId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        android.util.Log.d("CourtDetailVM", "✅ Upcoming bookings loaded: ${result.data?.size} bookings for venue $currentVenueId")
                        result.data?.forEachIndexed { index, booking ->
                            android.util.Log.d("CourtDetailVM", "  [$index] Booking ${booking.id}:")
                            android.util.Log.d("CourtDetailVM", "       User: ${booking.user.fullname}")
                            android.util.Log.d("CourtDetailVM", "       Status: ${booking.status}")
                            android.util.Log.d("CourtDetailVM", "       Venue: ${booking.venue.id} (${booking.venue.name})")
                            android.util.Log.d("CourtDetailVM", "       Time: ${booking.startTime} to ${booking.endTime}")
                            android.util.Log.d("CourtDetailVM", "       Courts: ${booking.getCourtsDisplayName()}")
                        }

                        _state.value = _state.value.copy(
                            confirmedBookings = result.data ?: emptyList()
                        )

                        android.util.Log.d("CourtDetailVM", "   State updated - confirmedBookings: ${_state.value.confirmedBookings.size}")
                    }
                    is Resource.Error -> {
                        android.util.Log.e("CourtDetailVM", "❌ Error loading upcoming bookings: ${result.message}")
                        android.util.Log.e("CourtDetailVM", "   Setting confirmedBookings to empty list")
                        _state.value = _state.value.copy(
                            confirmedBookings = emptyList()
                        )
                    }
                    is Resource.Loading -> {
                        android.util.Log.d("CourtDetailVM", "⏳ Loading upcoming bookings...")
                    }
                }
            }
        }
    }
}
