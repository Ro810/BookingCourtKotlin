package com.example.bookingcourt.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Full Venue detail DTO from backend API
 * Endpoints: GET /api/venues, GET /api/venues/{id}
 *
 * Venue (Cơ sở/Địa điểm) chứa nhiều Courts (Sân con)
 */
data class VenueDetailDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("numberOfCourt")
    val numberOfCourt: Int,

    @SerializedName("address")
    val address: AddressDto,

    @SerializedName("courtsCount")
    val courtsCount: Int? = null, // For GET /api/venues response

    @SerializedName("owner")
    val owner: OwnerDto? = null
)

/**
 * Address DTO from API
 */
data class AddressDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("provinceOrCity")
    val provinceOrCity: String,

    @SerializedName("district")
    val district: String,

    @SerializedName("detailAddress")
    val detailAddress: String
)

/**
 * Owner info in Venue response
 */
data class OwnerDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("fullname")
    val fullname: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("bankName")
    val bankName: String? = null,

    @SerializedName("bankAccountNumber")
    val bankAccountNumber: String? = null,

    @SerializedName("bankAccountName")
    val bankAccountName: String? = null
)

/**
 * Request DTO for creating a new venue
 * POST /venues
 */
data class CreateVenueRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("address")
    val address: CreateAddressRequest
)

/**
 * Address request for creating venue
 */
data class CreateAddressRequest(
    @SerializedName("provinceOrCity")
    val provinceOrCity: String,

    @SerializedName("district")
    val district: String,

    @SerializedName("detailAddress")
    val detailAddress: String
)
