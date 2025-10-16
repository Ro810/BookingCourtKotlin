package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.CourtDto
import com.example.bookingcourt.data.remote.dto.CourtListResponseDto
import com.example.bookingcourt.data.remote.dto.TimeSlotDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CourtApi {
    @GET("courts")
    suspend fun getCourts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sport_type") sportType: String? = null,
        @Query("search") search: String? = null,
        @Query("min_price") minPrice: Long? = null,
        @Query("max_price") maxPrice: Long? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Double? = null,
    ): CourtListResponseDto

    @GET("courts/{id}")
    suspend fun getCourtById(@Path("id") courtId: String): CourtDto

    @GET("courts/{id}/time-slots")
    suspend fun getTimeSlots(
        @Path("id") courtId: String,
        @Query("date") date: String,
    ): List<TimeSlotDto>

    @GET("courts/favorites")
    suspend fun getFavoriteCourts(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): CourtListResponseDto

    @GET("courts/nearby")
    suspend fun getNearbyCourts(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 5.0,
    ): List<CourtDto>
}
