package com.example.bookingcourt.core.di

import android.content.Context
import androidx.room.Room
import com.example.bookingcourt.core.utils.Constants
import com.example.bookingcourt.data.local.dao.BookingDao
import com.example.bookingcourt.data.local.dao.CourtDao
import com.example.bookingcourt.data.local.dao.UserDao
import com.example.bookingcourt.data.local.database.BookingCourtDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBookingCourtDatabase(
        @ApplicationContext context: Context,
    ): BookingCourtDatabase = Room.databaseBuilder(
        context,
        BookingCourtDatabase::class.java,
        Constants.DATABASE_NAME,
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideCourtDao(database: BookingCourtDatabase): CourtDao = database.courtDao()

    @Provides
    @Singleton
    fun provideBookingDao(database: BookingCourtDatabase): BookingDao = database.bookingDao()

    @Provides
    @Singleton
    fun provideUserDao(database: BookingCourtDatabase): UserDao = database.userDao()
}
