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
    suspend fun forgotPassword(email: String): Flow<Resource<String>>
    suspend fun resetPassword(token: String, newPassword: String): Flow<Resource<String>>
    suspend fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<Unit>>
    suspend fun getCurrentUser(): Flow<Resource<User?>>
    suspend fun updateProfile(user: User): Flow<Resource<User>>
    suspend fun updateBankInfo(bankName: String, accountNumber: String, accountHolderName: String): Flow<Resource<User>>
    suspend fun requestOwnerRole(): Flow<Resource<String>>
    suspend fun switchToUserRole(): Flow<Resource<String>>
    suspend fun saveUserSession(user: User)
    suspend fun clearUserSession()
    fun isLoggedIn(): Flow<Boolean>
}
