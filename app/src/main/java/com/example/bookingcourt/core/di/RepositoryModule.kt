package com.example.bookingcourt.core.di

import com.example.bookingcourt.data.repository.AuthRepositoryImpl
import com.example.bookingcourt.data.repository.BookingRepositoryImpl
import com.example.bookingcourt.data.repository.CourtRepositoryImpl
import com.example.bookingcourt.data.repository.ReviewRepositoryImpl
import com.example.bookingcourt.data.repository.VenueRepositoryImpl
import com.example.bookingcourt.domain.repository.AuthRepository
import com.example.bookingcourt.domain.repository.BookingRepository
import com.example.bookingcourt.domain.repository.CourtRepository
import com.example.bookingcourt.domain.repository.ReviewRepository
import com.example.bookingcourt.domain.repository.VenueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVenueRepository(
        venueRepositoryImpl: VenueRepositoryImpl,
    ): VenueRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl,
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindCourtRepository(
        courtRepositoryImpl: CourtRepositoryImpl,
    ): CourtRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl,
    ): ReviewRepository
}
