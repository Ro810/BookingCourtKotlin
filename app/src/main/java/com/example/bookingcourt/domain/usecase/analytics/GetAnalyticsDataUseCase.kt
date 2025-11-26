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

        // Chỉ đếm booking đã duyệt, hoàn thành và từ chối (không tính CANCELLED, EXPIRED, NO_SHOW, PENDING)
        val countableBookings = filteredBookings.filter {
            it.status == BookingStatus.CONFIRMED ||
                    it.status == BookingStatus.COMPLETED ||
                    it.status == BookingStatus.REJECTED
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
            totalBookings = countableBookings.size,
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

        // Tìm Chủ nhật đầu tiên của tháng
        val firstSunday = if (startOfMonth.dayOfWeek == DayOfWeek.SUNDAY) {
            startOfMonth
        } else {
            // Tính số ngày cần cộng để đến Chủ nhật đầu tiên
            val daysUntilSunday = 7 - startOfMonth.dayOfWeek.value
            startOfMonth.plus(DatePeriod(days = daysUntilSunday))
        }

        // Group bookings theo tuần
        // Tuần 1: từ ngày 1 tháng đến Chủ nhật đầu tiên
        // Tuần 2+: mỗi tuần là T2-CN
        val groupedByWeek = bookings.groupBy { booking ->
            val bookingDate = booking.startTime.date

            if (bookingDate <= firstSunday) {
                // Tuần 1: từ ngày 1 tháng đến Chủ nhật đầu tiên
                1
            } else {
                // Tuần 2+: mỗi tuần là T2-CN
                val daysSinceFirstSunday = bookingDate.toEpochDays() - firstSunday.toEpochDays()
                ((daysSinceFirstSunday - 1) / 7) + 2
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
        // Tạo data class tạm để lưu thông tin từng court trong booking
        data class CourtBookingInfo(
            val courtId: String,
            val courtName: String,
            val venueName: String,
            val price: Long,
            val isCompleted: Boolean
        )

        // Flatten tất cả booking items từ mỗi booking
        val allCourtBookings = bookings.flatMap { booking ->
            val isCompleted = booking.status == BookingStatus.COMPLETED
            val venueName = booking.venue.name

            when {
                // Nếu có bookingItems (multi-court booking)
                !booking.bookingItems.isNullOrEmpty() -> {
                    booking.bookingItems.map { item ->
                        CourtBookingInfo(
                            courtId = item.courtId,
                            courtName = item.courtName,
                            venueName = venueName,
                            price = item.price,
                            isCompleted = isCompleted
                        )
                    }
                }
                // Nếu là legacy single court booking
                booking.court != null -> {
                    listOf(
                        CourtBookingInfo(
                            courtId = booking.court.id,
                            courtName = booking.court.description,
                            venueName = venueName,
                            price = booking.totalPrice,
                            isCompleted = isCompleted
                        )
                    )
                }
                else -> emptyList()
            }
        }

        // Group by court
        val groupedByCourt = allCourtBookings.groupBy { it.courtId }

        return groupedByCourt.map { (courtId, courtBookings) ->
            val firstBooking = courtBookings.first()
            VenuePerformance(
                venueId = courtId,
                venueName = "${firstBooking.venueName} - ${firstBooking.courtName}",
                bookingCount = courtBookings.size,
                revenue = courtBookings.sumOf { it.price },
                completedBookings = courtBookings.count { it.isCompleted }
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
