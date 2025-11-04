package com.example.bookingcourt.data.remote.api

import com.example.bookingcourt.data.remote.dto.ApiResponse
import com.example.bookingcourt.data.remote.dto.NotificationDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Notification API - Hệ thống thông báo
 * Backend: NotificationController.java
 */
interface NotificationApi {
    /**
     * Lấy danh sách thông báo của user hiện tại
     * GET /api/notifications
     * Requires: isAuthenticated()
     * Backend: NotificationController.getMyNotifications()
     */
    @GET("notifications")
    suspend fun getMyNotifications(): Response<ApiResponse<List<NotificationDto>>>

    /**
     * Lấy số lượng thông báo chưa đọc
     * GET /api/notifications/unread-count
     * Requires: isAuthenticated()
     * Backend: NotificationController.getUnreadCount()
     */
    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<Long>>

    /**
     * Đánh dấu một thông báo là đã đọc
     * PUT /api/notifications/{id}/read
     * Requires: isAuthenticated()
     * Backend: NotificationController.markAsRead()
     */
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long
    ): Response<ApiResponse<Void>>

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     * PUT /api/notifications/read-all
     * Requires: isAuthenticated()
     * Backend: NotificationController.markAllAsRead()
     */
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Void>>

    /**
     * Xóa một thông báo
     * DELETE /api/notifications/{id}
     * Requires: isAuthenticated()
     * Backend: NotificationController.deleteNotification()
     */
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: Long
    ): Response<ApiResponse<Void>>
}

