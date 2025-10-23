package com.example.bookingcourt.domain.repository

import com.example.bookingcourt.core.common.Resource
import com.example.bookingcourt.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<Resource<User>>
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String,
    ): Flow<Resource<User>>
    suspend fun logout(): Flow<Resource<Unit>>
    suspend fun forgotPassword(email: String): Flow<Resource<Unit>>
    suspend fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>>
    suspend fun getCurrentUser(): Flow<Resource<User?>>
    suspend fun updateProfile(user: User): Flow<Resource<User>>
    suspend fun saveUserSession(user: User)
    suspend fun clearUserSession()
    fun isLoggedIn(): Flow<Boolean>
}
