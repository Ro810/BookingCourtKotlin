package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CourtAvailabilityDto(
    @SerializedName("courtId")
    val courtId: Long,
    @SerializedName("available")
    val available: Boolean,
    @SerializedName("bookedSlots")
    val bookedSlots: List<BookedSlotDto> = emptyList()
)

data class BookedSlotDto(
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("bookingId")
    val bookingId: Long?
)
