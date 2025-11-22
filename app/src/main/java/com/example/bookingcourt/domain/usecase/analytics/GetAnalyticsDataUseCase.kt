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

        // Doanh thu theo tuần (cho filter Tháng)
        val revenueByWeek = if (period == AnalyticsPeriod.MONTH) {
            calculateRevenueByWeek(revenueBookings)
        } else {
            emptyList()
        }

        // Doanh thu theo tháng (cho filter Năm)
        val revenueByMonth = if (period == AnalyticsPeriod.YEAR) {
            calculateRevenueByMonth(revenueBookings)
        } else {
            emptyList()
        }

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
            revenueByWeek = revenueByWeek,
            revenueByMonth = revenueByMonth,
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

        return when (period) {
            AnalyticsPeriod.DAY -> {
                // Chỉ lấy bookings trong ngày hôm nay
                bookings.filter { booking ->
                    booking.startTime.date == today
                }
            }
            AnalyticsPeriod.WEEK -> {
                // Lấy từ thứ 2 tuần này đến chủ nhật tuần này
                val dayOfWeek = today.dayOfWeek.value // 1=Monday, 7=Sunday
                val startOfWeek = today.minus(DatePeriod(days = dayOfWeek - 1)) // Thứ 2
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 6)) // Chủ nhật

                bookings.filter { booking ->
                    val bookingDate = booking.startTime.date
                    bookingDate >= startOfWeek && bookingDate <= endOfWeek
                }
            }
            AnalyticsPeriod.MONTH -> {
                // Lấy từ ngày 1 đến ngày cuối tháng này
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = startOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

                bookings.filter { booking ->
                    val bookingDate = booking.startTime.date
                    bookingDate >= startOfMonth && bookingDate <= endOfMonth
                }
            }
            AnalyticsPeriod.YEAR -> {
                // Lấy từ ngày 1/1 đến 31/12 năm này
                val startOfYear = LocalDate(today.year, 1, 1)
                val endOfYear = LocalDate(today.year, 12, 31)

                bookings.filter { booking ->
                    val bookingDate = booking.startTime.date
                    bookingDate >= startOfYear && bookingDate <= endOfYear
                }
            }
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

        // Conversion rate: (confirmed + completed) / (confirmed + completed + rejected)
        val successCount = confirmedCount + completedCount
        val conversionRate = if (successCount + rejectedCount > 0) {
            successCount.toFloat() / (successCount + rejectedCount)
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
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date

        // Xác định khoảng ngày cần hiển thị
        val (startDate, endDate) = when (period) {
            AnalyticsPeriod.DAY -> {
                today to today
            }
            AnalyticsPeriod.WEEK -> {
                val dayOfWeek = today.dayOfWeek.value
                val startOfWeek = today.minus(DatePeriod(days = dayOfWeek - 1))
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 6))
                startOfWeek to endOfWeek
            }
            AnalyticsPeriod.MONTH -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = startOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
                startOfMonth to endOfMonth
            }
            AnalyticsPeriod.YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                val endOfYear = LocalDate(today.year, 12, 31)
                startOfYear to endOfYear
            }
        }

        // Group bookings by date
        val groupedByDate = bookings.groupBy { it.startTime.date }

        // Tạo danh sách đầy đủ các ngày trong khoảng
        val allDates = mutableListOf<DailyRevenue>()
        var currentDate = startDate

        while (currentDate <= endDate) {
            val bookingsOnDate = groupedByDate[currentDate] ?: emptyList()
            allDates.add(
                DailyRevenue(
                    date = currentDate,
                    revenue = bookingsOnDate.sumOf { it.totalPrice },
                    bookingCount = bookingsOnDate.size
                )
            )
            currentDate = currentDate.plus(DatePeriod(days = 1))
        }

        return allDates
    }

    private fun calculateRevenueByWeek(bookings: List<BookingDetail>): List<WeeklyRevenue> {
        if (bookings.isEmpty()) {
            return (1..5).map { weekNumber ->
                WeeklyRevenue(
                    weekNumber = weekNumber,
                    weekLabel = "Tuần $weekNumber",
                    revenue = 0,
                    bookingCount = 0
                )
            }
        }

        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date

        // Lấy ngày đầu tháng
        val startOfMonth = LocalDate(today.year, today.month, 1)

        // Tìm thứ 2 đầu tiên của tháng
        val firstMonday = if (startOfMonth.dayOfWeek == DayOfWeek.MONDAY) {
            startOfMonth
        } else {
            // Tính số ngày cần cộng để đến thứ 2 đầu tiên
            val daysUntilMonday = (DayOfWeek.MONDAY.value - startOfMonth.dayOfWeek.value + 7) % 7
            if (daysUntilMonday == 0) startOfMonth
            else startOfMonth.plus(DatePeriod(days = daysUntilMonday))
        }

        // Group bookings theo tuần (T2-CN)
        val groupedByWeek = bookings.groupBy { booking ->
            val bookingDate = booking.startTime.date

            // Nếu booking trước thứ 2 đầu tiên, không tính vào tuần nào
            if (bookingDate < firstMonday) {
                0
            } else {
                // Tính số ngày từ thứ 2 đầu tiên
                val daysSinceFirstMonday = bookingDate.toEpochDays() - firstMonday.toEpochDays()
                // Chia cho 7 để lấy tuần (tuần 1, 2, 3, ...)
                (daysSinceFirstMonday / 7).toInt() + 1
            }
        }

        // Tạo danh sách đầy đủ 5 tuần
        return (1..5).map { weekNumber ->
            val weekBookings = groupedByWeek[weekNumber] ?: emptyList()
            WeeklyRevenue(
                weekNumber = weekNumber,
                weekLabel = "Tuần $weekNumber",
                revenue = weekBookings.sumOf { it.totalPrice },
                bookingCount = weekBookings.size
            )
        }
    }

    private fun calculateRevenueByMonth(bookings: List<BookingDetail>): List<MonthlyRevenue> {
        // Group by month number
        val groupedByMonth = bookings.groupBy { booking ->
            booking.startTime.monthNumber
        }

        // Tạo danh sách đầy đủ 12 tháng, tháng nào không có data thì revenue = 0
        return (1..12).map { monthNumber ->
            val monthBookings = groupedByMonth[monthNumber] ?: emptyList()
            MonthlyRevenue(
                monthNumber = monthNumber,
                monthLabel = "T$monthNumber",
                revenue = monthBookings.sumOf { it.totalPrice },
                bookingCount = monthBookings.size
            )
        }
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
