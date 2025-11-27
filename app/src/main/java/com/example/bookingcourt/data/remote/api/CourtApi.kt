package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import com.example.bookingcourt.data.remote.dto.CreateCourtRequest
import com.example.bookingcourt.data.remote.dto.UpdateCourtRequest
import com.example.bookingcourt.data.remote.dto.ToggleCourtStatusResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Court API - Quản lý courts
 * Based on HUONG_DAN_FRONTEND_API.md
 */
interface CourtApi {
    /**
     * Lấy tất cả courts
     * GET /api/courts
     */
    @GET("courts")
    suspend fun getAllCourts(): Response<List<CourtDetailDto>>

    /**
     * Lấy court theo ID
     * GET /api/courts/{id}
     */
    @GET("courts/{id}")
    suspend fun getCourtById(
        @Path("id") courtId: Long
    ): Response<CourtDetailDto>

    /**
     * Tạo court mới
     * POST /courts
     */
    @POST("courts")
    suspend fun createCourt(
        @Body request: CreateCourtRequest
    ): Response<ApiResponse<CourtDetailDto>>

    /**
     * Cập nhật court
     * PUT /courts/{id}
     */
    @PUT("courts/{id}")
    suspend fun updateCourt(
        @Path("id") courtId: Long,
        @Body request: UpdateCourtRequest
    ): Response<ApiResponse<CourtDetailDto>>

    /**
     * Xóa court
     * DELETE /courts/{id}
     */
    @DELETE("courts/{id}")
    suspend fun deleteCourt(
        @Path("id") courtId: Long
    ): Response<ApiResponse<Unit>>

    /**
     * Khóa/Mở khóa court
     * PATCH /courts/{id}/toggle-status
     * Toggle trạng thái hoạt động của court (khóa/mở khóa)
     * Chỉ chủ sân mới được phép thực hiện
     */
    @PATCH("courts/{id}/toggle-status")
    suspend fun toggleCourtStatus(
        @Path("id") courtId: Long
    ): Response<ToggleCourtStatusResponse>
}

