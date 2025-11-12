package com.example.bookingcourt.presentation.review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Review
import com.example.bookingcourt.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _venueReviewsState = MutableStateFlow(VenueReviewsState())
    val venueReviewsState: StateFlow<VenueReviewsState> = _venueReviewsState.asStateFlow()

    private val _myReviewsState = MutableStateFlow(MyReviewsState())
    val myReviewsState: StateFlow<MyReviewsState> = _myReviewsState.asStateFlow()

    private val _createReviewState = MutableStateFlow(CreateReviewState())
    val createReviewState: StateFlow<CreateReviewState> = _createReviewState.asStateFlow()

    private val _bookingReviewState = MutableStateFlow(BookingReviewState())
    val bookingReviewState: StateFlow<BookingReviewState> = _bookingReviewState.asStateFlow()

    private val _updateReviewState = MutableStateFlow(UpdateReviewState())
    val updateReviewState: StateFlow<UpdateReviewState> = _updateReviewState.asStateFlow()

    /**
     * Load all reviews for a venue
     * Public API - không cần authentication
     */
    fun loadVenueReviews(venueId: Long) {
        viewModelScope.launch {
            reviewRepository.getVenueReviews(venueId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _venueReviewsState.value = _venueReviewsState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        val reviews = resource.data ?: emptyList()
                        val averageRating = if (reviews.isNotEmpty()) {
                            reviews.map { it.rating }.average().toFloat()
                        } else {
                            0f
                        }
                        _venueReviewsState.value = _venueReviewsState.value.copy(
                            isLoading = false,
                            reviews = reviews,
                            averageRating = averageRating,
                            totalReviews = reviews.size,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _venueReviewsState.value = _venueReviewsState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Load all reviews of current user
     * Yêu cầu authentication
     */
    fun loadMyReviews() {
        viewModelScope.launch {
            reviewRepository.getMyReviews().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = false,
                            reviews = resource.data ?: emptyList(),
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Check if booking already has a review
     */
    fun loadBookingReview(bookingId: Long) {
        viewModelScope.launch {
            reviewRepository.getBookingReview(bookingId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _bookingReviewState.value = _bookingReviewState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _bookingReviewState.value = _bookingReviewState.value.copy(
                            isLoading = false,
                            review = resource.data,
                            hasReview = true,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _bookingReviewState.value = _bookingReviewState.value.copy(
                            isLoading = false,
                            hasReview = false,
                            error = null // Không hiển thị lỗi nếu chưa có review
                        )
                    }
                }
            }
        }
    }

    /**
     * Create a new review for a booking
     * Chỉ có thể review booking có status = CONFIRMED hoặc COMPLETED
     */
    fun createReview(bookingId: Long, rating: Int, comment: String?) {
        viewModelScope.launch {
            reviewRepository.createReview(bookingId, rating, comment).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _createReviewState.value = _createReviewState.value.copy(
                            isLoading = true,
                            error = null,
                            success = false
                        )
                    }
                    is Resource.Success -> {
                        _createReviewState.value = _createReviewState.value.copy(
                            isLoading = false,
                            success = true,
                            review = resource.data,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _createReviewState.value = _createReviewState.value.copy(
                            isLoading = false,
                            success = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Delete a review
     * Chỉ người tạo review mới có thể xóa
     */
    fun deleteReview(reviewId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = false,
                            error = null
                        )
                        // Reload reviews after deletion
                        loadMyReviews()
                        onSuccess()
                    }
                    is Resource.Error -> {
                        _myReviewsState.value = _myReviewsState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Update a review
     * Chỉ người tạo review mới có thể cập nhật
     */
    fun updateReview(reviewId: Long, rating: Int, comment: String?) {
        viewModelScope.launch {
            reviewRepository.updateReview(reviewId, rating, comment).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _updateReviewState.value = _updateReviewState.value.copy(
                            isLoading = true,
                            error = null,
                            success = false
                        )
                    }
                    is Resource.Success -> {
                        _updateReviewState.value = _updateReviewState.value.copy(
                            isLoading = false,
                            success = true,
                            review = resource.data,
                            error = null
                        )
                        // Reload reviews after update
                        loadMyReviews()
                    }
                    is Resource.Error -> {
                        _updateReviewState.value = _updateReviewState.value.copy(
                            isLoading = false,
                            success = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Reset create review state
     */
    fun resetCreateReviewState() {
        _createReviewState.value = CreateReviewState()
    }

    /**
     * Reset update review state
     */
    fun resetUpdateReviewState() {
        _updateReviewState.value = UpdateReviewState()
    }

    /**
     * Reset booking review state
     */
    fun resetBookingReviewState() {
        _bookingReviewState.value = BookingReviewState()
    }
}

// State classes
data class VenueReviewsState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val error: String? = null
)

data class MyReviewsState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
)

data class CreateReviewState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val review: Review? = null,
    val error: String? = null
)

data class BookingReviewState(
    val isLoading: Boolean = false,
    val hasReview: Boolean = false,
    val review: Review? = null,
    val error: String? = null
)

data class UpdateReviewState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val review: Review? = null,
    val error: String? = null
)
