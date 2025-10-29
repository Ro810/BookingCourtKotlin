package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.CreateVenueRequest
import com.example.bookingcourt.data.remote.dto.UpdateVenueRequest
import com.example.bookingcourt.data.remote.dto.VenueDetailDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueApi {
    /**
     * Get all venues
     * GET /api/venues
     */
    @GET("venues")
    suspend fun getVenues(): Response<ApiResponse<List<VenueDetailDto>>>

    /**
     * Search venues with filters
     * GET /api/venues/search
     */
    @GET("venues/search")
    suspend fun searchVenues(
        @Query("name") name: String? = null,
        @Query("province") province: String? = null,
        @Query("district") district: String? = null,
        @Query("detail") detail: String? = null,
    ): Response<ApiResponse<List<VenueDetailDto>>>

    /**
     * Get venue by ID
     * GET /api/venues/{id}
     */
    @GET("venues/{id}")
    suspend fun getVenueById(
        @Path("id") venueId: Long
    ): Response<ApiResponse<VenueDetailDto>>

    /**
     * Create a new venue (Owner only)
     * POST /api/venues
     */
    @POST("venues")
    suspend fun createVenue(
        @Body request: CreateVenueRequest
    ): Response<ApiResponse<VenueDetailDto>>

    /**
     * Update an existing venue (Owner only)
     * PUT /api/venues/{id}
     */
    @PUT("venues/{id}")
    suspend fun updateVenue(
        @Path("id") venueId: Long,
        @Body request: UpdateVenueRequest
    ): Response<ApiResponse<VenueDetailDto>>

    /**
     * Delete a venue (Owner only)
     * DELETE /api/venues/{id}
     */
    @DELETE("venues/{id}")
    suspend fun deleteVenue(
        @Path("id") venueId: Long
    ): Response<ApiResponse<Any>>
}
