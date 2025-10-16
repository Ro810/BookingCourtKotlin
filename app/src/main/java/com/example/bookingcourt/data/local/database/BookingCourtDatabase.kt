package com.example.bookingcourt.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookingcourt.data.local.dao.BookingDao
import com.example.bookingcourt.data.local.dao.CourtDao
import com.example.bookingcourt.data.local.dao.UserDao
import com.example.bookingcourt.data.local.entity.BookingEntity
import com.example.bookingcourt.data.local.entity.CourtEntity
import com.example.bookingcourt.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CourtEntity::class,
        BookingEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BookingCourtDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courtDao(): CourtDao
    abstract fun bookingDao(): BookingDao
}
