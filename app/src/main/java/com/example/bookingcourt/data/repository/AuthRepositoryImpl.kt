package com.example.bookingcourt.data.repository

import android.util.Log
import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.local.datastore.UserPreferencesDataStore
import com.example.bookingcourt.data.remote.api.AuthApi
import com.example.bookingcourt.data.remote.api.UserApi
import com.example.bookingcourt.data.remote.dto.ForgotPasswordRequest
import com.example.bookingcourt.data.remote.dto.LoginRequest
import com.example.bookingcourt.data.remote.dto.RegisterRequest
import com.example.bookingcourt.data.remote.dto.ErrorResponse
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.example.bookingcourt.domain.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import java.net.SocketTimeoutException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : AuthRepository {

    private val gson = Gson()

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Parse error response from backend to get meaningful error message
     */
    private fun parseErrorResponse(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Đã xảy ra lỗi"
        }

        Log.d(TAG, "Raw error body: $errorBody")

        return try {
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            Log.d(TAG, "Parsed error response: $errorResponse")

            // Prioritize specific error messages
            when {
                // Check for field-specific errors first
                !errorResponse.errors.isNullOrEmpty() -> {
                    // Get first error from errors map
                    val firstError = errorResponse.errors.entries.firstOrNull()
                    val field = firstError?.key ?: ""
                    val messages = firstError?.value ?: emptyList()

                    Log.d(TAG, "Field error - Field: $field, Messages: $messages")

                    when {
                        field.contains("phone", ignoreCase = true) ->
                            messages.firstOrNull() ?: "Số điện thoại không hợp lệ"
                        field.contains("email", ignoreCase = true) ->
                            messages.firstOrNull() ?: "Email không hợp lệ"
                        else -> messages.firstOrNull() ?: errorResponse.message ?: "Đã xảy ra lỗi"
                    }
                }

                // Check message field
                !errorResponse.message.isNullOrBlank() -> {
                    Log.d(TAG, "Using message field: ${errorResponse.message}")
                    errorResponse.message
                }

                // Check error field
                !errorResponse.error.isNullOrBlank() -> {
                    Log.d(TAG, "Using error field: ${errorResponse.error}")
                    errorResponse.error
                }

                else -> {
                    Log.d(TAG, "No specific error found, using default")
                    "Đã xảy ra lỗi"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse error response", e)
            // If JSON parsing fails, return the raw error body if it's readable
            if (errorBody.length < 200) errorBody else "Đã xảy ra lỗi"
        }
    }

    override suspend fun login(username: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== LOGIN REQUEST ==========")
            Log.d(TAG, "Username: $username")

            val response = authApi.login(LoginRequest(username, password))

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val loginResponse = response.body()

                Log.d(TAG, "========== LOGIN RESPONSE ==========")
                Log.d(TAG, "Response body is null: ${loginResponse == null}")
                Log.d(TAG, "Success flag: ${loginResponse?.success}")
                Log.d(TAG, "Message: ${loginResponse?.message}")
                Log.d(TAG, "Data is null: ${loginResponse?.data == null}")
                Log.d(TAG, "Token: ${loginResponse?.data?.token?.take(50)}...")
                Log.d(TAG, "Token length: ${loginResponse?.data?.token?.length}")
                Log.d(TAG, "Phone: ${loginResponse?.data?.phone}")
                Log.d(TAG, "Roles: ${loginResponse?.data?.roles}")

                if (loginResponse != null && loginResponse.success) {
                    // Lưu token TRƯỚC KHI gọi API /users/me
                    val token = loginResponse.data?.token

                    Log.d(TAG, "========== SAVING TOKEN ==========")
                    Log.d(TAG, "Token to save: ${token?.take(50)}...")
                    Log.d(TAG, "Token length: ${token?.length}")

                    if (!token.isNullOrEmpty()) {
                        userPreferencesDataStore.saveAuthTokens(token, "")
                        Log.d(TAG, "✓ Token saved to DataStore")

                        // Gọi API /users/me ngay sau khi login để lấy thông tin đầy đủ
                        Log.d(TAG, "========== FETCHING USER INFO FROM /users/me ==========")
                        try {
                            val userInfoResponse = userApi.getCurrentUser()

                            if (userInfoResponse.isSuccessful) {
                                val userResponse = userInfoResponse.body()
                                Log.d(TAG, "User info response: ${userResponse?.success}")
                                Log.d(TAG, "Fullname: ${userResponse?.data?.fullname}")
                                Log.d(TAG, "Email: ${userResponse?.data?.email}")

                                if (userResponse != null && userResponse.success && userResponse.data != null) {
                                    val fullUser = userResponse.toUser()
                                    if (fullUser != null) {
                                        // Lưu thông tin đầy đủ từ /users/me
                                        saveUserSession(fullUser)
                                        Log.d(TAG, "✓ Full user info saved from /users/me")
                                        Log.d(TAG, "✓ User fullName: ${fullUser.fullName}")
                                        Log.d(TAG, "======================================")
                                        emit(Resource.Success(fullUser))
                                        return@flow
                                    } else {
                                        Log.e(TAG, "✗ Failed to parse user from /users/me")
                                    }
                                } else {
                                    Log.e(TAG, "✗ Invalid response from /users/me")
                                }
                            } else {
                                Log.e(TAG, "✗ /users/me API failed with code: ${userInfoResponse.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "✗ Exception calling /users/me: ${e.message}")
                        }

                        // Fallback: Nếu /users/me lỗi, dùng thông tin từ login response
                        Log.d(TAG, "Using basic user info from login response as fallback")
                        val basicUser = loginResponse.toUser()
                        if (basicUser != null) {
                            saveUserSession(basicUser)
                            Log.d(TAG, "✓ Basic user session saved")
                            Log.d(TAG, "======================================")
                            emit(Resource.Success(basicUser))
                        } else {
                            Log.e(TAG, "✗ Failed to parse user data")
                            Log.d(TAG, "======================================")
                            emit(Resource.Error("Failed to parse user data"))
                        }
                    } else {
                        Log.e(TAG, "✗ Token is null or empty!")
                        emit(Resource.Error("Không nhận được token từ server"))
                    }
                } else {
                    Log.e(TAG, "✗ Login response is null or not successful")
                    Log.d(TAG, "======================================")
                    emit(Resource.Error(loginResponse?.message ?: "Đăng nhập thất bại"))
                }
            } else {
                // Parse error response from backend
                val errorMessage = parseErrorResponse(response.errorBody()?.string())
                Log.e(TAG, "✗ Login failed: $errorMessage")
                Log.d(TAG, "======================================")
                emit(Resource.Error(errorMessage))
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "✗ Login timeout (SocketTimeoutException): ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error("Kết nối tới máy chủ quá chậm (timeout). Vui lòng thử lại."))
        } catch (e: Exception) {
            Log.e(TAG, "✗ Login exception: ${e.message}", e)
            Log.d(TAG, "======================================")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "Registering user - Phone: $phone, Email: $email, FullName: $fullName")

            val response = authApi.register(
                RegisterRequest(
                    fullname = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    confirmPassword = password
                )
            )

            Log.d(TAG, "Register response code: ${response.code()}")

            if (response.isSuccessful) {
                val registerResponse = response.body()
                Log.d(TAG, "Register response body: $registerResponse")

                if (registerResponse != null && registerResponse.success) {
                    // Backend không trả user info, tạo mock user để UI hiển thị
                    val mockUser = User(
                        id = "",
                        email = email,
                        fullName = fullName,
                        phoneNumber = phone,
                        avatar = null,
                        role = UserRole.USER,
                        isVerified = false,
                        createdAt = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                        updatedAt = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                        bankName = null,
                        bankAccountNumber = null,
                        bankAccountName = null,
                    )
                    emit(Resource.Success(mockUser))
                } else {
                    val errorMsg = registerResponse?.message ?: "Đăng ký thất bại"
                    Log.e(TAG, "Register failed: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                // Parse error response from backend
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Register failed with code ${response.code()}: $errorBody")

                val errorMessage = parseErrorResponse(errorBody)
                Log.d(TAG, "Final error message: $errorMessage")

                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register exception", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun logout(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            clearUserSession()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun forgotPassword(email: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "ForgotPassword - sending request for email: $email")
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            Log.d(TAG, "ForgotPassword - response code: ${response.code()}")
            Log.d(TAG, "ForgotPassword - response successful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "ForgotPassword - response body: $body")

                // Lấy message từ data hoặc message field
                val message = body?.data ?: body?.message ?: "Email đặt lại mật khẩu đã được gửi"
                Log.d(TAG, "ForgotPassword - message to display: $message")

                emit(Resource.Success(message))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "ForgotPassword - failed with code ${response.code()}, body: $errorBody")
                val errorMessage = parseErrorResponse(errorBody)
                emit(Resource.Error(errorMessage))
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "ForgotPassword timeout: ${e.message}", e)
            emit(Resource.Error("Kết nối tới máy chủ quá chậm (timeout). Vui lòng thử lại."))
        } catch (e: Exception) {
            Log.e(TAG, "ForgotPassword exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "ResetPassword - sending request with token")
            val response = authApi.resetPassword(
                com.example.bookingcourt.data.remote.dto.ResetPasswordRequest(
                    token = token,
                    newPassword = newPassword
                )
            )
            Log.d(TAG, "ResetPassword - response code: ${response.code()}")
            Log.d(TAG, "ResetPassword - response successful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "ResetPassword - response body: $body")

                // Lấy message từ data hoặc message field
                val message = body?.data ?: body?.message ?: "Mật khẩu đã được đặt lại thành công"
                Log.d(TAG, "ResetPassword - message to display: $message")

                emit(Resource.Success(message))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "ResetPassword - failed with code ${response.code()}, body: $errorBody")
                val errorMessage = parseErrorResponse(errorBody)
                emit(Resource.Error(errorMessage))
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "ResetPassword timeout: ${e.message}", e)
            emit(Resource.Error("Kết nối tới máy chủ quá chậm (timeout). Vui lòng thử lại."))
        } catch (e: Exception) {
            Log.e(TAG, "ResetPassword exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.changePassword(
                com.example.bookingcourt.data.remote.dto.ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
            if (response.isSuccessful) {
                val changePasswordResponse = response.body()
                if (changePasswordResponse != null && changePasswordResponse.success) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(changePasswordResponse?.message ?: "Đổi mật khẩu thất bại"))
                }
            } else {
                emit(Resource.Error("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun getCurrentUser(): Flow<Resource<User?>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== GET CURRENT USER ==========")

            // Kiểm tra token trước khi gọi API
            val token = userPreferencesDataStore.accessToken.first()
            Log.d(TAG, "Token exists: ${!token.isNullOrEmpty()}")
            Log.d(TAG, "Token length: ${token?.length}")

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "✗ No token found, cannot call API")
                // Thử lấy từ cache
                val cachedUser = userPreferencesDataStore.getUser()
                if (cachedUser != null) {
                    Log.d(TAG, "Using cached user: ${cachedUser.fullName}")
                    emit(Resource.Success(cachedUser))
                } else {
                    emit(Resource.Error("Vui lòng đăng nhập lại"))
                }
                return@flow
            }

            // Gọi API /api/users/me
            Log.d(TAG, "Calling API /api/users/me...")
            val response = userApi.getCurrentUser()

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val userResponse = response.body()
                Log.d(TAG, "Response body: $userResponse")
                Log.d(TAG, "Success flag: ${userResponse?.success}")
                Log.d(TAG, "Data: ${userResponse?.data}")
                Log.d(TAG, "Fullname from API: ${userResponse?.data?.fullname}")
                Log.d(TAG, "Email from API: ${userResponse?.data?.email}")
                Log.d(TAG, "Phone from API: ${userResponse?.data?.phone}")

                if (userResponse != null && userResponse.success && userResponse.data != null) {
                    val user = userResponse.toUser()
                    Log.d(TAG, "User object created: ${user != null}")
                    Log.d(TAG, "User fullName: ${user?.fullName}")

                    if (user != null) {
                        // Lưu thông tin user vào DataStore
                        saveUserSession(user)
                        Log.d(TAG, "✓ User info retrieved and saved successfully")
                        Log.d(TAG, "======================================")
                        emit(Resource.Success(user))
                    } else {
                        Log.e(TAG, "✗ Failed to create user object from response")
                        emit(Resource.Error("Không thể xử lý thông tin người dùng"))
                    }
                } else {
                    Log.e(TAG, "✗ Invalid response format")
                    Log.e(TAG, "Success: ${userResponse?.success}, Data: ${userResponse?.data}")

                    // Fallback to cache
                    val cachedUser = userPreferencesDataStore.getUser()
                    if (cachedUser != null) {
                        Log.d(TAG, "Using cached user as fallback")
                        emit(Resource.Success(cachedUser))
                    } else {
                        emit(Resource.Error(userResponse?.message ?: "Không thể lấy thông tin người dùng"))
                    }
                }
            } else {
                // API trả về lỗi
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ API error - Code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                // Fallback to cache
                val cachedUser = userPreferencesDataStore.getUser()
                if (cachedUser != null) {
                    Log.d(TAG, "✓ Using cached user data (API failed)")
                    Log.d(TAG, "Cached user: ${cachedUser.fullName}")
                    emit(Resource.Success(cachedUser))
                } else {
                    Log.e(TAG, "✗ No cached user data available")
                    val errorMsg = when (response.code()) {
                        401 -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại"
                        403 -> "Không có quyền truy cập"
                        404 -> "Không tìm thấy thông tin ng��ời dùng"
                        else -> parseErrorResponse(errorBody)
                    }
                    emit(Resource.Error(errorMsg))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception when getting current user", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Message: ${e.message}")

            // Fallback to cache on any exception
            try {
                val cachedUser = userPreferencesDataStore.getUser()
                if (cachedUser != null) {
                    Log.d(TAG, "✓ Using cached user data (exception occurred)")
                    emit(Resource.Success(cachedUser))
                } else {
                    emit(Resource.Error("Không thể tải thông tin người dùng: ${e.message}"))
                }
            } catch (cacheException: Exception) {
                Log.e(TAG, "✗ Cache also failed", cacheException)
                emit(Resource.Error("Đã xảy ra lỗi: ${e.message}"))
            }
        }
    }

    override suspend fun updateProfile(user: User): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATE PROFILE ==========")
            Log.d(TAG, "Fullname: ${user.fullName}")
            Log.d(TAG, "Email: ${user.email}")

            val updateRequest = com.example.bookingcourt.data.remote.dto.UpdateUserRequest(
                fullname = user.fullName,
                email = user.email.takeIf { it.isNotEmpty() }
            )

            val response = userApi.updateUser(updateRequest)

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val userResponse = response.body()

                if (userResponse != null && userResponse.success && userResponse.data != null) {
                    val updatedUser = userResponse.toUser()

                    if (updatedUser != null) {
                        // Lưu thông tin user đã cập nhật vào cache
                        saveUserSession(updatedUser)
                        Log.d(TAG, "✓ Profile updated successfully")
                        Log.d(TAG, "======================================")
                        emit(Resource.Success(updatedUser))
                    } else {
                        Log.e(TAG, "✗ Failed to parse updated user")
                        emit(Resource.Error("Không thể cập nhật thông tin"))
                    }
                } else {
                    val errorMsg = userResponse?.message ?: "Không thể cập nhật thông tin"
                    Log.e(TAG, "✗ API returned success=false: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ API error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                emit(Resource.Error("Lỗi cập nhật thông tin: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Exception updating profile: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun updateBankInfo(
        bankName: String,
        accountNumber: String,
        accountHolderName: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== UPDATE BANK INFO ==========")
            Log.d(TAG, "Bank name: $bankName")
            Log.d(TAG, "Account number: $accountNumber")
            Log.d(TAG, "Account holder: $accountHolderName")

            val updateRequest = com.example.bookingcourt.data.remote.dto.UpdateUserRequest(
                bankName = bankName,
                bankAccountNumber = accountNumber,
                bankAccountName = accountHolderName
            )

            val response = userApi.updateUser(updateRequest)

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val userResponse = response.body()

                if (userResponse != null && userResponse.success && userResponse.data != null) {
                    val updatedUser = userResponse.toUser()

                    if (updatedUser != null) {
                        // Lưu thông tin user đã cập nhật vào cache
                        saveUserSession(updatedUser)
                        Log.d(TAG, "✓ Bank info updated successfully")
                        Log.d(TAG, "======================================")
                        emit(Resource.Success(updatedUser))
                    } else {
                        Log.e(TAG, "✗ Failed to parse updated user")
                        emit(Resource.Error("Không thể cập nhật thông tin"))
                    }
                } else {
                    Log.e(TAG, "✗ Invalid response")
                    emit(Resource.Error(userResponse?.message ?: "Cập nhật thất bại"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Update failed - Code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                val errorMsg = parseErrorResponse(errorBody)
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Update bank info exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun requestOwnerRole(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== REQUEST OWNER ROLE ==========")

            val response = userApi.requestOwnerRole()

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val baseResponse = response.body()

                if (baseResponse != null && baseResponse.success) {
                    val message = baseResponse.message ?: "Đã nâng cấp thành chủ sân thành công"
                    Log.d(TAG, "✓ Owner role requested successfully")
                    Log.d(TAG, "Message: $message")
                    Log.d(TAG, "======================================")

                    // Clear session để user phải đăng nhập lại
                    clearUserSession()

                    emit(Resource.Success(message))
                } else {
                    Log.e(TAG, "✗ Invalid response")
                    emit(Resource.Error(baseResponse?.message ?: "Yêu cầu thất bại"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Request failed - Code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                val errorMsg = parseErrorResponse(errorBody)
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Request owner role exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun switchToUserRole(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            Log.d(TAG, "========== SWITCH TO USER ROLE ==========")

            val response = userApi.switchToUserRole()

            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val baseResponse = response.body()

                if (baseResponse != null && baseResponse.success) {
                    val message = baseResponse.message ?: "Đã chuyển sang chế độ người dùng thành công"
                    Log.d(TAG, "✓ Switched to user role successfully")
                    Log.d(TAG, "Message: $message")
                    Log.d(TAG, "======================================")

                    // Clear session để user phải đăng nhập lại
                    clearUserSession()

                    emit(Resource.Success(message))
                } else {
                    Log.e(TAG, "✗ Invalid response")
                    emit(Resource.Error(baseResponse?.message ?: "Yêu cầu thất bại"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "✗ Switch to user role failed - Code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                val errorMsg = parseErrorResponse(errorBody)
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Switch to user role exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun saveUserSession(user: User) {
        userPreferencesDataStore.saveUser(user)
    }

    override suspend fun clearUserSession() {
        userPreferencesDataStore.clearUser()
    }

    override fun isLoggedIn(): Flow<Boolean> = userPreferencesDataStore.isLoggedIn
}
