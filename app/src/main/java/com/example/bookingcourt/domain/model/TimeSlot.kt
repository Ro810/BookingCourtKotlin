package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDateTime

data class TimeSlot(
    val id: String,
    val courtId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAvailable: Boolean,
    val price: Long,
    val discount: Float? = null,
    val bookingId: String? = null,
)

data class TimeSlotFilter(
    val courtId: String,
    val date: LocalDateTime,
//    val sportType: SportType? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
)
