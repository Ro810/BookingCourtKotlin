package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Venue
import kotlinx.coroutines.flow.Flow

interface VenueRepository {
    suspend fun getVenues(): Flow<Resource<List<Venue>>>

    suspend fun getMyVenues(): Flow<Resource<List<Venue>>>

    suspend fun searchVenues(
        name: String? = null,
        province: String? = null,
        district: String? = null,
        detail: String? = null
    ): Flow<Resource<List<Venue>>>

    suspend fun getVenueById(venueId: Long): Flow<Resource<Venue>>

    suspend fun createVenue(
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        numberOfCourt: Int? = null,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double? = null,
        openingTime: String? = null,
        closingTime: String? = null
    ): Flow<Resource<Venue>>

    suspend fun updateVenue(
        venueId: Long,
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double? = null,
        openingTime: String? = null,
        closingTime: String? = null,
        images: List<String>? = null
    ): Flow<Resource<Venue>>

    suspend fun deleteVenue(venueId: Long): Flow<Resource<Unit>>
}
