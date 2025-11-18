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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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

    override suspend fun getMyVenues(): Flow<Resource<List<Venue>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== FETCHING MY VENUES ==========")
            Log.d(TAG, "API Call: GET /api/venues/my-venues")

            val response = venueApi.getMyVenues()

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

                    Log.d(TAG, "✓ Successfully fetched ${venues.size} my venues")
                    venues.forEachIndexed { index, venue ->
                        Log.d(TAG, "  [$index] ${venue.name} - ${venue.courtsCount} courts")
                    }
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(venues))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tải danh sách sân của bạn"
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
            Log.e(TAG, "⚠ Exception fetching my venues: ${e.message}", e)
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
                    Log.d(TAG, "API Response data images: ${apiResponse.data.images}")
                    Log.d(TAG, "API Response data images count: ${apiResponse.data.images?.size ?: 0}")

                    val venue = apiResponse.data.toDomain()

                    Log.d(TAG, "Successfully fetched venue: ${venue.name}")
                    Log.d(TAG, "Domain Venue images: ${venue.images}")
                    Log.d(TAG, "Domain Venue images count: ${venue.images?.size ?: 0}")
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
        numberOfCourt: Int?,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double?,
        openingTime: String?,
        closingTime: String?
    ): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== CREATING VENUE ==========")
            Log.d(TAG, "Venue name: $name")
            Log.d(TAG, "Phone: $phoneNumber, Email: $email")
            Log.d(TAG, "Number of courts: $numberOfCourt")
            Log.d(TAG, "Address: $detailAddress, $district, $provinceOrCity")
            Log.d(TAG, "Price/hour: $pricePerHour, Time: $openingTime - $closingTime")

            val request = CreateVenueRequest(
                name = name,
                description = description,
                phoneNumber = phoneNumber,
                email = email,
                numberOfCourt = numberOfCourt,
                address = CreateAddressRequest(
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress
                ),
                pricePerHour = pricePerHour,
                openingTime = openingTime,
                closingTime = closingTime
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
        numberOfCourt: Int?,
        provinceOrCity: String,
        district: String,
        detailAddress: String,
        pricePerHour: Double?,
        openingTime: String?,
        closingTime: String?,
        images: List<String>?
    ): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATING VENUE ==========")
            Log.d(TAG, "Venue ID: $venueId")
            Log.d(TAG, "Venue name: $name")
            Log.d(TAG, "Phone: $phoneNumber, Email: $email")
            Log.d(TAG, "Number of courts: $numberOfCourt")
            Log.d(TAG, "Address: $detailAddress, $district, $provinceOrCity")
            Log.d(TAG, "Price/hour: $pricePerHour, Time: $openingTime - $closingTime")
            Log.d(TAG, "Images: ${images?.size ?: 0} items")

            val request = UpdateVenueRequest(
                name = name,
                description = description,
                phoneNumber = phoneNumber,
                email = email,
                numberOfCourt = numberOfCourt,
                address = CreateAddressRequest(
                    provinceOrCity = provinceOrCity,
                    district = district,
                    detailAddress = detailAddress
                ),
                pricePerHour = pricePerHour,
                openingTime = openingTime,
                closingTime = closingTime,
                images = images
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
        Log.d(TAG, "  - ownerPhoneNumber: $ownerPhoneNumber")

        return Venue(
            id = id,
            name = name,
            description = description,
            numberOfCourt = numberOfCourt,
            address = address.toDomain(),
            courtsCount = courtsCount ?: numberOfCourt,
            pricePerHour = pricePerHour ?: 0,
            averageRating = averageRating ?: 0f,
            totalReviews = totalReviews ?: 0,
            openingTime = openingTime,
            closingTime = closingTime,
            phoneNumber = phoneNumber,
            email = email,
            ownerPhone = ownerPhoneNumber ?: owner?.phone, // Ưu tiên ownerPhoneNumber từ API response
            images = images // Sử dụng images từ API response
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

    override suspend fun getCourtsAvailability(
        venueId: Long,
        startTime: String,
        endTime: String
    ): Flow<Resource<List<com.example.bookingcourt.domain.model.CourtAvailability>>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== FETCHING COURTS AVAILABILITY ==========")
            Log.d(TAG, "Venue ID: $venueId")
            Log.d(TAG, "Start Time: $startTime")
            Log.d(TAG, "End Time: $endTime")

            val response = venueApi.getCourtsAvailability(venueId, startTime, endTime)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")
                Log.d(TAG, "Response data - venueName: ${apiResponse?.data?.venueName}")
                Log.d(TAG, "Response data - courts count: ${apiResponse?.data?.courts?.size}")

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val courtsAvailability = apiResponse.data.courts.map { dto ->
                        com.example.bookingcourt.domain.model.CourtAvailability(
                            courtId = dto.id,
                            courtName = dto.description,
                            available = dto.available,
                            bookedSlots = dto.bookedSlots?.map { slotDto ->
                                com.example.bookingcourt.domain.model.BookedSlotInfo(
                                    startTime = slotDto.getStartTimeString(),
                                    endTime = slotDto.getEndTimeString(),
                                    bookingId = slotDto.bookingId
                                )
                            } ?: emptyList()
                        )
                    }

                    Log.d(TAG, "✓ Successfully fetched ${courtsAvailability.size} courts availability")
                    courtsAvailability.forEachIndexed { index, court ->
                        Log.d(TAG, "  [$index] ${court.courtName} - Available: ${court.available}, Booked Slots: ${court.bookedSlots.size}")
                    }
                    Log.d(TAG, "======================================")
                    emit(Resource.Success(courtsAvailability))
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể tải thông tin tình trạng sân"
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
            Log.e(TAG, "⚠ Exception fetching courts availability: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun uploadVenueImages(venueId: Long, imageFiles: List<File>): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPLOADING VENUE IMAGES ==========")
            Log.d(TAG, "Venue ID: $venueId")
            Log.d(TAG, "Number of images: ${imageFiles.size}")

            if (imageFiles.isEmpty()) {
                Log.e(TAG, "⚠ Không có file nào để upload")
                emit(Resource.Error("Vui lòng chọn ít nhất một ảnh"))
                return@flow
            }

            // Validate all files
            imageFiles.forEachIndexed { index, file ->
                Log.d(TAG, "Image $index: ${file.absolutePath} (${file.length()} bytes)")
                if (!file.exists() || file.length() == 0L) {
                    Log.e(TAG, "⚠ File $index không tồn tại hoặc rỗng")
                    emit(Resource.Error("File ${file.name} không hợp lệ"))
                    return@flow
                }
            }

            // Create multipart body parts for all images
            val imageParts = imageFiles.map { file ->
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }

            val response = venueApi.uploadVenueImages(venueId, imageParts)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")

                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    val uploadedImages = apiResponse.data
                    Log.d(TAG, "API Response uploaded images: $uploadedImages")
                    Log.d(TAG, "API Response uploaded images count: ${uploadedImages.size}")

                    // Fetch venue detail để lấy thông tin đầy đủ sau khi upload
                    Log.d(TAG, "Fetching venue detail after upload...")
                    val venueDetailResponse = venueApi.getVenueById(venueId)

                    if (venueDetailResponse.isSuccessful) {
                        val venueApiResponse = venueDetailResponse.body()
                        if (venueApiResponse != null && venueApiResponse.success && venueApiResponse.data != null) {
                            val venue = venueApiResponse.data.toDomain()

                            Log.d(TAG, "✓ Successfully uploaded ${imageFiles.size} images for venue: ${venue.name}")
                            Log.d(TAG, "Domain Venue images: ${venue.images}")
                            Log.d(TAG, "Domain Venue images count: ${venue.images?.size ?: 0}")
                            Log.d(TAG, "======================================")
                            emit(Resource.Success(venue))
                        } else {
                            Log.e(TAG, "⚠ Failed to fetch venue detail after upload")
                            Log.d(TAG, "======================================")
                            emit(Resource.Error("Upload thành công nhưng không thể tải thông tin sân"))
                        }
                    } else {
                        Log.e(TAG, "⚠ Failed to fetch venue detail: ${venueDetailResponse.code()}")
                        Log.d(TAG, "======================================")
                        emit(Resource.Error("Upload thành công nhưng không thể tải thông tin sân"))
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể upload ảnh"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")

                val message = when (response.code()) {
                    400 -> "File không hợp lệ. Vui lòng chọn ảnh khác."
                    401 -> "Vui lòng đăng nhập lại"
                    413 -> "File quá lớn. Vui lòng chọn ảnh nhỏ hơn."
                    500 -> "Lỗi server. Vui lòng thử lại sau."
                    else -> "Lỗi upload ảnh: ${response.code()}"
                }
                emit(Resource.Error(message))
            }
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "⚠ HTTP Exception uploading images: ${e.code()}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error("Lỗi kết nối: ${e.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception uploading images: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun deleteVenueImage(
        venueId: Long,
        imageUrl: String
    ): Flow<Resource<Venue>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "======================================")
            Log.d(TAG, "Deleting venue image...")
            Log.d(TAG, "Venue ID: $venueId")
            Log.d(TAG, "Image URL: $imageUrl")

            val response = venueApi.deleteVenueImage(venueId, imageUrl)

            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()

                Log.d(TAG, "Response body is null: ${apiResponse == null}")
                Log.d(TAG, "Response success: ${apiResponse?.success}")
                Log.d(TAG, "Response message: ${apiResponse?.message}")
                Log.d(TAG, "Response data is null: ${apiResponse?.data == null}")

                if (apiResponse != null && apiResponse.success) {
                    // API trả về success, fetch venue detail để lấy thông tin cập nhật
                    Log.d(TAG, "Fetching venue detail after delete...")
                    val venueDetailResponse = venueApi.getVenueById(venueId)

                    if (venueDetailResponse.isSuccessful) {
                        val venueApiResponse = venueDetailResponse.body()
                        if (venueApiResponse != null && venueApiResponse.success && venueApiResponse.data != null) {
                            val venue = venueApiResponse.data.toDomain()

                            Log.d(TAG, "✓ Successfully deleted image for venue: ${venue.name}")
                            Log.d(TAG, "Domain Venue images: ${venue.images}")
                            Log.d(TAG, "Domain Venue images count: ${venue.images?.size ?: 0}")
                            Log.d(TAG, "======================================")
                            emit(Resource.Success(venue))
                        } else {
                            Log.e(TAG, "⚠ Failed to fetch venue detail after delete")
                            Log.d(TAG, "======================================")
                            emit(Resource.Error("Xóa ảnh thành công nhưng không thể tải thông tin sân"))
                        }
                    } else {
                        Log.e(TAG, "⚠ Failed to fetch venue detail: ${venueDetailResponse.code()}")
                        Log.d(TAG, "======================================")
                        emit(Resource.Error("Xóa ảnh thành công nhưng không thể tải thông tin sân"))
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Không thể xóa ảnh"
                    Log.e(TAG, "⚠ API returned success=false: $errorMsg")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "⚠ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                Log.d(TAG, "======================================")

                val message = when (response.code()) {
                    400 -> "Không thể xóa ảnh. Vui lòng thử lại."
                    401 -> "Vui lòng đăng nhập lại"
                    404 -> "Không tìm thấy ảnh"
                    500 -> "Lỗi server. Vui lòng thử lại sau."
                    else -> "Lỗi xóa ảnh: ${response.code()}"
                }
                emit(Resource.Error(message))
            }
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "⚠ HTTP Exception deleting image: ${e.code()}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error("Lỗi kết nối: ${e.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "⚠ Exception deleting image: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }
}
