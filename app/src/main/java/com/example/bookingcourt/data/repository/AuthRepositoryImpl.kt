package com.example.bookingcourt.data.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.data.local.datastore.UserPreferencesDataStore
import com.example.bookingcourt.data.remote.api.AuthApi
import com.example.bookingcourt.data.remote.dto.ForgotPasswordRequest
import com.example.bookingcourt.data.remote.dto.LoginRequest
import com.example.bookingcourt.data.remote.dto.RegisterRequest
import com.example.bookingcourt.domain.model.User
import com.example.bookingcourt.domain.model.UserRole
import com.example.bookingcourt.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.success) {
                    val user = loginResponse.toUser()
                    if (user != null) {
                        // Lưu token từ backend
                        val token = loginResponse.data?.token
                        if (!token.isNullOrEmpty()) {
                            userPreferencesDataStore.saveAuthTokens(token, "")
                        }
                        saveUserSession(user)
                        emit(Resource.Success(user))
                    } else {
                        emit(Resource.Error("Failed to parse user data"))
                    }
                } else {
                    emit(Resource.Error(loginResponse?.message ?: "Đăng nhập thất bại"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Lỗi kết nối: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
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
            val response = authApi.register(
                RegisterRequest(
                    fullname = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    confirmPassword = password // Thêm confirmPassword
                )
            )
            if (response.isSuccessful) {
                val registerResponse = response.body()
                if (registerResponse != null && registerResponse.success) {
                    // Backend không trả user info, tạo mock user để UI hiển thị
                    // User thực sẽ login sau để lấy thông tin đầy đủ
                    val mockUser = User(
                        id = "",
                        email = email,
                        fullName = fullName,
                        phoneNumber = phone,
                        avatar = null,
                        role = UserRole.USER,  // Mặc định là USER (ROLE_USER từ backend)
                        isVerified = false,
                        createdAt = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                        updatedAt = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()),
                    )
                    // Không saveUserSession vì chưa có token
                    // User cần login sau khi đăng ký
                    emit(Resource.Success(mockUser))
                } else {
                    emit(Resource.Error(registerResponse?.message ?: "Đăng ký thất bại"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Lỗi kết nối: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
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

    override suspend fun forgotPassword(email: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.changePassword(
                com.example.bookingcourt.data.remote.dto.ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
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
            val user = userPreferencesDataStore.getUser()
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi"))
        }
    }

    override suspend fun updateProfile(user: User): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            // API call to update profile
            saveUserSession(user)
            emit(Resource.Success(user))
        } catch (e: Exception) {
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
