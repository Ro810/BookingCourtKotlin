package com.example.bookingcourt.presentation.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.*
import com.example.bookingcourt.domain.usecase.analytics.GetAnalyticsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình Analytics/Thống kê của chủ sân
 * Quản lý các metrics:
 * - Doanh thu theo thời gian
 * - Số booking theo trạng thái
 * - Hiệu suất venue
 * - Giờ đặt nhiều nhất
 * - Top khách hàng
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsDataUseCase: GetAnalyticsDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        loadAnalytics(AnalyticsPeriod.MONTH) // Default: tháng này
    }

    fun loadAnalytics(period: AnalyticsPeriod = _state.value.selectedPeriod) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedPeriod = period)

            getAnalyticsDataUseCase(period).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            analyticsData = resource.data,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun changePeriod(period: AnalyticsPeriod) {
        loadAnalytics(period)
    }

    fun refresh() {
        loadAnalytics()
    }
}

data class AnalyticsState(
    val isLoading: Boolean = false,
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val analyticsData: AnalyticsData? = null,
    val error: String? = null
)
