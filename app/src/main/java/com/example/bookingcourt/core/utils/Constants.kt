package com.example.bookingcourt.core.utils

object Constants {
    // DevTunnel URL - thay đổi URL này khi tạo tunnel mới
    // Format: https://[tunnel-id]-[port].euw.devtunnels.ms/api/v1/
    // VD: https://abc123xyz-8080.euw.devtunnels.ms/api/v1/
    const val BASE_URL = "https://tnp23pw1-8080.asse.devtunnels.ms/"

    // Production URL (comment khi dev)
    // const val BASE_URL = "https://api.bookingcourt.com/api/v1/"

    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    const val DATABASE_NAME = "booking_court_database"
    const val DATASTORE_NAME = "booking_court_preferences"

    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 3

    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 32

    const val SESSION_TIMEOUT_MINUTES = 30
    const val CACHE_TIMEOUT_MINUTES = 5

    const val IMAGE_MAX_SIZE = 5 * 1024 * 1024
    const val ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/webp"

    const val DATE_FORMAT = "dd/MM/yyyy"
    const val TIME_FORMAT = "HH:mm"
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm"

    const val DEFAULT_AVATAR = "https://ui-avatars.com/api/?name="

    object PrefsKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val IS_LOGGED_IN = "is_logged_in"
        const val IS_ONBOARDING_COMPLETE = "is_onboarding_complete"
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
    }

    object SportTypes {
        const val BADMINTON = "BADMINTON"
        const val TABLE_TENNIS = "TABLE_TENNIS"
        const val TENNIS = "TENNIS"
        const val FOOTBALL = "FOOTBALL"
        const val BASKETBALL = "BASKETBALL"
        const val VOLLEYBALL = "VOLLEYBALL"
    }

    object BookingStatus {
        const val PENDING = "PENDING"
        const val CONFIRMED = "CONFIRMED"
        const val CANCELLED = "CANCELLED"
        const val COMPLETED = "COMPLETED"
        const val NO_SHOW = "NO_SHOW"
    }

    object PaymentStatus {
        const val PENDING = "PENDING"
        const val PAID = "PAID"
        const val FAILED = "FAILED"
        const val REFUNDED = "REFUNDED"
    }
}
