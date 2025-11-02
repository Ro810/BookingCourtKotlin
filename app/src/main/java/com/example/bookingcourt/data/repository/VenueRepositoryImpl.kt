package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.remote.api.VenueApi
import com.example.bookingcourt.data.remote.dto.AddressDto
import com.example.bookingcourt.data.remote.dto.CreateAddressRequest
import com.example.bookingcourt.data.remote.dto.CreateVenueRequest
import com.example.bookingcourt.data.remote.dto.UpdateVenueRequest
import com.example.bookingcourt.data.remote.dto.VenueDetailDto
import com.example.bookingcourt.domain.model.Address
import com.example.bookingcourt.domain.model.Venue
import com.example.bookingcourt.domain.repository.VenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VenueRepositoryImpl @Inject constructor(
    private val venueApi: VenueApi,
) : VenueRepository {

    companion object {
        private const val TAG = "VenueRepository"
    }

    override suspend fun getVenues(): Flow<Resource<List<Venue>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== FETCHING VENUES ==========")
            Log.d(TAG, "API Call: GET /api/venues")

            val response = venueApi.getVenues()

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")
                Log.d(TAG, "Response data size: ${apiResponse?.data?.size}")

                if (apiResponse != null && apiResponse.success) {
                    val venues = apiResponse.data?.map { it.toDomain() } ?: emptyList()

                    Log.d(TAG, "✓ Successfully fetched ${venues.size} venues")
                    venues.forEachIndexed { index, venue ->
                        Log.d(TAG, "  [$index] ${venue.name} - ${venue.courtsCount} courts")
                    }
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(venues))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tải danh sách sân"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")
                emit(Resource.Error("Lỗi kết nối: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception fetching venues: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun searchVenues(
        name: String?,
        province: String?,
        district: String?,
        detail: String?,
    ): Flow<Resource<List<Venue>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Searching venues - name: $name, province: $province, district: $district")

            val response = venueApi.searchVenues(name, province, district, detail)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success) {
                    val venues = apiResponse.data?.map { it.toDomain() } ?: emptyList()

                    Log.d(TAG, "Search found ${venues.size} venues")
                    emit(Resource.Success(venues))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không tìm thấy kết quả"
                    emit(Resource.Error(errorMsg))
                }
            } else {
                emit(Resource.Error("Lỗi tìm kiếm: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception searching venues", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun getVenueById(venueId: Long): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Fetching venue by ID: $venueId")

            val response = venueApi.getVenueById(venueId)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val venue = apiResponse.data.toDomain()

                    Log.d(TAG, "Successfully fetched venue: ${venue.name}")
                    emit(Resource.Success(venue))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không tìm thấy sân"
                    emit(Resource.Error(errorMsg))
                }
            } else {
                emit(Resource.Error("Lỗi tải thông tin sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching venue by id: $venueId", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun createVenue(
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String
    ): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== CREATING VENUE ==========")
            Log.d(TAG, "Venue name: $name")
            Log.d(TAG, "Phone: $phoneNumber, Email: $email")
            Log.d(TAG, "Address: $detailAddress, $district, $provinceOrCity")

            val request = CreateVenueRequest(
                name = name,
                description = description,
                phoneNumber = phoneNumber,
                email = email,
                address = CreateAddressRequest(
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress
                )
            )

            val response = venueApi.createVenue(request)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val venue = apiResponse.data.toDomain()

                    Log.d(TAG, "✓ Successfully created venue: ${venue.name} (ID: ${venue.id})")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(venue))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tạo sân"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")
                emit(Resource.Error("Lỗi tạo sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception creating venue: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun updateVenue(
        venueId: Long,
        name: String,
        description: String?,
        phoneNumber: String,
        email: String,
        provinceOrCity: String,
        district: String,
        detailAddress: String
    ): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATING VENUE ==========")
            Log.d(TAG, "Venue ID: $venueId")
            Log.d(TAG, "Venue name: $name")
            Log.d(TAG, "Phone: $phoneNumber, Email: $email")
            Log.d(TAG, "Address: $detailAddress, $district, $provinceOrCity")

            val request = UpdateVenueRequest(
                name = name,
                description = description,
                phoneNumber = phoneNumber,
                email = email,
                address = CreateAddressRequest(
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress
                )
            )

            val response = venueApi.updateVenue(venueId, request)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val venue = apiResponse.data.toDomain()

                    Log.d(TAG, "✓ Successfully updated venue: ${venue.name} (ID: ${venue.id})")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(venue))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể cập nhật sân"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")
                emit(Resource.Error("Lỗi cập nhật sân: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception updating venue: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun deleteVenue(venueId: Long): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== DELETING VENUE ==========")
            Log.d(TAG, "Venue ID: $venueId")

            val response = venueApi.deleteVenue(venueId)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")

                if (apiResponse != null && apiResponse.success) {
                    Log.d(TAG, "✓ Successfully deleted venue: $venueId")
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(Unit))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể xóa sân"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")
                emit(Resource.Error("Lỗi xóa sân: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception deleting venue: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    /**
     * Convert VenueDetailDto to Domain Model
     */
    private fun VenueDetailDto.toDomain(): Venue {
        // Log để debug
        Log.d(TAG, "Converting VenueDetailDto to Domain:")
        Log.d(TAG, "  - Venue name: $name")
        Log.d(TAG, "  - phoneNumber: $phoneNumber")
        Log.d(TAG, "  - email: $email")
        Log.d(TAG, "  - owner: $owner")
        Log.d(TAG, "  - owner?.phone: ${owner?.phone}")

        return Venue(
            id = id,
            name = name,
            numberOfCourt = numberOfCourt,
            address = address.toDomain(),
            courtsCount = numberOfCourt,
            pricePerHour = pricePerHour ?: 0,
            averageRating = averageRating ?: 0f,
            totalReviews = totalReviews ?: 0,
            phoneNumber = phoneNumber,
            email = email,
            ownerPhone = owner?.phone // Lấy số điện thoại từ owner
        )
    }

    private fun AddressDto.toDomain(): Address {
        return Address(
            id = id,
            provinceOrCity = provinceOrCity,
            district = district,
            detailAddress = detailAddress
        )
    }
}
