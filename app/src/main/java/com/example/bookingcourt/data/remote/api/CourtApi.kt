package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.CourtAvailabilityResponse
import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import com.example.bookingcourt.data.remote.dto.CourtRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Court API - Lấy danh sách các sân cụ thể
 * Based on HUONG_DAN_FRONTEND_API.md and CourtController.java
 */
interface CourtApi {
    /**
     * Lấy tất cả courts
     * GET /api/courts
     * Backend: CourtController.getAllCourts()
     */
    @GET("courts")
    suspend fun getAllCourts(): Response<List<CourtDetailDto>>

    /**
     * Lấy court theo ID
     * GET /api/courts/{id}
     * Backend: CourtController.getCourtById()
     */
    @GET("courts/{id}")
    suspend fun getCourtById(
        @Path("id") courtId: Long
    ): Response<CourtDetailDto>

    /**
     * Kiểm tra lịch trống của sân trong khoảng thời gian
     * GET /api/courts/{id}/availability?startTime=...&endTime=...
     * Backend: CourtController.checkAvailability()
     *
     * @param courtId ID của sân
     * @param startTime Thời gian bắt đầu (ISO format: 2025-11-06T14:00:00)
     * @param endTime Thời gian kết thúc (ISO format: 2025-11-06T16:00:00)
     * @return Thông tin về tính khả dụng và các slot đã đặt
     */
    @GET("courts/{id}/availability")
    suspend fun checkAvailability(
        @Path("id") courtId: Long,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String
    ): Response<CourtAvailabilityResponse>

    /**
     * Tạo court mới (Owner only)
     * POST /api/courts
     * Backend: CourtController.createCourt()
     * Requires: ROLE_OWNER
     */
    @POST("courts")
    suspend fun createCourt(
        @Body request: CourtRequest
    ): Response<ApiResponse<CourtDetailDto>>
}

