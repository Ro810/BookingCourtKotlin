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
    val courtsCount: Int
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

