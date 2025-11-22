package com.example.bookingcourt.domain.usecase.analytics

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.*
import com.example.bookingcourt.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * Use case để lấy và xử lý dữ liệu analytics cho owner
 * Tính toán các metrics từ bookings data
 */
class GetAnalyticsDataUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(period: AnalyticsPeriod): Flow<Resource<AnalyticsData>> {
        return bookingRepository.getAllOwnerBookings().map { resource ->
            when (resource) {
                is Resource.Loading -> Resource.Loading()
                is Resource.Error -> Resource.Error(resource.message ?: "Lỗi không xác định")
                is Resource.Success -> {
                    val bookings = resource.data ?: emptyList()
                    val analyticsData = processAnalyticsData(bookings, period)
                    Resource.Success(analyticsData)
                }
            }
        }
    }

    private fun processAnalyticsData(
        bookings: List<BookingDetail>,
        period: AnalyticsPeriod
    ): AnalyticsData {
        // Filter bookings theo period
        val filteredBookings = filterBookingsByPeriod(bookings, period)

        // Chỉ tính revenue từ CONFIRMED và COMPLETED
        val revenueBookings = filteredBookings.filter {
            it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.COMPLETED
        }

        // Tính tổng doanh thu
        val totalRevenue = revenueBookings.sumOf { it.totalPrice }

        // Tính booking stats
        val bookingStats = calculateBookingStats(filteredBookings)

        // Doanh thu theo ngày
        val revenueByDate = calculateRevenueByDate(revenueBookings, period)

        // Hiệu suất venue
        val venuePerformance = calculateVenuePerformance(revenueBookings)

        // Thống kê theo giờ
        val timeSlotStats = calculateTimeSlotStats(revenueBookings)

        // Thống kê payment method (hiện tại chưa có trong BookingDetail, để empty)
        val paymentMethodStats = emptyList<PaymentMethodStats>()

        // Top customers
        val topCustomers = calculateTopCustomers(revenueBookings)

        // Peak hour
        val peakHour = timeSlotStats.maxByOrNull { it.bookingCount }?.hour

        // Best venue
        val bestVenue = venuePerformance.maxByOrNull { it.revenue }

        // Average booking value
        val averageBookingValue = if (revenueBookings.isNotEmpty()) {
            totalRevenue / revenueBookings.size
        } else 0L

        return AnalyticsData(
            period = period,
            totalRevenue = totalRevenue,
            totalBookings = filteredBookings.size,
            averageBookingValue = averageBookingValue,
            bookingStats = bookingStats,
            revenueByDate = revenueByDate,
            venuePerformance = venuePerformance,
            timeSlotStats = timeSlotStats,
            paymentMethodStats = paymentMethodStats,
            topCustomers = topCustomers,
            peakHour = peakHour,
            bestVenue = bestVenue
        )
    }

    private fun filterBookingsByPeriod(
        bookings: List<BookingDetail>,
        period: AnalyticsPeriod
    ): List<BookingDetail> {
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date

        val startDate = when (period) {
            AnalyticsPeriod.DAY -> today
            AnalyticsPeriod.WEEK -> {
                val daysToSubtract = DatePeriod(days = 7)
                today.minus(daysToSubtract)
            }
            AnalyticsPeriod.MONTH -> {
                val daysToSubtract = DatePeriod(days = 30)
                today.minus(daysToSubtract)
            }
            AnalyticsPeriod.YEAR -> {
                val yearsToSubtract = DatePeriod(years = 1)
                today.minus(yearsToSubtract)
            }
        }

        return bookings.filter { booking ->
            val bookingDate = booking.startTime.date
            bookingDate >= startDate
        }
    }

    private fun calculateBookingStats(bookings: List<BookingDetail>): BookingStats {
        val totalBookings = bookings.size
        val pendingCount = bookings.count {
            it.status == BookingStatus.PENDING || it.status == BookingStatus.PAYMENT_UPLOADED
        }
        val confirmedCount = bookings.count { it.status == BookingStatus.CONFIRMED }
        val completedCount = bookings.count { it.status == BookingStatus.COMPLETED }
        val rejectedCount = bookings.count { it.status == BookingStatus.REJECTED }
        val cancelledCount = bookings.count {
            it.status == BookingStatus.CANCELLED || it.status == BookingStatus.NO_SHOW
        }

        // Conversion rate: confirmed / (confirmed + rejected)
        val conversionRate = if (confirmedCount + rejectedCount > 0) {
            confirmedCount.toFloat() / (confirmedCount + rejectedCount)
        } else 0f

        return BookingStats(
            totalBookings = totalBookings,
            pendingCount = pendingCount,
            confirmedCount = confirmedCount,
            completedCount = completedCount,
            rejectedCount = rejectedCount,
            cancelledCount = cancelledCount,
            conversionRate = conversionRate
        )
    }

    private fun calculateRevenueByDate(
        bookings: List<BookingDetail>,
        period: AnalyticsPeriod
    ): List<DailyRevenue> {
        // Group by date
        val groupedByDate = bookings.groupBy { it.startTime.date }

        return groupedByDate.map { (date, bookingsOnDate) ->
            DailyRevenue(
                date = date,
                revenue = bookingsOnDate.sumOf { it.totalPrice },
                bookingCount = bookingsOnDate.size
            )
        }.sortedBy { it.date }
    }

    private fun calculateVenuePerformance(bookings: List<BookingDetail>): List<VenuePerformance> {
        // Group by venue
        val groupedByVenue = bookings.groupBy { it.venue.id }

        return groupedByVenue.map { (venueId, venueBookings) ->
            VenuePerformance(
                venueId = venueId,
                venueName = venueBookings.first().venue.name,
                bookingCount = venueBookings.size,
                revenue = venueBookings.sumOf { it.totalPrice },
                completedBookings = venueBookings.count { it.status == BookingStatus.COMPLETED }
            )
        }.sortedByDescending { it.revenue }
    }

    private fun calculateTimeSlotStats(bookings: List<BookingDetail>): List<TimeSlotStats> {
        // Group by hour
        val groupedByHour = bookings.groupBy { it.startTime.hour }

        return groupedByHour.map { (hour, hourBookings) ->
            TimeSlotStats(
                hour = hour,
                bookingCount = hourBookings.size,
                revenue = hourBookings.sumOf { it.totalPrice }
            )
        }.sortedBy { it.hour }
    }

    private fun calculateTopCustomers(bookings: List<BookingDetail>): List<TopCustomer> {
        // Group by customer
        val groupedByCustomer = bookings.groupBy { it.user.id }

        return groupedByCustomer.map { (userId, customerBookings) ->
            TopCustomer(
                userId = userId,
                userName = customerBookings.first().user.fullname,
                bookingCount = customerBookings.size,
                totalSpent = customerBookings.sumOf { it.totalPrice }
            )
        }.sortedByDescending { it.totalSpent }
            .take(10) // Top 10 customers
    }
}
