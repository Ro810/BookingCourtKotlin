package com.example.bookingcourt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookingcourt.data.local.entity.CourtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourtDao {
    @Query("SELECT * FROM courts")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts WHERE sport_type = :sportType")
    fun getCourtsBySportType(sportType: String): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts WHERE id = :courtId")
    suspend fun getCourtById(courtId: String): CourtEntity?

    @Query("SELECT * FROM courts WHERE id = :courtId")
    fun getCourtByIdFlow(courtId: String): Flow<CourtEntity?>

    @Query("SELECT * FROM courts WHERE is_favorite = 1")
    fun getFavoriteCourts(): Flow<List<CourtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourt(court: CourtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourts(courts: List<CourtEntity>)

    @Update
    suspend fun updateCourt(court: CourtEntity)

    @Query("UPDATE courts SET is_favorite = :isFavorite WHERE id = :courtId")
    suspend fun updateFavoriteStatus(courtId: String, isFavorite: Boolean)

    @Query("DELETE FROM courts WHERE id = :courtId")
    suspend fun deleteCourtById(courtId: String)

    @Query("DELETE FROM courts")
    suspend fun deleteAllCourts()

    @Query("DELETE FROM courts WHERE cached_at < :timestamp")
    suspend fun deleteOldCachedCourts(timestamp: Long)

    @Query("SELECT * FROM courts WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchCourts(query: String): Flow<List<CourtEntity>>
}
