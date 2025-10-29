package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.CourtApi
import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import com.example.bookingcourt.domain.model.Court
import com.example.bookingcourt.domain.model.CourtType
import com.example.bookingcourt.domain.model.SportType
import com.example.bookingcourt.domain.repository.CourtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourtRepositoryImpl @Inject constructor(
    private val courtApi: CourtApi,
) : CourtRepository {

    companion object {
        private const val TAG = "CourtRepository"
    }

    override suspend fun getAllCourts(): Flow<Resource<List<Court>>> = flow {
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

    override suspend fun getCourtById(courtId: Long): Flow<Resource<Court>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Fetching court by ID: $courtId")

            val response = courtApi.getCourtById(courtId)

            if (response.isSuccessful) {
                val courtDto = response.body()
                if (courtDto != null) {
                    val court = courtDto.toDomain()
                    Log.d(TAG, "Successfully fetched court: ${court.name}")
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

    override suspend fun getCourtsByVenueId(venueId: Long): Flow<Resource<List<Court>>> = flow {
        try {
            emit(Resource.Loading())

            // Get all courts and filter by venueId
            val response = courtApi.getAllCourts()

            if (response.isSuccessful) {
                val courtsDto = response.body() ?: emptyList()
                val filteredCourts = courtsDto
                    .filter { it.venue.id == venueId } // Đổi từ 'venues' -> 'venue'
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
     * Convert CourtDetailDto to Court domain model
     */
    private fun CourtDetailDto.toDomain(): Court {
        return Court(
            id = id.toString(),
            name = "${venue.name} - ${description}", // Đổi từ 'venues' -> 'venue'
            description = description,
            address = venue.name, // Đổi từ 'venues' -> 'venue'
            latitude = 21.0, // Mock - backend doesn't provide coordinates
            longitude = 105.8, // Mock - backend doesn't provide coordinates
            images = emptyList(), // Backend doesn't provide images
            sportType = SportType.BADMINTON, // Default - backend doesn't specify
            courtType = CourtType.INDOOR, // Default - backend doesn't specify
            pricePerHour = 100000, // Default - will get from PriceRules later
            openTime = LocalTime(6, 0), // Default
            closeTime = LocalTime(22, 0), // Default
            amenities = emptyList(),
            rules = null,
            ownerId = venue.id.toString(), // Đổi từ 'venues' -> 'venue'
            rating = 4.0f + (id % 5) * 0.2f, // Mock rating
            totalReviews = 10 + (id.toInt() * 5), // Mock reviews
            isActive = booked != true, // Fix nullable Boolean: nếu booked == true thì isActive = false
            maxPlayers = 4,
        )
    }
}
