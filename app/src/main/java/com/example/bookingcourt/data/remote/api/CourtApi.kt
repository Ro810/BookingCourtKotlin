package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Court API - Lấy danh sách các sân cụ thể
 * Based on HUONG_DAN_FRONTEND_API.md
 */
interface CourtApi {
    /**
     * Lấy tất cả courts
     * GET /api/courts
     */
    @GET("api/courts")
    suspend fun getAllCourts(): Response<List<CourtDetailDto>>

    /**
     * Lấy court theo ID
     * GET /api/courts/{id}
     */
    @GET("api/courts/{id}")
    suspend fun getCourtById(
        @Path("id") courtId: Long
    ): Response<CourtDetailDto>
}

