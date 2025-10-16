package com.example.bookingcourt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "court_id")
    val courtId: String,
    @ColumnInfo(name = "court_name")
    val courtName: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "user_name")
    val userName: String,
    @ColumnInfo(name = "user_phone")
    val userPhone: String,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long,
    @ColumnInfo(name = "total_price")
    val totalPrice: Long,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "cancellation_reason")
    val cancellationReason: String?,
    @ColumnInfo(name = "qr_code")
    val qrCode: String?,
)
