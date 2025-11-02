package com.example.bookingcourt.domain.model

/**
 * Venue domain model (Địa điểm sân)
 * Một venue có thể chứa nhiều courts
 */
data class Venue(
    val id: Long,
    val name: String,
    val numberOfCourt: Int,
    val address: Address,
    val courtsCount: Int,
    val pricePerHour: Long = 0,
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val phoneNumber: String? = null,
    val email: String? = null,
    val ownerPhone: String? = null // Số điện thoại chủ sân từ owner.phone
)

/**
 * Address domain model
 */
data class Address(
    val id: Long,
    val provinceOrCity: String,
    val district: String,
    val detailAddress: String
) {
    /**
     * Get full address as single string
     */
    fun getFullAddress(): String {
        return "$detailAddress, $district, $provinceOrCity"
    }
}
