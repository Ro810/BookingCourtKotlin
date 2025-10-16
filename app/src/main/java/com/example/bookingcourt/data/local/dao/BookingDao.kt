package com.example.bookingcourt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookingcourt.data.local.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY created_at DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY created_at DESC")
    fun getBookingsByStatus(status: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingByIdFlow(bookingId: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE start_time > :currentTime AND status = 'CONFIRMED' ORDER BY start_time ASC")
    fun getUpcomingBookings(currentTime: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE end_time < :currentTime ORDER BY end_time DESC")
    fun getPastBookings(currentTime: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE court_id = :courtId")
    fun getBookingsByCourtId(courtId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: String)

    @Query("UPDATE bookings SET payment_status = :paymentStatus WHERE id = :bookingId")
    suspend fun updatePaymentStatus(bookingId: String, paymentStatus: String)

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteBookingById(bookingId: String)

    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()
}
