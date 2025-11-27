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
     * Lấy courts theo venue ID (filter local)
     */
    suspend fun getCourtsByVenueId(venueId: Long): Flow<Resource<List<CourtDetail>>>

    /**
     * Tạo court mới
     */
    suspend fun createCourt(
        description: String,
        venueId: Long
    ): Flow<Resource<CourtDetail>>

    /**
     * Cập nhật court
     */
    suspend fun updateCourt(
        courtId: Long,
        description: String
    ): Flow<Resource<CourtDetail>>

    /**
     * Xóa court
     */
    suspend fun deleteCourt(courtId: Long): Flow<Resource<Unit>>

    /**
     * Khóa/Mở khóa court
     * Toggle trạng thái hoạt động của court
     * @return Flow chứa thông tin court sau khi toggle (isActive, message)
     */
    suspend fun toggleCourtStatus(courtId: Long): Flow<Resource<ToggleCourtResult>>
}

/**
 * Kết quả của việc toggle trạng thái court
 */
data class ToggleCourtResult(
    val courtId: Long,
    val description: String,
    val isActive: Boolean,
    val venueId: Long,
    val message: String
)
