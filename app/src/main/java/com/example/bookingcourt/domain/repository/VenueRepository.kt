package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.CourtAvailability
import com.example.bookingcourt.domain.model.Venue
import kotlinx.coroutines.flow.Flow
import java.io.File

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
        numberOfCourt: Int? = null,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double? = null,
        openingTime: String? = null,
        closingTime: String? = null,
        images: List<String>? = null
    ): Flow<Resource<Venue>>

    suspend fun deleteVenue(venueId: Long): Flow<Resource<Unit>>

    /**
     * Get court availability for a specific time range
     * @param venueId ID của venue
     * @param startTime Thời gian bắt đầu (ISO format: "2025-11-07T14:00:00")
     * @param endTime Thời gian kết thúc (ISO format: "2025-11-07T15:00:00")
     * @return Danh sách courts với thông tin availability
     */
    suspend fun getCourtsAvailability(
        venueId: Long,
        startTime: String,
        endTime: String
    ): Flow<Resource<List<CourtAvailability>>>

    /**
     * Upload venue image
     * @param venueId ID của venue
     * @param imageFile File ảnh để upload
     * @return Venue sau khi upload ảnh
     */
    suspend fun uploadVenueImage(
        venueId: Long,
        imageFile: File
    ): Flow<Resource<Venue>>
}
