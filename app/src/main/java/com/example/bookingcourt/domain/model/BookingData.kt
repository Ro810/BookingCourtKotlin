package com.example.bookingcourt.domain.model

data class BookingData(
    val courtId: String,
    val courtName: String,
    val courtAddress: String,
    val selectedDate: String,
    val selectedSlots: Set<CourtTimeSlot>,
    val playerName: String,
    val phoneNumber: String,
    val pricePerHour: Long,
    val totalPrice: Long
)

data class CourtTimeSlot(
    val courtNumber: Int,
    val timeSlot: String
)
