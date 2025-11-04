package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.CourtApi
import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import com.example.bookingcourt.domain.model.CourtDetail
import com.example.bookingcourt.domain.model.CourtVenueInfo
import com.example.bookingcourt.domain.repository.CourtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourtRepositoryImpl @Inject constructor(
    private val courtApi: CourtApi,
) : CourtRepository {

    companion object {
        private const val TAG = "CourtRepository"
    }

    override suspend fun getAllCourts(): Flow<Resource<List<CourtDetail>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Fetching all courts from /api/courts")

            val response = courtApi.getAllCourts()

            if (response.isSuccessful) {
                val courtsDto = response.body() ?: emptyList()
                val courts = courtsDto.map { it.toDomain() }

                Log.d(TAG, "Successfully fetched ${courts.size} courts")
                emit(Resource.Success(courts))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API error: ${response.code()} - $errorBody")
                emit(Resource.Error("Lỗi tải danh sách sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching courts", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun getCourtById(courtId: Long): Flow<Resource<CourtDetail>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Fetching court by ID: $courtId")

            val response = courtApi.getCourtById(courtId)

            if (response.isSuccessful) {
                val courtDto = response.body()
                if (courtDto != null) {
                    val court = courtDto.toDomain()
                    Log.d(TAG, "Successfully fetched court: ${court.description}")
                    emit(Resource.Success(court))
                } else {
                    emit(Resource.Error("Không tìm thấy sân"))
                }
            } else {
                emit(Resource.Error("Lỗi tải thông tin sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching court by id: $courtId", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun getCourtsByVenueId(venueId: Long): Flow<Resource<List<CourtDetail>>> = flow {
        try {
            emit(Resource.Loading())

            // Get all courts and filter by venueId
            val response = courtApi.getAllCourts()

            if (response.isSuccessful) {
                val courtsDto = response.body() ?: emptyList()
                val filteredCourts = courtsDto
                    .filter { it.venue.id == venueId }
                    .map { it.toDomain() }

                Log.d(TAG, "Found ${filteredCourts.size} courts for venue $venueId")
                emit(Resource.Success(filteredCourts))
            } else {
                emit(Resource.Error("Lỗi tải danh sách sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching courts for venue: $venueId", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    /**
     * Convert CourtDetailDto to CourtDetail domain model
     */
    private fun CourtDetailDto.toDomain(): CourtDetail {
        return CourtDetail(
            id = id,
            description = description,
            booked = booked ?: false,
            venue = CourtVenueInfo(
                id = venue.id,
                name = venue.name
            )
        )
    }
}
