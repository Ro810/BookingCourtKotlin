package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.CourtApi
import com.example.bookingcourt.data.remote.dto.CourtDetailDto
import com.example.bookingcourt.data.remote.dto.CreateCourtRequest
import com.example.bookingcourt.data.remote.dto.UpdateCourtRequest
import com.example.bookingcourt.data.remote.dto.VenueIdDto
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

            Log.d(TAG, "========== GET COURTS BY VENUE ID ==========")
            Log.d(TAG, "  Venue ID: $venueId")

            // Get all courts and filter by venueId
            val response = courtApi.getAllCourts()

            Log.d(TAG, "  API Response Code: ${response.code()}")
            Log.d(TAG, "  API Response Success: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val courtsDto = response.body() ?: emptyList()
                Log.d(TAG, "  Total courts from API: ${courtsDto.size}")

                // Log tất cả courts để debug
                courtsDto.forEachIndexed { index, court ->
                    Log.d(TAG, "    Court $index: ID=${court.id}, VenueID=${court.venue.id}, Desc=${court.description}")
                }

                val filteredCourts = courtsDto
                    .filter { it.venue.id == venueId }
                    .map { it.toDomain() }

                Log.d(TAG, "  ✅ Filtered courts for venue $venueId: ${filteredCourts.size}")

                if (filteredCourts.isEmpty()) {
                    Log.w(TAG, "  ⚠️ No courts found for venue $venueId")
                    Log.w(TAG, "  ⚠️ This venue may not have any courts in database")
                }

                emit(Resource.Success(filteredCourts))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "  ❌ API Error: ${response.code()}")
                Log.e(TAG, "  ❌ Error body: $errorBody")
                emit(Resource.Error("Lỗi tải danh sách sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching courts for venue: $venueId", e)
            Log.e(TAG, "  Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "  Exception message: ${e.message}")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun createCourt(
        description: String,
        venueId: Long
    ): Flow<Resource<CourtDetail>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== CREATE COURT ==========")
            Log.d(TAG, "  Description: $description")
            Log.d(TAG, "  Venue ID: $venueId")

            val request = CreateCourtRequest(
                description = description,
                venues = VenueIdDto(id = venueId)
            )

            val response = courtApi.createCourt(request)

            Log.d(TAG, "  Response Code: ${response.code()}")
            Log.d(TAG, "  Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val court = apiResponse.data.toDomain()
                    Log.d(TAG, "  ✅ Successfully created court: ${court.description} (ID: ${court.id})")
                    emit(Resource.Success(court))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tạo sân"
                    Log.e(TAG, "  ⚠️ API returned success=false: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "  ❌ API error: ${response.code()}")
                Log.e(TAG, "  ❌ Error body: $errorBody")
                emit(Resource.Error("Lỗi tạo sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception creating court", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun updateCourt(
        courtId: Long,
        description: String
    ): Flow<Resource<CourtDetail>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATE COURT ==========")
            Log.d(TAG, "  Court ID: $courtId")
            Log.d(TAG, "  Description: $description")

            val request = UpdateCourtRequest(description = description)
            val response = courtApi.updateCourt(courtId, request)

            Log.d(TAG, "  Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val court = apiResponse.data.toDomain()
                    Log.d(TAG, "  ✅ Successfully updated court: ${court.description}")
                    emit(Resource.Success(court))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể cập nhật sân"
                    Log.e(TAG, "  ⚠️ API returned success=false: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "  ❌ API error: ${response.code()}")
                Log.e(TAG, "  ❌ Error body: $errorBody")
                emit(Resource.Error("Lỗi cập nhật sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating court", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun deleteCourt(courtId: Long): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== DELETE COURT ==========")
            Log.d(TAG, "  Court ID: $courtId")

            val response = courtApi.deleteCourt(courtId)

            Log.d(TAG, "  Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success) {
                    Log.d(TAG, "  ✅ Successfully deleted court ID: $courtId")
                    emit(Resource.Success(Unit))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể xóa sân"
                    Log.e(TAG, "  ⚠️ API returned success=false: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "  ❌ API error: ${response.code()}")
                Log.e(TAG, "  ❌ Error body: $errorBody")
                emit(Resource.Error("Lỗi xóa sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception deleting court", e)
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
