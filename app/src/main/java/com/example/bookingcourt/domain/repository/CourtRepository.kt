package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.CourtDetail
import kotlinx.coroutines.flow.Flow

interface CourtRepository {
    /**
     * Lấy tất cả courts từ backend
     */
    suspend fun getAllCourts(): Flow<Resource<List<CourtDetail>>>

    /**
     * Lấy court theo ID
     */
    suspend fun getCourtById(courtId: Long): Flow<Resource<CourtDetail>>

    /**
     * Lấy courts theo venue ID (filter local trước đây, giờ dùng API chuyên biệt)
     */
    suspend fun getCourtsByVenueId(venueId: Long): Flow<Resource<List<CourtDetail>>>

    /**
     * Kiểm tra khả dụng của court trong khoảng thời gian
     */
    suspend fun checkCourtAvailability(courtId: Long, startTime: String, endTime: String): Flow<Resource<Boolean>>
}
