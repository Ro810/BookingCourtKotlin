package com.example.bookingcourt.domain.model

/**
 * Domain model cho các time slots đã được đặt
 */
data class BookedSlot(
    val courtId: Long,
    val courtNumber: Int,
    val startTime: String, // Format: "2025-11-05T10:00:00"
    val endTime: String, // Format: "2025-11-05T11:00:00"
    val status: BookingStatus,
    val bookingId: String
) {
    /**
     * Kiểm tra xem một time slot cụ thể có bị đặt không
     * @param courtNum Số thứ tự sân
     * @param timeSlot Khung giờ (format: "HH:mm")
     * @return true nếu time slot này nằm trong khoảng đã đặt
     */
    fun containsSlot(courtNum: Int, timeSlot: String): Boolean {
        if (courtNumber != courtNum) return false

        // Parse start and end time
        val startHourMin = startTime.substring(11, 16) // Extract "HH:mm" from ISO format
        val endHourMin = endTime.substring(11, 16)

        return timeSlot >= startHourMin && timeSlot < endHourMin
    }

    /**
     * Kiểm tra xem booking này có đang trong trạng thái pending/confirmed không
     * (tức là đang "đóng băng" slot)
     */
    fun isBlocking(): Boolean {
        return status == BookingStatus.PENDING_PAYMENT ||
               status == BookingStatus.PAYMENT_UPLOADED ||
               status == BookingStatus.CONFIRMED
    }
}

