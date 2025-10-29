package com.example.bookingcourt.core.di

import com.example.bookingcourt.data.repository.AuthRepositoryImpl
import com.example.bookingcourt.data.repository.VenueRepositoryImpl
import com.example.bookingcourt.domain.repository.AuthRepository
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
}
