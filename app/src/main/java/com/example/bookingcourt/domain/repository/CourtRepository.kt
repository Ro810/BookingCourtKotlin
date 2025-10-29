package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.Court
import kotlinx.coroutines.flow.Flow

interface CourtRepository {
    /**
     * Lấy tất cả courts từ backend
     */
    suspend fun getAllCourts(): Flow<Resource<List<Court>>>

    /**
     * Lấy court theo ID
     */
    suspend fun getCourtById(courtId: Long): Flow<Resource<Court>>

    /**
     * Lấy courts theo venue ID (filter local)
     */
    suspend fun getCourtsByVenueId(venueId: Long): Flow<Resource<List<Court>>>
}
