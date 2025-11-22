package com.example.bookingcourt.domain.model

import kotlinx.datetime.LocalDate

/**
 * Khoảng thời gian phân tích
 */
enum class AnalyticsPeriod {
    DAY,    // Theo ngày
    WEEK,   // Theo tuần
    MONTH,  // Theo tháng
    YEAR    // Theo năm
}

/**
 * Doanh thu theo ngày
 */
data class DailyRevenue(
    val date: LocalDate,
    val revenue: Long,
    val bookingCount: Int
)

/**
 * Doanh thu theo tuần trong tháng
 */
data class WeeklyRevenue(
    val weekNumber: Int,        // 1-5: Tuần thứ mấy trong tháng
    val weekLabel: String,       // "Tuần 1", "Tuần 2", etc.
    val revenue: Long,
    val bookingCount: Int
)

/**
 * Doanh thu theo tháng trong năm
 */
data class MonthlyRevenue(
    val monthNumber: Int,       // 1-12: Tháng thứ mấy trong năm
    val monthLabel: String,     // "T1", "T2", ..., "T12"
    val revenue: Long,
    val bookingCount: Int
)

/**
 * Thống kê tổng quan booking
 */
data class BookingStats(
    val totalBookings: Int,
    val pendingCount: Int,          // PENDING, PAYMENT_UPLOADED
    val confirmedCount: Int,         // CONFIRMED
    val completedCount: Int,         // COMPLETED
    val rejectedCount: Int,          // REJECTED
    val cancelledCount: Int,         // CANCELLED, NO_SHOW
    val conversionRate: Float        // confirmed / (confirmed + rejected)
)

/**
 * Hiệu suất theo venue
 */
data class VenuePerformance(
    val venueId: String,
    val venueName: String,
    val bookingCount: Int,
    val revenue: Long,
    val completedBookings: Int
)

/**
 * Thống kê theo khung giờ (peak hours)
 */
data class TimeSlotStats(
    val hour: Int,              // 0-23
    val bookingCount: Int,
    val revenue: Long
)

/**
 * Thống kê theo phương thức thanh toán
 */
data class PaymentMethodStats(
    val method: PaymentMethod,
    val count: Int,
    val totalAmount: Long,
    val percentage: Float
)

/**
 * Top khách hàng
 */
data class TopCustomer(
    val userId: String,
    val userName: String,
    val bookingCount: Int,
    val totalSpent: Long
)

/**
 * Dữ liệu phân tích tổng hợp
 */
data class AnalyticsData(
    val period: AnalyticsPeriod,
    val totalRevenue: Long,
    val totalBookings: Int,
    val averageBookingValue: Long,
    val bookingStats: BookingStats,
    val revenueByDate: List<DailyRevenue>,
    val revenueByWeek: List<WeeklyRevenue>,   // Doanh thu theo tuần (dùng cho filter Tháng)
    val revenueByMonth: List<MonthlyRevenue>, // Doanh thu theo tháng (dùng cho filter Năm)
    val venuePerformance: List<VenuePerformance>,
    val timeSlotStats: List<TimeSlotStats>,
    val paymentMethodStats: List<PaymentMethodStats>,
    val topCustomers: List<TopCustomer>,
    val peakHour: Int?,              // Giờ đặt nhiều nhất
    val bestVenue: VenuePerformance? // Sân hoạt động tốt nhất
)
